def call(String imageFullName) {
    def (imageName, imageTag) = imageFullName.tokenize(':')
    def ecrRepoUrl = env.ECR_REPO_URL

    if (!ecrRepoUrl) {
        error "Environment variable ECR_REPO_URL is required (e.g., 123456789012.dkr.ecr.us-east-1.amazonaws.com/my-repo)"
    }

    def fullImageName = "${ecrRepoUrl}:${imageTag}"

    try {
        sh """
            if docker images | grep -q ${imageName}; then
                docker rmi -f ${imageName}:${imageTag}
            fi
        """

        sh "docker build -t ${imageName}:${imageTag} ."
        sh "docker tag ${imageName}:${imageTag} ${fullImageName}"
        sh "aws ecr get-login-password | docker login --username AWS --password-stdin ${ecrRepoUrl.split('/')[0]}"
        sh "docker push ${fullImageName}"

        echo "Image pushed to: ${fullImageName}"
    } catch (err) {
        error "Docker build/push failed: ${err.getMessage()}"
    }
}
