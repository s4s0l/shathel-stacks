# shathel-stacks
Shathel core stacks.

Core [shathel](https://github.com/s4s0l/shathel) stacks. For bootstrapping common environment tools like proxy, ssl, grafana, logging infrastructure.

* [consul](./consul/README.md) - Consul cluster if you need more sofisticated service discovery.
* [core](./core/README.md) - Adds a common network to all stacks.
* [letsencrypt](./letsencrypt/README.md) - Adds certificates from [Let's Encrypt](https://letsencrypt.org/), and handles certificate reissuing.
* [fileabeat](./fileabeat/README.md) - Adds filebeat log collector from all container logs to logstore stack.
* [logstash](./logstash/README.md) - Adds logstash log collector from all container logs to logstore stack.
* [logstore](./logstore/README.md) - EK from ELK.
* [monitoring](./monitoring/README.md) - Monitoring tools, prometheus, grafana, preconfigured with infrastructure monitoring.
* [portainer](./portainer/README.md) - Simple docker web console [Portainer](https://github.com/portainer/portainer).
* [proxy](./proxy/README.md) - Sets up proxy with auto reconfiguration functionalities based on [docker-flow-proxy](https://github.com/vfarcic/docker-flow-proxy).
