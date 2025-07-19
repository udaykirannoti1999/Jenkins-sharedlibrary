def call(String buildGitBranch, String envTag) {
    def imageFullName = "${buildGitBranch}-${envTag}"
    def reportFileHtml = "trivy-report.html"
    def vulnSummaryFile = "trivy-summary.txt"
    def timestamp = new Date().format("yyyy-MM-dd HH:mm:ss")

    echo "🔍 Starting Trivy scan for image: ${imageFullName}"

    sh """
        # Run Trivy scan in JSON format
        trivy image --severity HIGH,CRITICAL --ignore-unfixed --format json ${imageFullName} > trivy-output.json
    """

    def json = readJSON file: 'trivy-output.json'

    def vulnerabilities = []
    if (json instanceof List) {
        vulnerabilities = json.collectMany { it.containsKey('Vulnerabilities') ? it.Vulnerabilities : [] }
    } else if (json.containsKey('Results')) {
        vulnerabilities = json.Results.collectMany { it.containsKey('Vulnerabilities') ? it.Vulnerabilities : [] }
    }

    def highCount = vulnerabilities.count { it.Severity == 'HIGH' }
    def criticalCount = vulnerabilities.count { it.Severity == 'CRITICAL' }
    def totalCount = highCount + criticalCount

    writeFile file: vulnSummaryFile, text: "HIGH: ${highCount}\nCRITICAL: ${criticalCount}\n"

    sh """
        echo "<html><head><title>Trivy Report</title></head><body><h2>Trivy Scan Result</h2>" > ${reportFileHtml}
        echo "<p>Scan Time: ${timestamp}</p><p>Git Branch: ${buildGitBranch}</p>" >> ${reportFileHtml}
        echo "<h3>Vulnerability Summary:</h3><pre>" >> ${reportFileHtml}
        cat ${vulnSummaryFile} >> ${reportFileHtml}
        echo "</pre><h3>Detailed Report:</h3><pre>" >> ${reportFileHtml}
        cat trivy-output.json >> ${reportFileHtml}
        echo "</pre></body></html>" >> ${reportFileHtml}
    """

    echo "🧮 Vulnerability Count Summary:"
    sh "cat ${vulnSummaryFile}"

    publishHTML target: [
        allowMissing: false,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: '.',
        reportFiles: reportFileHtml,
        reportName: "Trivy Vulnerability Report"
    ]

    return totalCount
}
