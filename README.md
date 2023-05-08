## Jenkins Shared Library

### What is a Shared Library

A [Jenkins shared library](https://www.jenkins.io/doc/book/pipeline/shared-libraries/) is a collection of independent Groovy scripts which you pull into your Jenkinsfile at runtime.

The best part is, the Shared Library can be stored in a Git repository. It can be versioned, tagged, etc.

### Prerequisites

[Jenkins 2.277+](https://hub.docker.com/r/jenkins/jenkins/tags/?page=1&ordering=last_updated)

Pipeline Shared Libraries plugin

Other plugins may be required for specific library calls (i.e. AWS, Docker, Slack)

### Getting started with Shared Library

This library consists of `groovy` and `shell` scripts.

### Example usage:

#### Use library in a pipeline

To use shared library in a pipeline, you add `@Library('your-library-name')` to the top of your pipeline definition, or Jenkinsfile

```
@Library('pipeline-library-demo')_

stage('Demo') {
 echo 'Hello world'
 sayHello 'Dave'
}
```

**NOTE:** The underscore (\_) is not a typo! You need this underscore if the line immediately after the @Library annotation is not an import statement.

#### Stage to send notification using Slack of build start:

```
stage('Start') {
  steps {
    new SlackNotifier().notifyStart()
  }
```

#### Stage to send notification to Squadcast of build result:

```
stage('Start') {
  steps {
    squadcastNotifier()
  }
```

### Note:

:information_source: There is a plan to split `Build` and `Deploy` and use **Jenkins** for building and **Spinnaker** for deploying.

#### References:

- https://github.com/jenkinsci/jenkins
- https://www.jenkins.io/doc/book/pipeline/shared-libraries/
