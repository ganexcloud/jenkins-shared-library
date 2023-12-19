package org.ganex.jenkins.discord


void send(String title, String message, String result, String url) {
  def options = getOptions title, message, result, url
  discordSend options
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