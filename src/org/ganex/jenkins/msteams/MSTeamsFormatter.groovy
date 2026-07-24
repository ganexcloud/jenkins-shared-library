package org.ganex.jenkins.msteams

import groovy.json.JsonOutput

String formatMessageSimple(String title = '') {
  return formatCard(title, 'default', [])
}

String formatMessage(String title = '', String message = '', String testSummary = '') {
  def resultItems = []
  if (message?.trim()) {
    resultItems << [type: 'TextBlock', text: message.trim(), wrap: true]
  }

  if (testSummary?.trim()) {
    resultItems << [type: 'TextBlock', text: testSummary.trim(), wrap: true]
  }

  return formatCard(title, new JenkinsStatus().getStatusColor(), resultItems)
}

String formatMessage(String title, List<Map> resultItems, String testSummary = '') {
  def items = resultItems ?: []

  if (testSummary?.trim()) {
    items << [type: 'TextBlock', text: testSummary.trim(), wrap: true]
  }

  return formatCard(title, new JenkinsStatus().getStatusColor(), items)
}

private String formatCard(String title, String color, List<Map> resultItems) {
  def helper = new JenkinsHelper()
  def titleFormated = "${helper.getProjectName()} (#${helper.getBuildNumber()}) - ${title.trim()}"
  def items = [[
    type: 'TextBlock',
    text: titleFormated,
    weight: 'Bolder',
    size: 'Large'
  ]]
  items.addAll(resultItems)
  items << [
    type: 'ActionSet',
    actions: [[
      type: 'Action.OpenUrl',
      title: 'View Job',
      url: helper.getAbsoluteUrl()
    ]]
  ]

  return JsonOutput.toJson([
    type: 'message',
    attachments: [[
      contentType: 'application/vnd.microsoft.card.adaptive',
      content: [
        type: 'AdaptiveCard',
        version: '1.4',
        '$schema': 'http://adaptivecards.io/schemas/adaptive-card.json',
        msteams: [width: 'Full'],
        body: [[
          type: 'Container',
          style: color,
          items: items
        ]]
      ]
    ]]
  ])
}
