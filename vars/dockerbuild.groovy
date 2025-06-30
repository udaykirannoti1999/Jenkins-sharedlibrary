def call(imageName, imageTag) {
    sh """
        if docker images | grep -q ${imageName}; then
            docker rmi -f ${imageName}:${imageTag}
        fi
    """
    sh "docker build -t ${imageName}:${imageTag} ."
}
