def call(String serviceName) {
    def cluster = env.ECS_CLUSTER ?: 'devcluster'
    def desiredCount = 1
    def taskDefJsonPath = "task-def.json"

    def newTaskDefArn = registerNewTaskDefinition(taskDefJsonPath)
    updateEcsService(serviceName, cluster, desiredCount, newTaskDefArn)
    waitForServiceStability(serviceName, cluster)
}

def updateEcsService(String serviceName, String cluster, int desiredCount, String taskDefinitionArn) {
    echo "Updating ECS service '${serviceName}' in cluster '${cluster}' to use task definition '${taskDefinitionArn}' and desired count ${desiredCount}"

    sh """
        aws ecs update-service \
          --cluster ${cluster} \
          --service ${serviceName} \
          --desired-count ${desiredCount} \
          --task-definition ${taskDefinitionArn} \
          --force-new-deployment
    """
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

def registerNewTaskDefinition(String taskDefJsonPath) {
    echo "Registering new ECS task definition from ${taskDefJsonPath}"

    def taskDefArn = sh(
        script: """
            aws ecs register-task-definition \
              --cli-input-json file://${taskDefJsonPath} \
              --query 'taskDefinition.taskDefinitionArn' \
              --output text
        """,
        returnStdout: true
    ).trim()

    echo "Registered new task definition: ${taskDefArn}"
    return taskDefArn
}
