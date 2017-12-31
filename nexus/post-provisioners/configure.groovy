import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.StackCommand
import org.s4s0l.shathel.commons.scripts.HttpApis
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.json.StringEscapeUtils
import static groovyx.net.http.ContentType.JSON
import org.s4s0l.shathel.commons.utils.TemplateUtils
/*
        This is inspired by https://github.com/sonatype/nexus-book-examples/tree/NEXUS-14940-deprecate-service-siesta
 */

def log(String x) {
    LOGGER.info("-----nexus arti post provisioning:" + x);
}


EnvironmentContext environmentContext = context;
ExecutableApiFacade apii = api;
StackCommand stackCommand = command;
HttpApis httpApi = http


def address = "http://${api.openPublishedPort(8082)}"
log("Waiting for connection")
nexus = httpApi.waitAndGetClient(address, [200, 401], "/", 1000)


log "Checking if already initialized"

envVars = environmentContext.getAsEnvironmentVariables() << ["NEXUS_API_URL": address]
token = Base64.getEncoder().encodeToString(("admin:admin123").bytes)

result = nexus.get(
        headers: [Authorization: "Basic $token"],
        contentType: JSON,
        path: '/service/siesta/rest/beta/read-only',
)

if (result.status == 401){
    log "Default password is not working, will use SHATHEL_ENV_NEXUS_ADMIN_OVERRIDE_PASS or SHATHEL_ENV_NEXUS_ADMIN_PASS if it does not exist"
    String override_pass = envVars.get("SHATHEL_ENV_NEXUS_ADMIN_OVERRIDE_PASS")
    token =  (override_pass == null || override_pass.isEmpty()) ?
            Base64.getEncoder().encodeToString(("admin:${envVars.get("SHATHEL_ENV_NEXUS_ADMIN_PASS")}").bytes) :
            Base64.getEncoder().encodeToString(("admin:$override_pass").bytes)
    result = nexus.get(
            headers: [Authorization: "Basic $token"],
            contentType: JSON,
            path: '/service/siesta/rest/beta/read-only',
    )
}

assert result.status == 200
log "got valid response for /service/siesta/rest/beta/read-only"

new_token = Base64.getEncoder().encodeToString(("admin:${envVars.get("SHATHEL_ENV_NEXUS_ADMIN_PASS")}").bytes)

def getScripts() {
    def resultGet = nexus.get(
            contentType: JSON,
            headers: [Authorization: "Basic $token"],
            path: "/service/siesta/rest/v1/script"
    )
    assert resultGet.status == 200
    def scripts = resultGet.data.collect { it.name }
    return scripts
}


def uploadScript(String scriptText, String name) {
    def scripts = getScripts()
    log "Starting with scripts: [${scripts.join(",")}]"

    if (scripts.contains(name)) {
        log "Script by name $name already exists uploading new version:"
        def result = nexus.put(
                contentType: JSON,
                headers: [Authorization: "Basic $token", Accept: "application/json"],
                path: "/service/siesta/rest/v1/script/$name",
                body: [
                        name   : name,
                        content: scriptText,
                        type   : 'groovy'
                ]
        )
        assert result.status == 204
    } else {
        def result = nexus.post(
                contentType: JSON,
                headers: [Authorization: "Basic $token", Accept: "application/json"],
                path: "/service/siesta/rest/v1/script",
                body: [
                        name   : name,
                        content: scriptText,
                        type   : 'groovy'
                ]
        )
        assert result.status == 204
    }

    log "Finished with scripts: [${getScripts().join(",")}]"
}

//  curl -v -X POST -u $username:$password --header "Content-Type: text/plain" "$host/service/siesta/rest/v1/script/$name/run"
def runScript(String name) {
    log "starting running script $name"
    def result = nexus.post(
            contentType: 'text/plain',
            headers: [Authorization: "Basic $token", Accept: "application/json"],
            path: "/service/siesta/rest/v1/script/$name/run",
            body: [name: name, result: ""]
    )
    assert result.status == 200
    log "successfully run script: $name"

    def deleteRes = nexus.delete(
            headers: [Authorization: "Basic $token", Accept: "application/json"],
            path: "/service/siesta/rest/v1/script/$name"
    )

    if (deleteRes.status == 401) { // after changing default password... ;)
        deleteRes = nexus.delete(
                headers: [Authorization: "Basic $new_token", Accept: "application/json"],
                path: "/service/siesta/rest/v1/script/$name"
        )
    }

    assert deleteRes.status == 204
    log "saccessfully deleted run script $name for security reasons"
}


/**
 *
 * Scripts must be correctly formatted so replacements do not change meaning of script
 *
 * @param fileName mask that should be equal to filename in stack/scripts directory
 * @return script correct for placing in json string file. It cannot be multiline or contain unescaped double quotes
 */
def getScriptText(String fileName){
    File contextDir = command.description.getStackResources().getComposeFileDirectory()
    assert contextDir != null
    File scriptsDir = new File(contextDir, "scripts")
    File script = scriptsDir.listFiles().find {it.name.equals(fileName)}
    if (script == null || !script.exists()){
        throw new FileNotFoundException("Script for name $fileName not found in ${contextDir.getAbsolutePath()}")
    } else {
        String codeWithTemplatesRemoved = TemplateUtils.fillEnvironmentVariables(script.text, envVars)
        return StringEscapeUtils.escapeJava(codeWithTemplatesRemoved.replace("\n", "; "))
    }
}


uploadScript(getScriptText("maven_repository.groovy"), "maven_repository")
uploadScript(getScriptText("security.groovy"), "security")
uploadScript(getScriptText("changeAdminPassword.groovy"), "changeAdminPassword")


runScript("maven_repository")
runScript("security")
runScript("changeAdminPassword")
