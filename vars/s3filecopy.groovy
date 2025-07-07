
def call(String localFilePath, String s3DestinationPath) {
    if (!localFilePath || !s3DestinationPath) {
        error "Both localFilePath and s3DestinationPath are required."
    }

    sh "aws s3 cp ${localFilePath} ${s3DestinationPath}"
}
