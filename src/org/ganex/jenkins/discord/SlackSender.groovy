package org.ganex.jenkins.discord


void send(String message, String color = '#fff') {
  def options = getOptions message, color

  discordSend options
}


def getOptions(String message = '', String color = '') {
  def obj = [
    message: message
  ]

  if (color) {
    obj.color = color
  }

  if (env.DISCORD_WEBHOOK_URL) {
    obj.webhookURL = env.discord_WEBHOOK_URL
  }

  return obj
}