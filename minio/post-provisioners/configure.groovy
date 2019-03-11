import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.StackCommand
import org.s4s0l.shathel.commons.scripts.HttpApis
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.json.StringEscapeUtils
import static groovyx.net.http.ContentType.JSON
import org.s4s0l.shathel.commons.utils.TemplateUtils

def log(String x) {
    LOGGER.info("-----nexus arti post provisioning:" + x);
}


EnvironmentContext environmentContext = context;
ExecutableApiFacade apii = api;
StackCommand stackCommand = command;
HttpApis httpApi = http

def address = "http://${api.openPublishedPort(9000)}"
log("Waiting for connection")
httpApi.waitAndGetClient(address, [403], "/", 1000)

