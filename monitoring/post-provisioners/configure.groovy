#!/usr/bin/env groovy
import static groovyx.net.http.ContentType.JSON
import groovyx.net.http.HttpResponseDecorator

import groovyx.net.http.RESTClient
import groovyx.net.http.Status
import org.apache.commons.lang.StringUtils
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody



//@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')
//@Grab('org.apache.httpcomponents:httpmime:4.2.1')
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.scripts.HttpApis

def address = "http://${api.openPublishedPort(3000)}"
def log(String x) {
    LOGGER.info("-----grafana:" + x);
}
log("Waiting for connection")
def grafana = http.waitAndGetClient(address,[401,403],"/api/datasources")
def token = Base64.getEncoder().encodeToString(("admin:adminadmin").bytes)
HttpResponseDecorator result = grafana.get([
        requestContentType: JSON,
        headers           : [Authorization: "Basic $token"],
        path              : '/api/datasources'
])
assert result.status == 200
def initialized = result.data.collect {it.name} . contains("Prometheus")

if(!initialized){
    result = grafana.post([
            requestContentType: JSON,
            headers           : [Authorization: "Basic $token"],
            path              : '/api/datasources',
            body: [
                    name:"Prometheus",
                    type:"prometheus",
                    url:"http://prometheus:9090${env.getOrDefault("PROMETHEUS_CONTEXT_PATH", "/")}".toString(),
                    access:"proxy",
                    isDefault:true
            ]
    ])
    assert result.status == 200
    log("Initialized")
}else{
    log("Already initialized")
}

