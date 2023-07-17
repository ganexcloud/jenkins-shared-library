package org.ganex.jenkins.discord


void send(String title, String message, String result) {
  def options = getOptions message, result
  discordSend options
}


def getOptions(String title, String message = '', String result = '') {
  def obj = [
    title: title,
    description: message
  ]

  
  //if (result) {
  //  obj.result = currentBuild.currentResult
  //}

  if (result) {
    obj.result = result
  }

  if (env.DISCORD_WEBHOOK_URL) {
    obj.webhookURL = env.discord_WEBHOOK_URL
  }

  return obj
}