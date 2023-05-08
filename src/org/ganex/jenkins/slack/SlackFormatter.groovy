package org.ganex.jenkins.slack


String format(String title = '', String message = '', String testSummary = '') {
  def helper = new JenkinsHelper()

  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()
  def url = helper.getAbsoluteUrl()

  def result = "${project} (#${buildNumber}) - ${title.trim()} (<${url}|Open>)"
  if(message) result = result + "\n ${message.trim()}"
  if(testSummary) result = result + "\n ${testSummary}"

  return result
}