# Overview

This setup will create fully functioning nexus instance with 3 users:

- *admin* - same as default super user but with changed password
- *dev* - readonly user for downloading artifacts
- *ci* - user for uploading images

By default view access anonymous access will be disabled.

example unencrypted settings used also for below tests:

```yaml
version: 1
shathel-solution:
  name: shathel
  environments:
    local:
      type: local-swarm
      build-allowed: true
      domain: localhost
      nexus_admin_mail: admin@admin.pl
      nexus_admin_pass: admin654
      nexus_dev_pass: dev123
      nexus_dev_mail: dev@dev.pl
      nexus_ci_mail: ci@admin.pl
      nexus_ci_pass: ci123
```

for anything more serious remember to encrypt properties using safe password 

To enable nice http logging use below commands in shathel-deployer cli:

```
log --level DEBUG --logger org.apache.http.wire
log --level DEBUG --logger org.apache.http.headers
```

For consecutive porovisioning there will be different behaviour when it comes to admin password (as it is changed by nexus/stack/scripts/changeAdminPassword.groovy).
Firstly the environment variable `SHATHEL_ENV_NEXUS_ADMIN_OVERRIDE_PASS` will be checked and if it's not available `SHATHEL_ENV_NEXUS_ADMIN_PASS` will be used as admin password.
This allows you to change it and still be able to provision it using shathel stack.

# Tests

To check status of scripts there are automated feature checks in post provisioner `test.groovy`.
They will use your environment properties to check behaviour of users. 