def call(String imageFullName) {
        // Generate JSON + HTML
    sh """
        mkdir -p ${env.TRIVY_CACHE_DIR}
        curl -sSL -o html.tpl ${env.TRIVY_TEMPLATE_URL}
        trivy image --cache-dir ${env.TRIVY_CACHE_DIR} --format json -o ${env.TRIVY_JSON_REPORT} ${imageFullName}
        trivy image --cache-dir ${env.TRIVY_CACHE_DIR} --format template --template "@html.tpl" -o ${env.TRIVY_HTML_REPORT} ${imageFullName}
    """

    def result = readJSON file: env.TRIVY_JSON_REPORT
    return result.Results.collectMany { it.Vulnerabilities ?: [] }
                         .count { it.Severity in ['HIGH', 'CRITICAL'] }
}
