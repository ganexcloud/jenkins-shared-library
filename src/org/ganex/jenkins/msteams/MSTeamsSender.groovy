package org.ganex.jenkins.msteams


void send(String title, String message, String result, String url) {
  def options = getOptions title, message, result, url
  MSTeamsSend options
}

def getOptions(String title = '', String message = '', String result = '', String url = '') {
  def obj = [
    description: message,
    title: title,
    link: url,
    result: result
  ]

  if (env.DISCORD_WEBHOOK_URL) {
    obj.webhookURL = env.discord_WEBHOOK_URL
  }

  return obj
}