def call(String reportPath = env.TRIVY_HTML_REPORT, String reportName = 'Trivy Vulnerability Report') {
    if (fileExists(reportPath)) {
        echo "Archiving Trivy HTML report and publishing to Jenkins UI."

        archiveArtifacts artifacts: reportPath, fingerprint: true

        publishHTML([
            reportDir: '.',
            reportFiles: reportPath,
            reportName: reportName,
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true
        ])
    } else {
        echo "⚠️ Trivy HTML report not found at ${reportPath}. Skipping HTML publishing."
    }
}
