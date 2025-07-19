def call(String buildGitBranch, String envTag) {
    def imageFullName = "${buildGitBranch}-${envTag}"
    def reportFile = "trivy-report.html"

    echo "Scanning image: ${imageFullName}"

    // Run shell silently using `script` block with `sh` and redirect output
    script {
        sh """
            {
                trivy image --severity HIGH,CRITICAL ${imageFullName} --format table > trivy-output.txt
                echo "<html><head><title>Trivy Report</title></head><body><pre>" > ${reportFile}
                cat trivy-output.txt >> ${reportFile}
                echo "</pre></body></html>" >> ${reportFile}
            } > /dev/null 2>&1
        """
    }

    // Publish the HTML report without exposing scan in console
    publishHTML target: [
        allowMissing: false,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: '.',
        reportFiles: reportFile,
        reportName: "Trivy Vulnerability Report"
    ]
}


