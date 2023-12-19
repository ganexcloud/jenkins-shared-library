#!/usr/bin/env groovy

package org.ganex.jenkins.msteams

void notifyStart() {
  JenkinsHelper helper = new JenkinsHelper()
  DiscordFormatter formatter = new DiscordFormatter()
  DiscordSender sender = new DiscordSender()
  JenkinsStatus status = new JenkinsStatus()

  def user = helper.getBuildUser()
  def title = formatter.formatTitle "Build started by ${user}..."
  def message = null
  def result = "UNSTABLE"
  def url = helper.getAbsoluteUrl()

  sender.send title, message, result, url
}


void notifyError(Throwable err) {
  def formatter = new DiscordFormatter()
  def sender = new DiscordSender()
  def result = currentBuild.currentResult

  def title = formatter.formatTitle "An error occurred :interrobang:"
  def message = formatter.formatMessage err.message
  def url = helper.getAbsoluteUrl()

  sender.send title, message, result, url
}

boolean shouldNotNotifySuccess(statusMessage) {
  Config config = new Config()
  return statusMessage == 'Success' && !config.getNotifySuccess()
}

void notifyResult() {
  JenkinsHelper helper = new JenkinsHelper()
  JenkinsStatus status = new JenkinsStatus()
  DiscordFormatter formatter = new DiscordFormatter()
  DiscordSender sender = new DiscordSender()
  Config config = new Config()

  def statusMessage = status.getStatusMessage()

  if(shouldNotNotifySuccess(statusMessage)) {
    println("DiscordNotifier - No notification will be send for SUCCESS result")
    return
  }

  def result = helper.getCurrentStatus()
  def duration = helper.getDuration()

  String changes = null
  if(config.getChangeList()) changes = helper.getChanges().join '\n'

  String testSummary = null
  if (config.getTestSummary()) {
    JenkinsTestsSummary jenkinsTestsSummary = new JenkinsTestsSummary()
    testSummary = jenkinsTestsSummary.getTestSummary()
  }

  def title = formatter.formatTitle "${statusMessage} after ${duration}"
  def message = formatter.formatMessage changes, testSummary
  def url = helper.getAbsoluteUrl()
  def url2 = "${env.TEAMS_WEBHOOK_URL}"
  
  println "message: ${message}"
  def comandoCurl = "curl -vvvv -X POST -H 'Content-Type: application/json' -d '{\"chave\": \"asdasd\"}' ${url2}"
  println "Comando curl: ${comandoCurl}"
  // Execute o comando
  //def resultadoComando = comandoCurl.execute()
  def processo = comandoCurl.execute()
  def resultadoComando = processo.text
  def statusCurl = processo.waitFor()
  println processo.err.text
  println processo.text
  if (statusCurl == 0) {
      println "Comando curl concluído com sucesso. Resultado: ${resultadoComando}"
  
      // Prossiga com o restante do seu script aqui
  } else {
      println "Erro ao executar o comando curl. Status de saída: ${statusCurl}"
  }
  println "Resultado do Comando: ${resultadoComando}"


  //sender.send title, message, result, url
  //sh "curl -X POST -H 'Content-Type: application/json' -d '{\"text\": \"${message}\"}' ${env.TEAMS_WEBHOOK_URL}"
}

void notifyResultFull() {
  env.TEST_SUMMARY = true
  env.CHANGE_LIST = true
  env.NOTIFY_SUCCESS = true
  notifyResult()
}