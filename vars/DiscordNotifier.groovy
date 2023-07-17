package org.ganex.jenkins.discord

void notifyStart() {
  JenkinsHelper helper = new JenkinsHelper()
  DiscordFormatter formatter = new DiscordFormatter()
  DiscordSender sender = new DiscordSender()
  JenkinsStatus status = new JenkinsStatus()

  def user = helper.getBuildUser()
  def title = formatter.formatTitle "Build started by ${user}..."
  def message = formatter.formatMessage "Build started by ${user}..."
  def result = "UNSTABLE"
  def url = helper.getAbsoluteUrl()

  sender.send title, message, result, url
}


void notifyError(Throwable err) {
  def formatter = new DiscordFormatter()
  def sender = new DiscordSender()
  def result = currentBuild.currentResult

  def title = formatter.formatTitle "An error occurred :interrobang:"
  def message = formatter.formatMessage "An error occurred :interrobang:", err.message
  def url = helper.getAbsoluteUrl()

  sender.send title, message, result, url
}

boolean shouldNotNotifySuccess(statusMessage) {
  Config config = new Config()
  return statusMessage == 'Success' && !config.getNotifySuccess()
}

void notifyResult() {
  JenkinsHelper helper = new JenkinsHelper()
  JenkinsStatus status = new JenkinsStatus()
  DiscordFormatter formatter = new DiscordFormatter()
  DiscordSender sender = new DiscordSender()
  Config config = new Config()

  def statusMessage = status.getStatusMessage()

  if(shouldNotNotifySuccess(statusMessage)) {
    println("DiscordNotifier - No notification will be send for SUCCESS result")
    return
  }

  def result = helper.getCurrentStatus()
  def duration = helper.getDuration()

  String changes = null
  if(config.getChangeList()) changes = helper.getChanges().join '\n'

  String testSummary = null
  if (config.getTestSummary()) {
    JenkinsTestsSummary jenkinsTestsSummary = new JenkinsTestsSummary()
    testSummary = jenkinsTestsSummary.getTestSummary()
  }

  def title = formatter.formatTitle "${statusMessage} after ${duration}"
  def message = formatter.formatMessage "${statusMessage} after ${duration}", changes, testSummary
  def url = helper.getAbsoluteUrl()

  sender.send title, message, result, url
}

void notifyResultFull() {
  env.TEST_SUMMARY = true
  env.CHANGE_LIST = true
  env.NOTIFY_SUCCESS = true
  notifyResult()
}