import org.s4s0l.shathel.commons.cert.KeyCert
boolean localEnv= "local-swarm" == context.environmentDescription.type

Optional<KeyCert> key = api.certificateManager.getKeyAndCert("shathel-registry")
List<String> privateIpAddresses = api.nodes.findAll{it.role =="manager"}.collect {it.privateIp}
privateIpAddresses.addAll(["127.0.0.1", "localhost"])
KeyCert kc = key.orElseGet {api.certificateManager.generateKeyAndCert("shathel-registry", privateIpAddresses)}

File localCertsDir = new File(command.description.getStackResources().getComposeFileDirectory(), "certs")

File registryCrt = new File(localCertsDir, "registry.crt")
File registryKey = new File(localCertsDir, "registry.key")

registryKey.text = kc.key.text
registryCrt.text = "${kc.cert.text}\n${api.certificateManager.rootCaCert.text}"
