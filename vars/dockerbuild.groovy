def call() {
    def imageName = env.IMAGE_NAME
    def ecrRepoUrl = env.ECR_REPO_URL
    def branchName = params.BRANCH_NAME

    if (!imageName) {
        error "❌ Environment variable IMAGE_NAME is required (e.g., trivy-sample)"
    }

    if (!ecrRepoUrl) {
        error "❌ Environment variable ECR_REPO_URL is required (e.g., 123456789012.dkr.ecr.us-east-1.amazonaws.com/my-repo)"
    }

    if (!branchName) {
        error "❌ BRANCH_NAME parameter is required."
    }

    def imageTag = resolveTag(branchName)
    def fullImageName = "${ecrRepoUrl}:${imageTag}"

    try {
        sh """
            if docker images | grep -q ${imageName}; then
                docker rmi -f ${imageName}:${imageTag} || true
            fi
        """

        sh "docker build -t ${imageName}:${imageTag} ."
        sh "docker tag ${imageName}:${imageTag} ${fullImageName}"

        def ecrDomain = ecrRepoUrl.split('/')[0]
        sh "aws ecr get-login-password | docker login --username AWS --password-stdin ${ecrDomain}"

        sh "docker push ${fullImageName}"

        echo "✅ Docker image successfully pushed: ${fullImageName}"
    } catch (err) {
        error "❌ Docker build/push failed: ${err.getMessage()}"
    }
}

def resolveTag(String branchName) {
    switch (branchName) {
        case 'dev':
        case 'preprod':
        case 'prod':
            return branchName
        default:
            error "❌ Unknown BRANCH_NAME: '${branchName}'. Allowed: dev, preprod, prod"
    }
}
