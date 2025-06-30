def call(String serviceName, Integer desiredCount = 1, String cluster = 'devcluster') {
    echo "Scaling ECS service '${serviceName}' in cluster '${cluster}' to desired count ${desiredCount}"

    sh """
        aws ecs update-service \
          --cluster ${cluster} \
          --service ${serviceName} \
          --desired-count ${desiredCount}
    """
    waitForServiceStability(serviceName, cluster)
}

def waitForServiceStability(String serviceName, String cluster) {
    echo "Waiting for ECS service '${serviceName}' in cluster '${cluster}' to become stable..."

    sh """
        aws ecs wait services-stable \
          --cluster ${cluster} \
          --services ${serviceName}
    """

    echo "ECS service '${serviceName}' is now stable."
}
