import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.StackCommand
import org.s4s0l.shathel.commons.scripts.HttpApis
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.json.StringEscapeUtils
import static groovyx.net.http.ContentType.JSON
import org.s4s0l.shathel.commons.utils.TemplateUtils
import groovyx.net.http.ContentType;

def log(String x) {
    LOGGER.info(x)
}


cleanup = {
    LOGGER.info("cleanup...")
    deleteFile("maven-internal","1.0.0",token)
}

def feature(input, test_code,cleanup_closure = cleanup) {
    LOGGER.info("\n\n\n\n\n\nTESTING feature {}:", input)
    try {
        test_code()
        LOGGER.info('result: OK')
    } catch (Throwable ex){
        LOGGER.error("result: FAILED due to", ex)
        throw new RuntimeException("feature $input is not satisfied",ex)
    } finally {
        cleanup_closure()
    }
}


EnvironmentContext environmentContext = context;
ExecutableApiFacade apii = api;
StackCommand stackCommand = command;
HttpApis httpApi = http

prefix="/nexus"

def address = "http://${api.openPublishedPort(8082)}"
nexus = httpApi.waitAndGetClient(address, [200, 401], "$prefix/", 10)
envVars = environmentContext.getAsEnvironmentVariables()
token = Base64.getEncoder().encodeToString(("admin:${envVars.get("SHATHEL_ENV_NEXUS_ADMIN_PASS")}").bytes)
token_ci = Base64.getEncoder().encodeToString(("ci:${envVars.get("SHATHEL_ENV_NEXUS_CI_PASS")}").bytes)
token_dev = Base64.getEncoder().encodeToString(("dev:${envVars.get("SHATHEL_ENV_NEXUS_DEV_PASS")}").bytes)


pomFile = new File(new File(command.description.getStackResources().getComposeFileDirectory(),"test"), "foo.pom")
assert pomFile.exists() && pomFile.isFile(): "feature pom file must exist!"

feature 'anonymous access should be disabled', {
    assert nexus.get(
            contentType: ContentType.TEXT,
            headers: [Accept: "application/xml"],
            path: "$prefix/repository/maven-internal/org/foo/1.0.0/foo-1.0.0.pom",
    ).status == 401
}

feature 'admin should be able to query', {
    log "deleting file so the state is deterministic"
    deleteFile("maven-internal","1.0.0",token)
    assert getFile("maven-internal","1.0.0",token).status == 404
}

feature 'admin should be able to upload', {
    assert uploadFile("maven-internal","1.0.0",token).status == 201
    log "successfully uploaded file"
    assert getFile("maven-internal","1.0.0",token).status == 200
    log "successfully got file"
}

feature 'maven-internal should disable overriding artifacts', {
    assert uploadFile("maven-internal","1.0.0",token).status == 201
    assert uploadFile("maven-internal","1.0.0",token).status == 400
}


feature 'dev should be able to read but not to upload',{
    assert getFile("maven-internal","1.0.0",token_dev).status == 404
    assert uploadFile("maven-internal","1.0.0",token_dev).status == 403
    assert getFile("maven-internal","1.0.0",token_dev).status == 404
}


feature 'ci should be able to read and upload but not to delete',{
    assert getFile("maven-internal","1.0.0",token_ci).status == 404
    assert uploadFile("maven-internal","1.0.0",token_ci).status == 201
    assert getFile("maven-internal","1.0.0",token_ci).status == 200
    assert deleteFile("maven-internal","1.0.0",token_ci).status == 403
    assert getFile("maven-internal","1.0.0",token_ci).status == 200
}






def uploadFile(String repo, String version, String my_token) {
    LOGGER.info("uploading artifact with version: $version and to repo $repo")
    return nexus.put(
            contentType: ContentType.TEXT,
            headers: [Authorization: "Basic $my_token", Accept: "application/json"],
            body: pomFile,
            path: "$prefix/repository/$repo/org/foo/$version/foo-${version}.pom",
    )
}

def getFile(String repo, String version, String my_token) {
    LOGGER.info("getting artifact with version: $version and to repo $repo")
    return nexus.get(
            contentType: ContentType.TEXT,
            headers: [Authorization: "Basic $my_token", Accept: "application/xml"],
            path: "$prefix/repository/$repo/org/foo/$version/foo-${version}.pom",
    )
}

def deleteFile(String repo, String version, String my_token) {
    LOGGER.info("deleting artifact with version: $version and to repo $repo")
    return nexus.delete(
            contentType: ContentType.ANY,
            headers: [Authorization: "Basic $my_token", Accept: "application/json"],
            path: "$prefix/repository/$repo/org/foo/$version/foo-${version}.pom",
    )
}
