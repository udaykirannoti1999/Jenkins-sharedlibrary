def call(String serviceName) {
    def cluster = env.ECS_CLUSTER ?: 'devcluster'
    def desiredCount = 1
    def taskDefJsonPath = "task-def.json"

    def newTaskDefArn = registerNewTaskDefinition(taskDefJsonPath)
    updateEcsService(serviceName, cluster, desiredCount, newTaskDefArn)
    waitForServiceStability(serviceName, cluster)
}

def updateEcsService(String serviceName, String cluster, int desiredCount, String taskDefinitionArn) {
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
    sh """
        aws ecs wait services-stable \
          --cluster ${cluster} \
          --services ${serviceName}
    """
}

def registerNewTaskDefinition(String taskDefJsonPath) {
    def jsonContent = libraryResource(taskDefJsonPath)
    writeFile file: 'task-def.json', text: jsonContent

    def taskDefArn = sh(
        script: """
            aws ecs register-task-definition \
              --cli-input-json file://task-def.json \
              --query 'taskDefinition.taskDefinitionArn' \
              --output text
        """,
        returnStdout: true
    ).trim()

    return taskDefArn
}
