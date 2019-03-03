def name = "${SHATHEL_ENV_NEXUS_INSTALL_NAME}"

def storeName = "${name}-store"

def s3Bucket = "${SHATHEL_ENV_NEXUS_S3_BUCKET}"
def s3KeyId = "${SHATHEL_ENV_NEXUS_S3_KEY_ID}"
def s3KeySecret = "${SHATHEL_ENV_NEXUS_S3_KEY_SECRET}"
def s3Region = "${SHATHEL_ENV_NEXUS_S3_REGION}"
def s3Endpoint = "${SHATHEL_ENV_NEXUS_S3_ENDPOINT}"


existingBlobStore = blobStore.getBlobStoreManager().get(storeName)
if (existingBlobStore == null) {
    if (s3Bucket != "") {
        def config = [
                'bucket'         : s3Bucket,
                'accessKeyId'    : s3KeyId,
                'secretAccessKey': s3KeySecret,
                'region'         : s3Region,
                'endpoint'       : s3Endpoint,
                'expiration'     : '-1',
        ]
        blobStore.createS3BlobStore(storeName, config)
        return "created-s3"
    } else {
        blobStore.createFileBlobStore(storeName, storeName)
        return "created-file"
    }
} else {
    return "already-exists"
}
