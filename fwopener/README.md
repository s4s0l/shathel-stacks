Add dependency to fwopener in tour stack and then use like:

```yml
version: "3"
services:
  metadata:
    image: ianneub/network-tools
    command: /bin/bash -c "nc -l -u -p 33334"
    ports:
      - "33334:33334/udp"
    deploy:
      labels:
        org.shathel.fwopener.udp: "33334"
  metadata2:
    image: ianneub/network-tools
    command: /bin/bash -c "nc -l -u -p 33335"
    ports:
      - "33335:33335/udp"
    deploy:
      labels:
        org.shathel.fwopener.udp: "33335:33336"



```

