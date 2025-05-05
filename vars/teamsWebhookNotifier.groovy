#!/usr/bin/env groovy

/**
 * Send a notification to Microsoft Teams using webhook
 * @param action The action to perform (e.g., "notify")
 */
def call(String action = "notify") {
  // Get the current build result
  def buildResult = currentBuild.currentResult
  
  // Load the Python script from resources
  final file = libraryResource('jenkins-teams-notifications.py')
  writeFile(file: 'jenkins-teams-notifications.py', text: file)
  
  // Use credentials securely without hardcoding
  withCredentials([
    string(credentialsId: "${TEAMS_URL}", variable: 'TEAMS_URL'),
    string(credentialsId: "${TEAMS_TOKEN}", variable: 'TEAMS_TOKEN')
  ]) {
    // Set the build result environment variable for the Python script
    env.BUILD_RESULT = buildResult
    
    // Execute the Python script
    sh "python3 jenkins-teams-notifications.py --url ${TEAMS_URL} --token ${TEAMS_TOKEN}"
  }
}