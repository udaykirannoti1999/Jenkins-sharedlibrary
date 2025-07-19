def call(String buildGitBranch, String envTag) {
    def imageFullName = "${buildGitBranch}-${envTag}"
    def reportFileHtml = "trivy-report.html"
    def vulnSummaryFile = "trivy-summary.txt"

    echo "🔍 Starting Trivy scan for image: ${imageFullName}"

    sh """
        timestamp=\$(date '+%Y-%m-%d %H:%M:%S')
        git_branch='${buildGitBranch}'

        trivy image --severity HIGH,CRITICAL --ignore-unfixed --format table ${imageFullName} > trivy-output.txt

        # Extract counts of HIGH and CRITICAL vulnerabilities
        grep -E '\\b(HIGH|CRITICAL)\\b' trivy-output.txt | awk '{print \$2}' | sort | uniq -c > ${vulnSummaryFile}

        echo "<html><head><title>Trivy Report</title></head><body><h2>Trivy Scan Result</h2>" > ${reportFileHtml}
        echo "<p>Scan Time: \${timestamp}</p><p>Git Branch: \${git_branch}</p>" >> ${reportFileHtml}
        echo "<h3>Vulnerability Summary:</h3><pre>" >> ${reportFileHtml}
        cat ${vulnSummaryFile} >> ${reportFileHtml}
        echo "</pre><h3>Detailed Report:</h3><pre>" >> ${reportFileHtml}
        cat trivy-output.txt >> ${reportFileHtml}
        echo "</pre></body></html>" >> ${reportFileHtml}
    """

    // Optional: Print the summary in console too
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
}




