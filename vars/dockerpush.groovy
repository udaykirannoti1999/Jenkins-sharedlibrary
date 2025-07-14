def call(String fullImageName) {
    if (!fullImageName) {
        error "❌ dockerPush: fullImageName is required."
    }

    try {
        def ecrDomain = fullImageName.split('/')[0]
        echo "🔐 Logging into ECR: ${ecrDomain}"

        // Set HOME=/tmp to avoid writing to /.docker
        sh """
            export HOME=/tmp
            aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin ${ecrDomain}
        """

        echo "📦 Pushing Docker image: ${fullImageName}"
        sh """
            export HOME=/tmp
            docker push ${fullImageName}
        """

        echo "✅ Docker image pushed: ${fullImageName}"
    } catch (err) {
        error "❌ dockerPush failed: ${err.getMessage()}"
    }
}


