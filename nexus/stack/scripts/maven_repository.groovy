import org.sonatype.nexus.blobstore.api.BlobStoreManager
import org.sonatype.nexus.repository.storage.WritePolicy
import org.sonatype.nexus.repository.maven.VersionPolicy
import org.sonatype.nexus.repository.maven.LayoutPolicy
if (!repository.getRepositoryManager().exists('maven-internal')){ repository.createMavenHosted('maven-internal', BlobStoreManager.DEFAULT_BLOBSTORE_NAME, true, VersionPolicy.RELEASE,        WritePolicy.ALLOW_ONCE, LayoutPolicy.STRICT)
    log.info('created repository maven-internal'); return 'created maven-internal'} else { log.info('skiped creation of repository maven-internal'); return 'skipped' }