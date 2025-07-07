def call(String localFilePath, String s3DestinationPath) {
    if (!localFilePath?.trim() || !s3DestinationPath?.trim()) {
        error "Both 'localFilePath' and 's3DestinationPath' are required."
    }

    echo "Uploading ${localFilePath} to S3: ${s3DestinationPath}"

    try {
        sh """
            set -e
            aws s3 cp "${localFilePath}" "${s3DestinationPath}" --only-show-errors
        """
        echo "Upload successful: ${s3DestinationPath}"
    } catch (Exception e) {
        error "Upload failed: ${e.getMessage()}"
    }
}
