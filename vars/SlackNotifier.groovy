package org.ganex.jenkins.slack


void notifyStart() {
  JenkinsHelper helper = new JenkinsHelper()
  SlackFormatter formatter = new SlackFormatter()
  SlackSender sender = new SlackSender()
  JenkinsStatus status = new JenkinsStatus()

  def user = helper.getBuildUser()
  def message = formatter.format "Build started by ${user}..."
  def color = new Color().blue()

  sender.send message, color
}


void notifyError(Throwable err) {
  def formatter = new SlackFormatter()
  def sender = new SlackSender()
  def color = new Color().red()

  def message = formatter.format "An error occurred :interrobang:", err.message
  sender.send message, color
}

boolean shouldNotNotifySuccess(statusMessage) {
  Config config = new Config()
  return statusMessage == 'Success' && !config.getNotifySuccess()
}

void notifyResult() {
  JenkinsHelper helper = new JenkinsHelper()
  JenkinsStatus status = new JenkinsStatus()
  SlackFormatter formatter = new SlackFormatter()
  SlackSender sender = new SlackSender()
  Config config = new Config()

  def statusMessage = status.getStatusMessage()

  if(shouldNotNotifySuccess(statusMessage)) {
    println("SlackNotifier - No notification will be send for SUCCESS result")
    return
  }

  def color = status.getStatusColor()
  def duration = helper.getDuration()

  String changes = null
  if(config.getChangeList()) changes = helper.getChanges().join '\n'

  String testSummary = null
  if (config.getTestSummary()) {
    JenkinsTestsSummary jenkinsTestsSummary = new JenkinsTestsSummary()
    testSummary = jenkinsTestsSummary.getTestSummary()
  }

  def message = formatter.format "${statusMessage} after ${duration}", changes, testSummary

  sender.send message, color
}

void notifyResultFull() {
  env.TEST_SUMMARY = true
  env.CHANGE_LIST = true
  env.NOTIFY_SUCCESS = true
  notifyResult()
}