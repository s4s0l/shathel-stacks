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
    LOGGER.info("-----db provisioner:" + x);
}


EnvironmentContext environmentContext = context;
ExecutableApiFacade apii = api;
StackCommand stackCommand = command;
HttpApis httpApi = http


def address = "http://${api.openPublishedPort(3000)}"
log("Waiting for connection")
ui = httpApi.waitAndGetClient(address, [200, 401], "", 1000)


log "Checking if already initialized"

envVars = environmentContext.getAsEnvironmentVariables()

String env_pass = envVars.get("SHATHEL_ENV_SQLPAD_PASS")
String env_user = envVars.get("SHATHEL_ENV_SQLPAD_USER")

pass = (env_pass == null || env_pass.isEmpty()) ? "admin123" : env_pass
user = (env_user == null || env_user.isEmpty()) ? "sql@ravenetics.com" : env_user

token = Base64.getEncoder().encodeToString(("$user:$pass").bytes)

result = ui.post(
        contentType: JSON,
        path: "/roachsql/api/signup",
        body: [
                email   : user,
                password: pass,
                passwordConfirmation: pass,
                redirect   : 'false'
        ]
)

assert result.status == 200
log "got valid response for /roachsql/api/signup"

// TODO for some reason basic auth works but app does not find admin role for user (while config file says otherwise...)
//resultGet = ui.post(
//        contentType: JSON,
//        headers: [Authorization: "Basic $token"],
//        path: "/api/connections",
//        body : [
//                driver:	'postgres',
//                host:	'127.0.0.1',
//                name:	'raoch-ui',
//                password:	'root',
//                port:	'26257',
//                username:	'root'
//        ]
//)
//assert resultGet.status == 200
//log "registered connection: postgres /api/connections"