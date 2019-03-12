import org.sonatype.nexus.blobstore.api.BlobStoreManager
import org.sonatype.nexus.repository.storage.WritePolicy
import org.sonatype.nexus.repository.maven.VersionPolicy
import org.sonatype.nexus.repository.maven.LayoutPolicy

def name = "${SHATHEL_ENV_NEXUS_INSTALL_NAME}"
def storeName = "${name}-store"

def repoDockerHostedName = "${name}-docker-snapshots"
def repoDockerReleasesName = "${name}-docker-releases"
def repoDockerProxyName = "${name}-docker-hub-proxy"
def repoDockerGroupName = "${name}-docker-group"


if (!repository.getRepositoryManager().exists(repoDockerReleasesName)) {
    repository.createDockerHosted(
            repoDockerReleasesName,
            5442,
            null,
            storeName,
            false,
            true,
            WritePolicy.ALLOW_ONCE);
}

if (!repository.getRepositoryManager().exists(repoDockerHostedName)) {
    repository.createDockerHosted(
            repoDockerHostedName,
            5443,
            null,
            storeName,
            false,
            true,
            WritePolicy.ALLOW);
}

if (!repository.getRepositoryManager().exists(repoDockerProxyName)) {
    repository.createDockerProxy(
            repoDockerProxyName,
            "https://registry-1.docker.io",
            "HUB",
            null,
            5444,
            null,
            storeName,
            true,
            false);
}


if (!repository.getRepositoryManager().exists(repoDockerGroupName)) {
    repository.createDockerGroup(
            repoDockerGroupName,
            5445,
            null,
            [repoDockerProxyName, repoDockerHostedName, repoDockerReleasesName],
            false,
            storeName);
}

return "done"