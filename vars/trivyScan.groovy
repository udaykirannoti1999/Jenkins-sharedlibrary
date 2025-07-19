def call(String buildGitBranch, String envTag, Map options = [:]) {
    def imageFullName = "${buildGitBranch}-${envTag}"
    def reportFileHtml = "trivy-report.html"
    def reportFileTxt = "trivy-output.txt"
    def reportFileJson = "trivy-output.json"

    def ignoreUnfixed = options.get('ignoreUnfixed', true)
    def failOnVuln   = options.get('failOnVuln', true)
    def severity     = options.get('severity', 'HIGH,CRITICAL')
    def outputFormat = options.get('outputFormat', 'table')

    echo "🔍 Starting Trivy scan for image: ${imageFullName}"
    
    def trivyCommand = "trivy image --severity ${severity} " +
                       "${ignoreUnfixed ? '--ignore-unfixed' : ''} " +
                       "--format ${outputFormat} ${imageFullName}"

    // 1. Run Trivy scan
    def exitCode = sh(script: "${trivyCommand} > ${reportFileTxt}", returnStatus: true)

    // 2. Also generate JSON report for integration (even if not styled yet)
    sh(script: "trivy image --severity ${severity} ${ignoreUnfixed ? '--ignore-unfixed' : ''} --format json ${imageFullName} > ${reportFileJson}", returnStatus: true)

    // 3. Wrap plain report in HTML
    sh """
        echo "<html><head><title>Trivy Report</title></head><body><h2>Trivy Scan Result</h2><pre>" > ${reportFileHtml}
        cat ${reportFileTxt} >> ${reportFileHtml}
        echo "</pre></body></html>" >> ${reportFileHtml}
    """

    // 4. Publish HTML report
    publishHTML target: [
        allowMissing: false,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: '.',
        reportFiles: reportFileHtml,
        reportName: "Trivy Vulnerability Report"
    ]

    // 5. Fail the build if vulnerabilities were found
    if (failOnVuln && exitCode != 0) {
        error "❌ Trivy found vulnerabilities in ${imageFullName}. See published report."
    } else {
        echo "✅ Trivy scan completed with no blocking vulnerabilities."
    }
}



