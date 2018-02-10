# rexray support for digital ocean

Works only on `shathel-envs:digital-ocean` environments.

Deploy this stack (no need to depend on it, but you can). In other service:

```$xslt
version: '3.4'
services:
  val-holder:
    image: busybox:1
    command: sleep 999999999999d
    volumes:
      - val-holder-volume:/database
    deploy:
      mode: replicated
      replicas: 2
volumes:
  val-holder-volume:
    name: 'val-holder'
    labels:
      org.shathel.rexray: 'true'

```
it will make val-holder-volume use driver rexray and change name to `${SHATHEL_ENV_SOLUTION_NAME}-val-holder-{{.Task.Slot}}`,
Label `org.shathel.rexray` can be:
* replicated - will change name to template `${SHATHEL_ENV_SOLUTION_NAME}-val-holder-{{.Task.Slot}}`, use it when services 
    using this volume are replicated
* true - same as replicated
* global - for services that are global, will make name look like `${SHATHEL_ENV_SOLUTION_NAME}-val-holder-{{.Node.Hostname}}`

`name` is optional, when missing volume id will be used as name.
