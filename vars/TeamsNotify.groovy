def call(String message) {
    sh "curl -X POST -H 'Content-Type: application/json' -d '{\"text\": \"${message}\"}' ${env.TEAMS_WEBHOOK_URL}"
}
