def call(String fullImageName) {
    if (!fullImageName) {
        error "❌ dockerPush: fullImageName is required."
    }

    try {
        def ecrDomain = fullImageName.split('/')[0]
        echo "🔐 Logging into ECR: ${ecrDomain}"

        // Use /tmp/.docker for Docker config to avoid permission issues
        sh """
            mkdir -p /tmp/.docker
            aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin --config /tmp/.docker ${ecrDomain}
        """

        echo "📦 Pushing Docker image: ${fullImageName}"
        sh "docker --config /tmp/.docker push ${fullImageName}"

        echo "✅ Docker image pushed: ${fullImageName}"
    } catch (err) {
        error "❌ dockerPush failed: ${err.getMessage()}"
    }
}

