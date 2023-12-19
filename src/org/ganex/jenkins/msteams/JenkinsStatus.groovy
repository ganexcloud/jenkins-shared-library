package org.ganex.jenkins.msteams


boolean isBackToNormal() {
  def current = currentBuild.currentResult
  def previous = currentBuild.previousBuild?.currentResult

  return current == 'SUCCESS' && (previous == 'FAILURE' || previous == 'UNSTABLE' || previous == 'ABORTED')
}


boolean stillFailing() {
  def current = currentBuild.currentResult
  def previous = currentBuild.previousBuild?.currentResult

  return current == 'FAILURE' && (previous == 'UNSTABLE' || previous == 'ABORTED')
}


boolean hasFailed() {
  return currentBuild.currentResult == 'FAILURE'
}

boolean isUnstable() {
  return currentBuild.currentResult == 'UNSTABLE'
}

boolean isAborted() {
  return currentBuild.currentResult == 'ABORTED'
}

boolean hasBeenSuccessful() {
  return currentBuild.currentResult == 'SUCCESS'
}

String getStatusMessage() {
  if (isBackToNormal()) {
    return 'Back to normal'
  }

  if (stillFailing()) {
    return 'Still failing'
  }

  if (hasFailed()) {
    return 'Failure'
  }

  if (hasBeenSuccessful()) {
    return 'Success'
  }

  if (isUnstable()) {
    return 'Unstable'
  }

  if (isAborted()) {
    return 'Aborted'
  }

  return ''
}

String getStatusColor() {
  def result = currentBuild.currentResult
  def colors = new Color()

  if (result == 'SUCCESS') {
    return colors.green()
  }

  if (result == 'FAILURE') {
    return colors.red()
  }

  if (result == 'ABORTED') {
    return colors.gray()
  }

  return colors.yellow()
}