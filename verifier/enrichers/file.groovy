log.info("Test Logging - enricher")
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
assert api.getDockerEnvs(api.nodes[0]) != null
assert api.getDocker(api.managerNode).daemonInfo().size() > 0

assert stack.name != null
assert stack.deployName != null
assert stack.stackResources.stackDirectory.exists()
assert stack.stackResources.composeFileDirectory.exists()
assert stack.stackResources.composeFileModel != null
assert compose != null

assert env["SHATHEL_ENV_TYPE"] != null
assert stackContext.getStackTreeDescription() != null

assert provisioners instanceof List

provisioners.add("groovy:git@github.com/s4s0l/shathel-stacks:verifier/verifier-provisioner",
        { params -> params.log.info("Log from enricher provisioner") })