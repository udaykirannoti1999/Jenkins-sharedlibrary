def call(String buildGitBranch, String envTag) {
    def imageFullName = "${buildGitBranch}-${envTag}".replaceAll('/', '-')

    echo "Building Docker image: ${imageFullName}"

    sh """
        docker build -t ${imageFullName} \
            --build-arg GIT_BRANCH=${buildGitBranch} \
            --build-arg ENV_TAG=${envTag} .
    """

    echo "Removing Docker image: ${imageFullName}"
    sh "docker rmi ${imageFullName} || true"

    return imageFullName
}
