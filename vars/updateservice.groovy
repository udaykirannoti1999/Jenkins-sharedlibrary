def call(String serviceName) {
    def cluster = env.ECS_CLUSTER ?: 'devcluster'
    def desiredCount = 1
    def taskDefJsonPath = "task-defs/${serviceName}-task-def.json"

    try {
        def jsonContent = libraryResource(taskDefJsonPath)
        def newTaskDefArn = registerNewTaskDefinition(jsonContent, serviceName)
        updateEcsService(serviceName, cluster, desiredCount, newTaskDefArn)
        retry(3) {
            waitForServiceStability(serviceName, cluster)
        }
    } catch (err) {
        error "ECS deployment failed for service '${serviceName}': ${err.message}"
    }
}

def registerNewTaskDefinition(String jsonContent, String serviceName) {
    echo "Registering new ECS task definition for ${serviceName}"

    def localFile = "${serviceName}-task-def.json"
    writeFile file: localFile, text: jsonContent

    def taskDefArn = sh(
        script: """
            aws ecs register-task-definition \
              --cli-input-json file://${localFile} \
              --query 'taskDefinition.taskDefinitionArn' \
              --output text
        """,
        returnStdout: true
    ).trim()

    if (!taskDefArn || !taskDefArn.contains(serviceName)) {
        error "Task definition registration failed or mismatched family name for ${serviceName}"
    }

    echo "Registered task definition ARN: ${taskDefArn}"
    return taskDefArn
}

def updateEcsService(String serviceName, String cluster, int desiredCount, String taskDefinitionArn) {
    echo "Updating ECS service '${serviceName}' in cluster '${cluster}'"

    def exitCode = sh(
        script: """
            aws ecs update-service \
              --cluster ${cluster} \
              --service ${serviceName} \
              --desired-count ${desiredCount} \
              --task-definition ${taskDefinitionArn} \
              --force-new-deployment
        """,
        returnStatus: true
    )

    if (exitCode != 0) {
        error "Failed to update ECS service '${serviceName}'"
    }

    echo "Service '${serviceName}' updated successfully."
}

def waitForServiceStability(String serviceName, String cluster) {
    echo "Waiting for ECS service '${serviceName}' in cluster '${cluster}' to stabilize..."

    def exitCode = sh(
        script: """
            aws ecs wait services-stable \
              --cluster ${cluster} \
              --services ${serviceName}
        """,
        returnStatus: true
    )

    if (exitCode != 0) {
        error "Service '${serviceName}' did not stabilize in time."
    }

    echo "Service '${serviceName}' is stable."
}
