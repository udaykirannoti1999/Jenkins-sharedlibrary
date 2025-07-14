def call(Map config) {
    if (!config.imageFullName || !config.s3Bucket) {
        error "Missing required parameters: 'imageFullName' and 's3Bucket' are mandatory."
    }

    def imageFullName    = config.imageFullName
    def jsonReportName   = env.TRIVY_JSON_REPORT ?: "scan_result.json"
    def htmlReportName   = env.TRIVY_HTML_REPORT ?: "trivy-report.html"

    script {
        stage('Trivy Security Scan') {
            sh """
                mkdir -p ${env.TRIVY_CACHE_DIR}
                curl -sSL -o html.tpl ${env.TRIVY_TEMPLATE_URL}
                trivy image --cache-dir ${env.TRIVY_CACHE_DIR} --format json -o ${jsonReportName} ${imageFullName}
                trivy image --cache-dir ${env.TRIVY_CACHE_DIR} --format template --template "@html.tpl" -o ${htmlReportName} ${imageFullName}
            """

            def scanResults = readJSON file: jsonReportName
            def criticalCount = 0

            scanResults?.Results?.each { result ->
                criticalCount += result.Vulnerabilities?.count { it.Severity == 'CRITICAL' } ?: 0
            }

            uploadReportToS3(jsonReportName, config)
            #uploadReportToS3(htmlReportName, config)

            if (criticalCount > 0 && config.get('failBuild', true)) {
                echo "BUILD FAILED: ${criticalCount} CRITICAL vulnerabilities found."
            } else if (criticalCount > 0) {
                echo "WARNING: ${criticalCount} CRITICAL vulnerabilities found."
            } else {
                echo "Scan passed. No CRITICAL vulnerabilities detected."
            }
        }
    }
}

private def uploadReportToS3(String reportName, Map config) {
    def reportType = reportName.endsWith(".html") ? "html" : "json"
    def s3Key = "trivy-scans/${config.imageFullName.replace(':', '/')}-report.${reportType}"
    def s3Uri = "s3://${config.s3Bucket}/${s3Key}"

    sh "aws s3 cp ${reportName} ${s3Uri}"
    echo "Report uploaded to ${s3Uri}"
}
