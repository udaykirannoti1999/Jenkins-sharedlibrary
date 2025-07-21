def call(String buildGitBranch, String envTag) {
    def imageLabel = "${buildGitBranch} ${envTag}".replaceAll('/', '-')  // for logging
    def imageFullName = "${buildGitBranch}-${envTag}".replaceAll('/', '-')  // for Docker image name
    echo "Building Docker image: ${imageLabel}"

    sh """
      docker build -t ${imageFullName} --build-arg GIT_BRANCH=${buildGitBranch} --build-arg ENV_TAG=${envTag} .
      """
     return imageFullName
}
