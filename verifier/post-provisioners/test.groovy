log.info("Test Logging - post - provision")

assert context.safeStorage != null
assert context.environmentDescription != null
assert context.environmentDescription.name != null
assert context.environmentDescription.type != null
assert context.solutionDescription != null
assert context.solutionDescription.name != null
assert context.solutionDescription.environments.size() > 0
assert context.contextName != null
assert context.getAsEnvironmentVariables().size() > 0
assert context.getSettingsDirectory().exists()
assert context.getDataDirectory().exists()
assert context.getTempDirectory().exists()
assert context.getEnrichedDirectory().exists()
assert context.getDependencyCacheDirectory().exists()

assert api.nodes.size() > 0
assert api.managerNode.role == "manager"
assert api.managerNodeClient != null
assert api.managerNodeWrapper != null
assert api.secretManager != null
assert api.getDockerEnvs(api.nodes[0])  != null
assert api.getDocker(api.managerNode).daemonInfo().size() > 0

assert dir.exists()
assert new File(dir, "enrichers/file.groovy").exists()

assert http != null


assert env["SHATHEL_ENV_TYPE"] != null
assert currentNodes.size() > 0


assert command.type != null
assert command.description.name == "verifier"
assert command.description.deployName == "verifier"
assert command.composeModel != null


/**
 * Sample usage of http api and socket forwarding
 */
def addressToUse = api.openPublishedPort(53456)
def client = http.waitAndGetClient("http://${addressToUse}")
result = client.get(
        path: '/'
)
assert result.status == 200
log.info("Successfully called hello service via ${addressToUse}")
/**
 * Test of proxy access via monitoring api
 */
addressToUse =  api.managerNode.publicIp
//we do it twice as prxy has 2 instances
clientViaProxy = http.waitAndGetClient("http://${addressToUse}", [401], "/verifier/", 300)
def clientViaProxy = http.waitAndGetClient("http://${addressToUse}", [401], "/verifier/", 300)
result = clientViaProxy.get(
        path: '/verifier/',
        headers: [Authorization: "Basic YWRtaW46YWRtaW5hZG1pbg=="] //admin admin password yuck

)
assert result.status == 200
log.info("Successfully called hello service via ${addressToUse}")