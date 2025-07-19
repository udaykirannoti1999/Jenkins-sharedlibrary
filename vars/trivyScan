def call(String buildGitBranch, String envTag) {
    def imageFullName = "${buildGitBranch}-${envTag}"

    echo "Scanning image: ${imageFullName}"

    env.TRIVY_CACHE_DIR = "${env.WORKSPACE}/.trivycache"
    env.TRIVY_HTML_REPORT = "${env.WORKSPACE}/trivy-report.html"
    env.TRIVY_TEMPLATE_URL = "https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl"

    sh """
        mkdir -p ${env.TRIVY_CACHE_DIR}
        curl -sSL -o html.tpl ${env.TRIVY_TEMPLATE_URL}
        trivy image --cache-dir ${env.TRIVY_CACHE_DIR} --format template --template "@html.tpl" -o ${env.TRIVY_HTML_REPORT} ${imageFullName}
    """
}
