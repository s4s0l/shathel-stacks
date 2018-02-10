if(env['SHATHEL_ENV_TYPE'] == "remote" && env['SHATHEL_ENV_GAV'].contains("shathel-envs:digital-ocean")){
    def validVals = ['global':'{{.Node.Hostname}}', 'replicated':'{{.Task.Slot}}', 'true':'{{.Task.Slot}}']
    compose.yml?.volumes.each {
        if(validVals.keySet().contains(it.value?.labels['org.shathel.rexray'])){
            it.value.driver = "shrex".toString()
            def suffix = validVals[it.value?.labels['org.shathel.rexray']]
            it.value.name = "\${SHATHEL_ENV_SOLUTION_NAME}-${it.value.name ?: it.key }-$suffix".toString()
        }
    }
}else{
    log.warn("Rexray plugin will be disabled on evtironment ${env['SHATHEL_ENV_TYPE']} - ${env['SHATHEL_ENV_GAV']}")
}
