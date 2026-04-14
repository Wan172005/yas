pipeline {
  agent any
  options { timestamps() }

  environment {
    DOCKERHUB_NAMESPACE      = "quangnguyenvuminh"
    DOCKERHUB_CREDENTIALS_ID = "dockerhub-creds"

    MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'

    SERVICES_TO_BUILD = "customer,cart,order,product,tax,media,search,rating,location,inventory"

    JAVA_HOME = tool 'JDK25'
    PATH = "${JAVA_HOME}/bin:${env.PATH}"
  }

  tools { maven 'Maven3' }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        script {
          env.GIT_SHA = sh(script: "git rev-parse --short=12 HEAD", returnStdout: true).trim()
          echo "GIT_SHA=${env.GIT_SHA}"
        }
      }
    }

    stage('Detect changed services vs main') {
      steps {
        script {
          def allServices = (env.SERVICES_TO_BUILD.split(',') as List)
            .collect { it.trim() }
            .findAll { it }

          def changedFilesRaw = sh(
            script: "git diff --name-only origin/main...HEAD",
            returnStdout: true
          ).trim()

          def changedFiles = changedFilesRaw ? (changedFilesRaw.split('\n') as List) : []
          echo "Changed files (${changedFiles.size()}):\n${changedFiles.join('\n')}"

          def changedServices = [] as Set
          changedFiles.each { f ->
            allServices.each { svc ->
              if (f.startsWith("${svc}/")) changedServices << svc
            }
          }

          def finalServices = (changedServices as List).sort()
          env.SELECTED_SERVICES = finalServices.join(',')

          if (!env.SELECTED_SERVICES?.trim()) {
            currentBuild.description = "No service folder changes vs main"
            echo "No changed service folders detected. Skipping build/push."
          } else {
            currentBuild.description = "Services: ${env.SELECTED_SERVICES} | Tag: ${env.GIT_SHA}"
            echo "Selected services: ${env.SELECTED_SERVICES}"
          }
        }
      }
    }

    stage('Build selected services') {
      when { expression { return env.SELECTED_SERVICES?.trim() } }
      steps {
        script {
          def pl = env.SELECTED_SERVICES.split(',') as List
          sh """
            set -euxo pipefail
            java -version
            javac -version
            mvn -B clean install -pl ${pl.join(',')} -am -DskipTests
          """
        }
      }
    }

    stage('Build Docker images (by commit tag)') {
      when { expression { return env.SELECTED_SERVICES?.trim() } }
      steps {
        script {
          def services = env.SELECTED_SERVICES.split(',') as List
          services.each { svc ->
            dir(svc) {
              sh """
                set -euxo pipefail
                echo "=== Docker build: ${svc}:${GIT_SHA} ==="
                ls -la target || true
                docker build -t ${DOCKERHUB_NAMESPACE}/${svc}:${GIT_SHA} .
              """
            }
          }
        }
      }
    }

    stage('Login & Push (by commit tag)') {
      when { expression { return env.SELECTED_SERVICES?.trim() } }
      steps {
        withCredentials([usernamePassword(credentialsId: "${DOCKERHUB_CREDENTIALS_ID}", usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
          sh """
            set -euxo pipefail
            echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
          """
        }

        script {
          def services = env.SELECTED_SERVICES.split(',') as List
          services.each { svc ->
            sh "set -euxo pipefail; docker push ${DOCKERHUB_NAMESPACE}/${svc}:${GIT_SHA}"
          }
        }
      }
    }
  }

  post {
    always { sh 'docker logout || true' }
  }
}
