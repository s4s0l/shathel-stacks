import org.sonatype.nexus.blobstore.api.BlobStoreManager
import org.sonatype.nexus.repository.storage.WritePolicy
import org.sonatype.nexus.repository.maven.VersionPolicy
import org.sonatype.nexus.repository.maven.LayoutPolicy

def name = "${SHATHEL_ENV_NEXUS_INSTALL_NAME}"
def storeName = "${name}-store"

def repoMvnHostedName = "${name}-mvn-hosted"
def repoMvnProxy1Name = "${name}-mvn-central-proxy"
def repoMvnProxy2Name = "${name}-mvn-jcenter-proxy"
def repoMvnGroupName = "${name}-mvn-group"

if (!repository.getRepositoryManager().exists(repoMvnHostedName)) {
    repository.createMavenHosted(
            repoMvnHostedName,
            storeName,
            true,
            VersionPolicy.RELEASE,
            WritePolicy.ALLOW_ONCE,
            LayoutPolicy.STRICT);
}
if (!repository.getRepositoryManager().exists(repoMvnProxy1Name)) {
    repository.createMavenProxy(
            repoMvnProxy1Name,
            'https://repo.maven.apache.org/maven2/',
            storeName,
            true,
            VersionPolicy.RELEASE,
            LayoutPolicy.STRICT);
}
if (!repository.getRepositoryManager().exists(repoMvnProxy2Name)) {
    repository.createMavenProxy(
            repoMvnProxy2Name,
            'http://jcenter.bintray.com/',
            storeName,
            true,
            VersionPolicy.RELEASE,
            LayoutPolicy.STRICT);
}
if (!repository.getRepositoryManager().exists(repoMvnGroupName)) {
    repository.createMavenGroup(
            repoMvnGroupName,
            [repoMvnHostedName, repoMvnProxy1Name, repoMvnProxy2Name],
            storeName
    )
}
return "done"