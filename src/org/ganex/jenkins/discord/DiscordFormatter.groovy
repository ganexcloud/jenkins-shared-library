package org.ganex.jenkins.discord


String formatTitle(String title = '') {
  def helper = new JenkinsHelper()
  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()

  def result = "${project} (#${buildNumber}) - ${title.trim()}"

  return result
}

String formatMessage(String title = '', String message = '', String testSummary = '') {
  def helper = new JenkinsHelper()

  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()
  def url = helper.getAbsoluteUrl()

  def result = "${project} (#${buildNumber}) - ${title.trim()}"
  if(message) result = "\n ${message.trim()}"
  if(testSummary) result = "\n ${testSummary}"

  return result
}