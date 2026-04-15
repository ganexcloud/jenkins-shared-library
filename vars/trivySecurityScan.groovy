void call(Map cfg = [:]) {
  String dockerImage = required(cfg, 'dockerImage')
  String workspace = stringValue(cfg, 'workspace', env.WORKSPACE)
  String imageRef = cfg.containsKey('imageRef') ? required(cfg, 'imageRef') : defaultImageRef(dockerImage)
  String dockerfilePath = cfg.containsKey('dockerfilePath') ? required(cfg, 'dockerfilePath') : defaultDockerfilePath(workspace, dockerImage)
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
  String reportDir = "${workspace}/trivy-reports"
  String statusDir = "${workspace}/.trivy-status/${dockerImage}"
  String vulnStatusFile = "${statusDir}/vuln.rc"
  String secretStatusFile = "${statusDir}/secret.rc"
  String configStatusFile = "${statusDir}/dockerfile.rc"

  int orchestrationRc = sh(
    script: """
      set -eu

      mkdir -p ${shellQuote(reportDir)} ${shellQuote(statusDir)}
      rm -f ${shellQuote(vulnStatusFile)} ${shellQuote(secretStatusFile)} ${shellQuote(configStatusFile)}

      set +e

      (
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
          -o ${shellQuote("${reportDir}/${dockerImage}-Vulnerabilities.html")} \
          ${shellQuote(imageRef)}
        echo \$? > ${shellQuote(vulnStatusFile)}
      ) &
      pid_vuln=\$!

      ${enableSecretScan ? """
      (
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
          -o ${shellQuote("${reportDir}/${dockerImage}-Secrets.txt")} \
          ${shellQuote(imageRef)}
        echo \$? > ${shellQuote(secretStatusFile)}
      ) &
      pid_secret=\$!
      """ : """
      echo 0 > ${shellQuote(secretStatusFile)}
      pid_secret=''
      """}

      ${enableDockerfileScan ? """
      (
        cd /
        trivy config \
          --timeout ${configTimeout} \
          --exit-code ${exitCode} \
          --severity ${severity} \
          --misconfig-scanners dockerfile \
          ${ignoreFileArg} \
          --format table \
          -o ${shellQuote("${reportDir}/${dockerImage}-Dockerfile.txt")} \
          ${shellQuote(dockerfilePath)}
        echo \$? > ${shellQuote(configStatusFile)}
      ) &
      pid_config=\$!
      """ : """
      echo 0 > ${shellQuote(configStatusFile)}
      pid_config=''
      """}

      wait "\$pid_vuln"
      [ -n "\$pid_secret" ] && wait "\$pid_secret" || true
      [ -n "\$pid_config" ] && wait "\$pid_config" || true

      exit 0
    """.stripIndent().trim(),
    returnStatus: true
  )

  if (orchestrationRc != 0) {
    error("trivySecurityScan: failed to orchestrate parallel scans for `${dockerImage}`")
  }

  int vulnRc = readStatusCode(vulnStatusFile)
  int secretRc = readStatusCode(secretStatusFile)
  int configRc = readStatusCode(configStatusFile)

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

private String requiredEnv(String key, String reason) {
  def value = env[key]
  if (value == null || value.toString().trim().isEmpty()) {
    error("trivySecurityScan: `${key}` is required ${reason}")
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

private String defaultImageRef(String dockerImage) {
  String awsAccountId = requiredEnv('AWS_ACCOUNT_ID', 'when `imageRef` is not provided')
  String awsRegion = requiredEnv('AWS_REGION', 'when `imageRef` is not provided')
  String ecrRepoName = requiredEnv('ECR_REPO_NAME', 'when `imageRef` is not provided')
  String buildNumber = requiredEnv('BUILD_NUMBER', 'when `imageRef` is not provided')

  "${awsAccountId}.dkr.ecr.${awsRegion}.amazonaws.com/${ecrRepoName}/${dockerImage}:${buildNumber}"
}

private String defaultDockerfilePath(String workspace, String dockerImage) {
  if (workspace == null || workspace.trim().isEmpty()) {
    error("trivySecurityScan: `WORKSPACE` is required when `dockerfilePath` is not provided")
  }

  "${workspace}/docker/${dockerImage}"
}

private int readStatusCode(String path) {
  if (!fileExists(path)) {
    error("trivySecurityScan: missing status file `${path}`")
  }

  readFile(path).trim().toInteger()
}

private String shellQuote(String value) {
  return "'" + value.replace("'", "'\"'\"'") + "'"
}
