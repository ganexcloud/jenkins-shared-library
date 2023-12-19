package org.ganex.jenkins.msteams


String getBranchName() {
  return env.GIT_BRANCH
}


int getBuildNumber() {
  return currentBuild.number
}


String getAbsoluteUrl() {
  return currentBuild.absoluteUrl
}


String getProjectName() {
  return env.JOB_NAME
}

String getBuildUser() {
  def isStartedByUser = currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause) != null
  if (isStartedByUser) { 
    return currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
  } else { 
    return "SCMTrigger"
  }
}

List<String> getChanges() {
  List<String> messages = []
  for (int i = 0; i < currentBuild.changeSets.size(); i++) {
    def entries = currentBuild.changeSets[i].items
    for (int j = 0; j < entries.length; j++) {
      def entry = entries[j]
      def branch = getBranchName()
      def jsonEntry = """
              {
                "Name": "TextBlock",
                "Value": "Author: ${entry.author}"
              },
              {
                "Name": "TextBlock",
                "Value": "Message: ${entry.msg}"
              },
              {
                "Name": "TextBlock",
                "Value": "Branch: ${branch}"
              },
              {
               "Name": "TextBlock",
               "Value": "Commit: ${entry.commitId}"
              }
      """
      
      messages.add(jsonEntry)


      //messages.add('\t{\n"type": "TextBlock",\n"text": "Author: ${entry.author}"\n"type": "TextBlock", "text": "Message: ${entry.msg}\n"type": "TextBlock", "text": "Branch: ${branch}"\n"type": "TextBlock", "text": "Commit: ${entry.commitId}"')
    }
  }

  return messages
}

String getDuration() {
  return currentBuild.durationString.replace(' and counting', '')
}


String getCurrentStatus() {
  return currentBuild.currentResult
}


String getPreviousStatus() {
  def prev = currentBuild.previousBuild?.currentResult

  if (!prev) {
    return 'SUCCESS'
  }

  return prev
}
