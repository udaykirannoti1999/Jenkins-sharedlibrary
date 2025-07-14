// vars/dockerPush.groovy
def call(String fullImageName) {
    if (!fullImageName) {
        error "❌ dockerPush: fullImageName is required."
    }

    try {
        def ecrDomain = fullImageName.split('/')[0]
        sh "aws ecr get-login-password | docker login --username AWS --password-stdin ${ecrDomain}"
        sh "docker push ${fullImageName}"

        echo "✅ Docker image pushed: ${fullImageName}"
    } catch (err) {
        error "❌ dockerPush failed: ${err.getMessage()}"
    }
}

