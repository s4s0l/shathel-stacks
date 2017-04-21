import groovy.json.JsonSlurper
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import groovyx.net.http.Status
import org.apache.commons.lang.StringUtils
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.StackCommand
import org.s4s0l.shathel.commons.scripts.HttpApis

import static groovyx.net.http.ContentType.JSON


def log(String x) {
    LOGGER.info("-----portainer:" + x);
}


EnvironmentContext environmentContext = context;
ExecutableApiFacade apii = api;
StackCommand stackCommand = command;
HttpApis httpApi = http



def address = "http://${api.openPublishedPort(9001)}"
log("Waiting for connection")
def portainer = httpApi.waitAndGetClient(address)
log "Checking if already initialized"

result = portainer.get(
        contentType: JSON,
        path: '/api/endpoints',
)

def nodesDefined = result.data.collect { it.Name }


portainer.encoder.'multipart/form-data' = {
    File file ->
        final MultipartEntity e = new MultipartEntity(
                HttpMultipartMode.STRICT)
        e.addPart(new FormBodyPart('file', new FileBody(file, file.getName(), 'application/x-x509-ca-cert', 'UTF8')))
        e
}

apii.nodes
        .findAll { !nodesDefined.contains(it.nodeName) }
        .each {

    def machineName = it.nodeName
    def envs = apii.getDockerEnvs(it)
    def certPath = envs['DOCKER_CERT_PATH']
    def machineIp = envs['DOCKER_HOST']
    if (machineIp == null) {
        return;
    }
    def tls = !StringUtils.isEmpty(certPath)
    machineIp = "tcp://${it.privateIp}:${tls ? 2376 : 2375}".toString()
    log "Adding $machineName as endpoint"
    result = portainer.post(
            requestContentType: JSON,
            contentType: JSON,
            query: [active: false],
            path: '/api/endpoints',
            body: [Name: machineName, URL: machineIp, TLS: tls]
    )
    assert result.status == 200
    if (tls) {
        def endpointId = result.data.Id
        def uploadFile = { String fileName ->
            log "Uploading $fileName to $machineName endpoint"
            result = portainer.post(
                    requestContentType: 'multipart/form-data',
                    contentType: JSON,
                    path: "/api/upload/tls/$endpointId/$fileName",
                    body: new File(certPath, "${fileName}.pem"),
            )
            assert result.status == 200
        }


        uploadFile "ca"
        uploadFile "cert"
        uploadFile "key"
    }
}

