FROM cockroachdb/cockroach:v2.0.2
RUN apt-get update && apt-get install -y \
        dnsutils \
     && rm -rf /var/lib/apt/lists/*
ADD entrypoint.sh /entrypoint.sh
ADD init.sh /init.sh
RUN chmod +x /entrypoint.sh
RUN chmod +x /init.sh
ENTRYPOINT ["/entrypoint.sh"]
