def call(String customMessage, String channel) {
    def slackChannel = channel ?: '#default-channel'  // Use passed channel or fallback

    wrap([$class: 'BuildUser']) {
        sh 'git show -s --pretty=%an > commit.txt'
        def author = readFile('commit.txt').trim()
        def result = currentBuild.currentResult ?: 'SUCCESS'

        slackSend(
            color: COLOR_MAP[result],
            channel: slackChannel,
            message: "*${result}:* Job ${env.JOB_NAME}\n" +
                     "Author Name: ${author}\n" +
                     "Build user: ${BUILD_USER}\n" +
                     "${customMessage}\n" +
                     "More info at: ${env.BUILD_URL}"
        )
        cleanWs()
    }
}

