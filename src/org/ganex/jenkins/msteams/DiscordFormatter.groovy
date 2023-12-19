package org.ganex.jenkins.discord


String formatTitle(String title = '') {
  def helper = new JenkinsHelper()
  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()
  def result = "${project} (#${buildNumber}) - ${title.trim()}"

  return result
}

String formatMessage(String message = '', String testSummary = '') {
  def helper = new JenkinsHelper()
  def project = helper.getProjectName()
  def branch = helper.getBranchName()
  def buildNumber = helper.getBuildNumber()
  def result = ""
  if(message) result = "\n ${message.trim()}"
  if(testSummary) result = "\n ${testSummary}"
  def criarObjetoJson() {
      def meuJson = [
        {
          "type": "message",
          "attachments": [
            {
              "contentType": "application/vnd.microsoft.card.adaptive",
              "contentUrl": null,
              "content": {
                "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
                "type": "AdaptiveCard",
                "version": "1.2",
                "body": [
                  {
                    "type": "TextBlock",
                    "text": "Author: caio"
                  },
                  {
                    "type": "TextBlock",
                    "text": "Message: asd"
                  },
                  {
                    "type": "TextBlock",
                    "text": "Branch: staging"
                  },
                  {
                    "type": "TextBlock",
                    "text": "Commit: 8cc55630b838b6356091e48f8da362a41ff95518"
                  }
                ]
              }
            }
          ]
        }
      ]
      return meuJson
  }

  def meuJson = criarObjetoJson()
  return meuJson
  //return result
}