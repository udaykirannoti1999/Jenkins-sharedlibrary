def call(String imageFullName) {
    def (imageName, _) = imageFullName.tokenize(':')
    def ecrRepoUrl = env.ECR_REPO_URL

    if (!ecrRepoUrl) {
        error "Environment variable ECR_REPO_URL is required (e.g., 123456789012.dkr.ecr.us-east-1.amazonaws.com/my-repo)"
    }

    def imageTag = myecrtag()
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

def myecrtag() {
    def tag = ''
    if (params.BRANCH_NAME == 'dev') {
        tag = 'dev'
    } else if (params.BRANCH_NAME == 'preprod') {
        tag = 'preprod'
    } else if (params.BRANCH_NAME == 'prod') {
        tag = 'prod'
    } else {
        error "Unknown branch: ${params.BRANCH_NAME}. Cannot push Docker image."
    }

    return tag
}
