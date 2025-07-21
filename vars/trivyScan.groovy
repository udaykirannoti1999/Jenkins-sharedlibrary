def call(String buildGitBranch, String envTag) {
    def imageFullName = "${buildGitBranch}-${envTag}"
    def reportFileHtml = "trivy-report.html"

    echo "🔍 Starting Trivy scan for image: ${imageFullName}"

    // Run Trivy scan and generate HTML report
    sh """
        timestamp=\$(TZ='Asia/Kolkata' date '+%Y-%m-%d %H:%M:%S'); echo \$timestamp
        git_branch='${buildGitBranch}'

        trivy image --severity HIGH,CRITICAL --ignore-unfixed --format table --exit-code 0 ${imageFullName} > trivy-output.txt

        echo "<html><head><title>Trivy Report</title></head><body><h2>Trivy Scan Result</h2>" > ${reportFileHtml}
        echo "<p>Scan Time: \${timestamp}</p><p>Git Branch: \${git_branch}</p><pre>" >> ${reportFileHtml}
        cat trivy-output.txt >> ${reportFileHtml}
        echo "</pre></body></html>" >> ${reportFileHtml}
    """

    def vulnCountRaw = sh(script: """
        grep -vE '^(\\+|\\|\\s*ID|\\s*\$)' trivy-output.txt | wc -l
    """, returnStdout: true).trim()

    def vulnCount = 0
    try {
        vulnCount = vulnCountRaw.toInteger()
    } catch (Exception e) {
        echo "⚠️ Failed to parse vulnerability count: ${e.message}"
    }

    echo "🛡️ Vulnerabilities found: ${vulnCount}"

    publishHTML target: [
        allowMissing: false,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: '.',
        reportFiles: reportFileHtml,
        reportName: "Trivy Vulnerability Report"
    ]

    return vulnCount
}
