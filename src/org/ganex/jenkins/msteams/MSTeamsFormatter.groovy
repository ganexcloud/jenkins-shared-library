package org.ganex.jenkins.msteams

String formatMessage(String title = '', String message = '', String testSummary = '') {
  def helper = new JenkinsHelper()
  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()
  def titleFormated = "${project} (#${buildNumber}) - ${title.trim()}"
  def color = status.getStatusColor()
  def result = ""
  if(message) result = "\n ${message.trim()}"
  if(testSummary) result = "\n ${testSummary}"
  def templateJson = """
    {
      "@type": "MessageCard",
      "@context": "http://schema.org/extensions",
      "themeColor": "${color}",
      "summary": "${titleFormated}",
      "sections": [
        {
          "activityTitle": "${titleFormated}",
          "facts": [
            ${message}
          ],
          "markdown": true
        }
      ]
    }
  """
  def jsonFinal = templateJson
  return jsonFinal
}