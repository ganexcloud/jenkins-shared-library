package org.ganex.jenkins.msteams


void send(String title, String message, String result, String url) {
  def options = getOptions title, message, result, url
  msTeamsSend options
}

def getOptions(String title = '', String message = '', String result = '', String url = '') {
  def obj = [
    description: message,
    title: title,
    link: url,
    result: result
  ]

  return obj
}