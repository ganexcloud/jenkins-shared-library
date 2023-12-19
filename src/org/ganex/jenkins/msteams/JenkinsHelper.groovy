package org.ganex.jenkins.discord


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
      messages.add("\tAuthor: ${entry.author}\nMessage: ${entry.msg}\nBranch: ${branch}\nCommit: ${entry.commitId}")
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
