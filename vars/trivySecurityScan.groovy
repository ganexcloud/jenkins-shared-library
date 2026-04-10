void call(Map cfg = [:]) {
  String dockerImage = required(cfg, 'dockerImage')
  String imageRef = required(cfg, 'imageRef')
  String dockerfilePath = required(cfg, 'dockerfilePath')

  String workspace = stringValue(cfg, 'workspace', env.WORKSPACE)
  String severity = stringValue(cfg, 'severity', env.TRIVY_SEVERITY ?: 'CRITICAL')
  String exitCode = stringValue(cfg, 'exitCode', env.TRIVY_EXIT_CODE ?: '1')

  String ignoreFilePath = cfg.containsKey('ignoreFile') ? stringValue(cfg, 'ignoreFile', '') : "${workspace}/.trivyignore"
  String secretConfigPath = cfg.containsKey('secretConfig') ? stringValue(cfg, 'secretConfig', '') : "${workspace}/.trivy-secret.yaml"

  String vulnTimeout = stringValue(cfg, 'vulnTimeout', '15m')
  String secretTimeout = stringValue(cfg, 'secretTimeout', '10m')
  String configTimeout = stringValue(cfg, 'configTimeout', '10m')

  boolean enableSecretScan = booleanValue(cfg, 'enableSecretScan', true)
  boolean enableDockerfileScan = booleanValue(cfg, 'enableDockerfileScan', true)

  String ignoreFileArg = fileExists(ignoreFilePath) ? "--ignorefile ${shellQuote(ignoreFilePath)}" : ''
  String secretConfigArg = fileExists(secretConfigPath) ? "--secret-config ${shellQuote(secretConfigPath)}" : ''

  int vulnRc = sh(
    script: """
      cd /
      trivy image \
        --timeout ${vulnTimeout} \
        --exit-code ${exitCode} \
        --severity ${severity} \
        --scanners vuln \
        --pkg-types os,library \
        --no-progress \
        ${ignoreFileArg} \
        --format template \
        --template @contrib/html.tpl \
        -o ${shellQuote("${workspace}/trivy-reports/${dockerImage}-Vulnerabilities.html")} \
        ${shellQuote(imageRef)}
    """.stripIndent().trim(),
    returnStatus: true
  )

  int secretRc = 0
  if (enableSecretScan) {
    secretRc = sh(
      script: """
        cd /
        trivy image \
          --timeout ${secretTimeout} \
          --exit-code ${exitCode} \
          --severity ${severity} \
          --scanners secret \
          ${ignoreFileArg} \
          ${secretConfigArg} \
          --no-progress \
          --format table \
          -o ${shellQuote("${workspace}/trivy-reports/${dockerImage}-Secrets.txt")} \
          ${shellQuote(imageRef)}
      """.stripIndent().trim(),
      returnStatus: true
    )
  }

  int configRc = 0
  if (enableDockerfileScan) {
    configRc = sh(
      script: """
        cd /
        trivy config \
          --timeout ${configTimeout} \
          --exit-code ${exitCode} \
          --severity ${severity} \
          --misconfig-scanners dockerfile \
          ${ignoreFileArg} \
          --format table \
          -o ${shellQuote("${workspace}/trivy-reports/${dockerImage}-Dockerfile.txt")} \
          ${shellQuote(dockerfilePath)}
      """.stripIndent().trim(),
      returnStatus: true
    )
  }

  if (vulnRc != 0 || secretRc != 0 || configRc != 0) {
    error("""
Security scan failed for ${dockerImage}.

What failed:
- Vulnerability scan: ${statusLabel(vulnRc)}
- Secret scan: ${statusLabel(secretRc)}
- Dockerfile scan: ${statusLabel(configRc)}

Reports:
- Jenkins published report: Trivy Security Scan
- trivy-reports/${dockerImage}-Vulnerabilities.html
- trivy-reports/${dockerImage}-Secrets.txt
- trivy-reports/${dockerImage}-Dockerfile.txt

What to do:
- Open the "Trivy Security Scan" report published in this Jenkins build.
- Fix CRITICAL findings in the image, dependencies or Dockerfile.
- If the secret finding is a false positive, review `.trivy-secret.yaml` or `.trivyignore`.
- Re-run the pipeline after the fix.
    """.stripIndent().trim())
  }
}

private String required(Map cfg, String key) {
  def value = cfg[key]
  if (value == null || value.toString().trim().isEmpty()) {
    error("trivySecurityScan: `${key}` is required")
  }
  value.toString()
}

private String stringValue(Map cfg, String key, String defaultValue) {
  def value = cfg[key]
  value == null ? defaultValue : value.toString()
}

private boolean booleanValue(Map cfg, String key, boolean defaultValue) {
  def value = cfg[key]
  value == null ? defaultValue : value as boolean
}

private String statusLabel(int rc) {
  rc == 0 ? 'PASSED' : 'FAILED'
}

private String shellQuote(String value) {
  return "'" + value.replace("'", "'\"'\"'") + "'"
}
