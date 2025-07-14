// vars/dockerBuild.groovy
def call(String imageName, String imageTag, String ecrRepoUrl) {
    if (!imageName || !imageTag || !ecrRepoUrl) {
        error "❌ dockerBuild: imageName, imageTag, and ecrRepoUrl are required."
    }

    def fullImageName = "${ecrRepoUrl}:${imageTag}"

    try {
        // Remove existing local image
        sh """
            if docker images | grep -q ${imageName}; then
                docker rmi -f ${imageName}:${imageTag} || true
            fi
        """

        // Build and tag the image
        sh "docker build -t ${imageName}:${imageTag} ."
        sh "docker tag ${imageName}:${imageTag} ${fullImageName}"

        echo "✅ Docker image built and tagged as ${fullImageName}"
        return fullImageName
    } catch (err) {
        error "❌ dockerBuild failed: ${err.getMessage()}"
    }
}
