import org.s4s0l.shathel.commons.swarm.BuildingEnricher
def contextVariables = binding.variables
def buildingEnricher = new BuildingEnricher(Optional.of("127.0.0.1:4000"))
buildingEnricher.execute(contextVariables)
