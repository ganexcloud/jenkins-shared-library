package org.ganex.jenkins.msteams

String formatMessageSimple(String title = '') {
  def helper = new JenkinsHelper()
  def status = new JenkinsStatus()
  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()
  def titleFormated = "${project} (#${buildNumber}) - ${title.trim()}"
  def color = status.getStatusColor()
  def url = helper.getAbsoluteUrl()
  def templateJson = """
{
  "type": "message",
  "attachments": [
    {
      "contentType": "application/vnd.microsoft.card.adaptive",
      "content": {
        "type": "AdaptiveCard",
        "version": "1.4",
        "\$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "msteams": { "width": "Full" },
        "body": [
          {
            "type": "Container",
            "style": "default",
            "items": [
              {
                "type": "TextBlock",
                "text": "${titleFormated}",
                "weight": "Bolder",
                "size": "Large"
              },
              {
                "type": "ActionSet",
                "actions": [
                  {
                    "type": "Action.OpenUrl",
                    "title": "View Job",
                    "url": "${url}"
                  }
                ]
              }
            ]
          }
        ]
      }
    }
  ]
}
"""
  return templateJson
}

String formatMessage(String title = '', String message = '', String testSummary = '') {
  def helper = new JenkinsHelper()
  def status = new JenkinsStatus()
  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()
  def titleFormated = "${project} (#${buildNumber}) - ${title.trim()}"
  def color = status.getStatusColor()
  def url = helper.getAbsoluteUrl()
  def resultItems = ""

  if (message?.trim()) {
    resultItems += """,
              {
                "type": "TextBlock",
                "text": "${message.trim()}",
                "wrap": true
              }"""
  }

  if (testSummary?.trim()) {
    resultItems += """,
              {
                "type": "TextBlock",
                "text": "${testSummary.trim()}",
                "wrap": true
              }"""
  }

  def templateJson = """
{
  "type": "message",
  "attachments": [
    {
      "contentType": "application/vnd.microsoft.card.adaptive",
      "content": {
        "type": "AdaptiveCard",
        "version": "1.4",
        "\$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "msteams": { "width": "Full" },
        "body": [
          {
            "type": "Container",
            "style": "${color}",
            "items": [
              {
                "type": "TextBlock",
                "text": "${titleFormated}",
                "weight": "Bolder",
                "size": "Large"
              }${resultItems},
              {
                "type": "ActionSet",
                "actions": [
                  {
                    "type": "Action.OpenUrl",
                    "title": "View Job",
                    "url": "${url}"
                  }
                ]
              }
            ]
          }
        ]
      }
    }
  ]
}
"""
  return templateJson
}
