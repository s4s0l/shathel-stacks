FROM sonatype/nexus3:3.15.2





USER root
ENV S3_BLOBSTORE_VERSION=1.2.1-SNAPSHOT
RUN mkdir -p ${NEXUS_HOME}/system/org/sonatype/nexus/nexus-blobstore-s3/${S3_BLOBSTORE_VERSION}

ADD nexus-blobstore-s3-${S3_BLOBSTORE_VERSION}.jar ${NEXUS_HOME}/system/org/sonatype/nexus/nexus-blobstore-s3/${S3_BLOBSTORE_VERSION}/nexus-blobstore-s3-${S3_BLOBSTORE_VERSION}.jar

RUN sed -i.bak -e "/nexus-blobstore-file/a\\"$'\n'"<bundle>mvn:org.sonatype.nexus/nexus-blobstore-s3/${S3_BLOBSTORE_VERSION}</bundle>" ${NEXUS_HOME}/system/org/sonatype/nexus/assemblies/nexus-base-feature/*/nexus-base-feature-*-features.xml
RUN sed -i.bak -e "/nexus-blobstore-file/a\\"$'\n'"<bundle>mvn:org.sonatype.nexus/nexus-blobstore-s3/${S3_BLOBSTORE_VERSION}</bundle>" ${NEXUS_HOME}/system/org/sonatype/nexus/assemblies/nexus-core-feature/*/nexus-core-feature-*-features.xml

USER nexus