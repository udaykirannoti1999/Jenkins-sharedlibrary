def call(String buildGitBranch, String envTag) {
    def imageFullName = "${buildGitBranch}-${envTag}"

    echo "Scanning image: ${imageFullName}"

    sh """
        # Run Trivy and show only HIGH and CRITICAL vulnerabilities in table format
        trivy image --severity HIGH,CRITICAL ${imageFullName}
    """
} 

