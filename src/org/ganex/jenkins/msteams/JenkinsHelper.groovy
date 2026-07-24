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

List<Map> getChanges() {
  List<Map> items = []
  for (int i = 0; i < currentBuild.changeSets.size(); i++) {
    def entries = currentBuild.changeSets[i].items
    for (int j = 0; j < entries.length; j++) {
      def entry = entries[j]
      def branch = getBranchName()
      items.addAll([
        [type: 'TextBlock', text: "**Author:** ${entry.author}"],
        [type: 'TextBlock', text: "**Message:** ${entry.msg}", spacing: 'none', wrap: true],
        [type: 'TextBlock', text: "**Branch:** ${branch}", spacing: 'none', wrap: true],
        [type: 'TextBlock', text: "**ID:** ${entry.commitId}", spacing: 'none', wrap: true]
      ])
    }
  }

  return items
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
