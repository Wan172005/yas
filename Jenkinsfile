pipeline {
  agent any

  environment {
    DOCKERHUB_REPO = "quangnguyenvuminh/yas"

    DOCKERHUB_CREDENTIALS_ID = "dockerhub-creds"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'git rev-parse --short=12 HEAD'
      }
    }

    stage('Build image') {
      steps {
        script {
          def commit = sh(script: "git rev-parse --short=12 HEAD", returnStdout: true).trim()
          env.IMAGE_TAG = commit
        }
        sh """
          docker build -t ${DOCKERHUB_REPO}:${IMAGE_TAG} .
        """
      }
    }

    stage('Login & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: "${DOCKERHUB_CREDENTIALS_ID}", usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
          sh """
            echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
            docker push ${DOCKERHUB_REPO}:${IMAGE_TAG}
          """
        }
      }
    }
  }

  post {
    always {
      sh 'docker logout || true'
    }
  }
}
