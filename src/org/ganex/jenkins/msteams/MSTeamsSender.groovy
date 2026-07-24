package org.ganex.jenkins.msteams


void send(String title, String message, String result, String url) {
  def options = getOptions title, message, result, url
  msTeamsSend options
}

void sendWebhook(def steps, String message) {
  def payloadFile = '.teams-notification-payload.json'
  steps.writeFile file: payloadFile, text: message

  try {
    def httpCode = steps.sh(
      returnStdout: true,
      script: """#!/bin/sh
set +x
curl --silent --show-error --output /dev/null --write-out '%{http_code}' \\
  --request POST --header 'Content-Type: application/json' \\
  --data-binary @${payloadFile} "\$TEAMS_WEBHOOK_URL" || true
"""
    ).trim()

    if (!(httpCode ==~ /2\d\d/)) {
      steps.echo("Teams notification was not accepted (HTTP ${httpCode ?: 'transport error'})")
    }
  } finally {
    steps.sh returnStatus: true, script: """#!/bin/sh
set +x
rm -f -- ${payloadFile}
"""
  }
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
