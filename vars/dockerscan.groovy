def scanDockerImage(String imageFullName) {
    sh "trivy image --format json -o scan_result.json ${imageFullName}"
    def result = readJSON file: 'scan_result.json'
    return result.Results.collectMany { it.Vulnerabilities ?: [] }
                         .count { it.Severity in ['HIGH', 'CRITICAL'] }
}
