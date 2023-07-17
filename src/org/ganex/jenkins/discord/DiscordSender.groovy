package org.ganex.jenkins.discord


void send(String title, String message, String result, String url) {
  def options = getOptions message, result
  discordSend options
}


def getOptions(String title = '', String message = '', String result = '', String url = '') {
  def obj = [
    description: message
  ]

  title = title
  link = url

  if (result) {
    obj.result = result
  }

  if (env.DISCORD_WEBHOOK_URL) {
    obj.webhookURL = env.discord_WEBHOOK_URL
  }

  return obj
}