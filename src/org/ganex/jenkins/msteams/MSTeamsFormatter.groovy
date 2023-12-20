package org.ganex.jenkins.msteams

String formatMessage(String title = '', String message = '', String testSummary = '') {
  def helper = new JenkinsHelper()
  def status = new JenkinsStatus()
  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()
  def titleFormated = "${project} (#${buildNumber}) - ${title.trim()}"
  def color = status.getStatusColor()
  def url = helper.getAbsoluteUrl()
  def result = ""
  if(message) result = "\n ${message.trim()}"
  if(testSummary) result = "\n ${testSummary}"
  def templateJson = """
    {
      "@type": "MessageCard",
      "@context": "http://schema.org/extensions",
      "themeColor": "${color}",
      "summary": "${titleFormated}",
      "title": "${titleFormated}",
      "text": "**Commits:**",
      "sections": [
        {
          ${message}
        }
      ],
      "potentialAction": [
        {
          "@type": "OpenUri",
          "name": "View Job",
          "targets": [
            {
              "os": "default",
              "uri": "${url}"
            }
          ]
        }
      ]
    }
  """
  def jsonFinal = templateJson
  return jsonFinal
}