package org.ganex.jenkins.msteams


String formatTitle(String title = '') {
  def helper = new JenkinsHelper()
  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()
  def result = "${project} (#${buildNumber}) - ${title.trim()}"

  return result
}

String formatMessage(String message = '', String testSummary = '') {
  def helper = new JenkinsHelper()
  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()
  def title = "${project} (#${buildNumber}) - ${title.trim()}"
  def result = ""
  if(message) result = "\n ${message.trim()}"
  if(testSummary) result = "\n ${testSummary}"
  def templateJson = """
    {
      "@type": "MessageCard",
      "@context": "http://schema.org/extensions",
      "themeColor": "0076D7",
      "summary": "${title}",
      "sections": [
        {
          "activityTitle": "${title}",
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