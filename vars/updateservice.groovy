def call(String serviceName, Integer desiredCount = 1, String cluster = 'devcluster') {
    echo "Scaling ECS service '${serviceName}' in cluster '${cluster}' to desired count ${desiredCount}"

    sh """
        aws ecs update-service \
          --cluster ${cluster} \
          --service ${serviceName} \
          --desired-count ${desiredCount}
    """
}
