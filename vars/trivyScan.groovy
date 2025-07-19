def call(String buildGitBranch, String envTag) {
    def imageFullName = "${buildGitBranch}-${envTag}"
    def reportFileHtml = "trivy-report.html"

    echo "🔍 Starting Trivy scan for image: ${imageFullName}"

    // Run Trivy scan and write output to a file
    sh """
        timestamp=\$(date '+%Y-%m-%d %H:%M:%S')
    // Run Trivy scan and write output to a file silently
    sh script: '''
        timestamp=$(date '+%Y-%m-%d %H:%M:%S')
        git_branch='${buildGitBranch}'

        trivy image --severity HIGH,CRITICAL --ignore-unfixed --format table ${imageFullName} > trivy-output.txt
        trivy image --severity HIGH,CRITICAL --ignore-unfixed --format table ${imageFullName} > trivy-output.txt 2>/dev/null

        echo "<html><head><title>Trivy Report</title></head><body><h2>Trivy Scan Result</h2>" > ${reportFileHtml}
        echo "<p>Scan Time: \${timestamp}</p><p>Git Branch: \${git_branch}</p><pre>" >> ${reportFileHtml}
        cat trivy-output.txt >> ${reportFileHtml}
        echo "</pre></body></html>" >> ${reportFileHtml}
    """
        {
            echo "<html><head><title>Trivy Report</title></head><body><h2>Trivy Scan Result</h2>"
            echo "<p>Scan Time: ${timestamp}</p><p>Git Branch: ${git_branch}</p><pre>"
            cat trivy-output.txt
            echo "</pre></body></html>"
        } > ${reportFileHtml}
    ''', returnStdout: false

    // Publish the HTML report
    publishHTML target: [
        allowMissing: false,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: '.',
        reportFiles: reportFileHtml,
        reportName: "Trivy Vulnerability Report"
    ]
}
