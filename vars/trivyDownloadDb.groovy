void call(Map cfg = [:]) {
  String workspace = stringValue(cfg, 'workspace', env.WORKSPACE)
  boolean ensureReportsDir = booleanValue(cfg, 'ensureReportsDir', true)
  boolean downloadJavaDb = booleanValue(cfg, 'downloadJavaDb', false)

  sh """
    ${ensureReportsDir ? "mkdir -p ${shellQuote("${workspace}/trivy-reports/")}" : ''}
    trivy image --download-db-only --no-progress
    ${downloadJavaDb ? 'trivy image --download-java-db-only --no-progress' : ''}
  """.stripIndent().trim()
}

private String stringValue(Map cfg, String key, String defaultValue) {
  def value = cfg[key]
  value == null ? defaultValue : value.toString()
}

private boolean booleanValue(Map cfg, String key, boolean defaultValue) {
  def value = cfg[key]
  value == null ? defaultValue : value as boolean
}

private String shellQuote(String value) {
  return "'" + value.replace("'", "'\"'\"'") + "'"
}
