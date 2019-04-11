FROM cockroachdb/cockroach:v2.0.5
RUN sed -i -e '2d' /etc/apt/sources.list
RUN apt-get update
RUN apt-get install -y dnsutils
RUN rm -rf /var/lib/apt/lists/*
ADD entrypoint.sh /entrypoint.sh
ADD init.sh /init.sh
RUN chmod +x /entrypoint.sh
RUN chmod +x /init.sh
ENTRYPOINT ["/entrypoint.sh"]
