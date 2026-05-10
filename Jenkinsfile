pipeline {
  agent any
  options {
    timestamps()
    skipDefaultCheckout(true)   // QUAN TRỌNG: tắt "Declarative: Checkout SCM"
  }

  environment {
    DOCKERHUB_NAMESPACE      = "quangnguyenvuminh"
    DOCKERHUB_CREDENTIALS_ID = "dockerhub-creds"
    MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    SERVICES_TO_BUILD = "customer,cart,order,product,tax,media,search,rating,location,inventory,backoffice-bff"
    JAVA_HOME = tool 'JDK25'
    PATH = "${JAVA_HOME}/bin:${env.PATH}"
  }

  tools { maven 'Maven3' }

  stages {
    stage('Checkout (clean)') {
      steps {
        deleteDir()          // xoá workspace để tránh .git hỏng
        checkout scm

        sh 'git status'
        sh 'git rev-parse --is-inside-work-tree'
        sh 'git fetch --no-tags --prune origin +refs/heads/main:refs/remotes/origin/main'

        script {
          env.GIT_SHA = sh(script: "git rev-parse --short=12 HEAD", returnStdout: true).trim()
          echo "GIT_SHA=${env.GIT_SHA}"
        }
      }
    }

    stage('Detect changed services vs main') {
      steps {
        script {
          def allServices = (env.SERVICES_TO_BUILD.split(',') as List).collect { it.trim() }.findAll { it }

          def changedFilesRaw = sh(script: "git diff --name-only origin/main...HEAD", returnStdout: true).trim()
          def changedFiles = changedFilesRaw ? (changedFilesRaw.split('\n') as List) : []

          def changedServices = [] as Set
          changedFiles.each { f ->
            allServices.each { svc ->
              if (f.startsWith("${svc}/")) changedServices << svc
            }
          }

          env.SELECTED_SERVICES = ((changedServices as List).sort()).join(',')
          echo "SELECTED_SERVICES=${env.SELECTED_SERVICES ?: '(none)'}"
        }
      }
    }

    stage('Build selected services') {
      when { expression { return env.SELECTED_SERVICES?.trim() } }
      steps {
        script {
          def pl = env.SELECTED_SERVICES.split(',') as List
          sh "set -euxo pipefail; mvn -B clean install -pl ${pl.join(',')} -am -DskipTests"
        }
      }
    }

    stage('Build & Push Docker images (commit tag)') {
      when { expression { return env.SELECTED_SERVICES?.trim() } }
      steps {
        withCredentials([usernamePassword(credentialsId: "${DOCKERHUB_CREDENTIALS_ID}", usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
          sh 'set -euxo pipefail; echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin'
        }

        script {
          def services = env.SELECTED_SERVICES.split(',') as List
          services.each { svc ->
            dir(svc) {
              sh """
                set -euxo pipefail
                docker build -t ${DOCKERHUB_NAMESPACE}/${svc}:${GIT_SHA} .
                docker push ${DOCKERHUB_NAMESPACE}/${svc}:${GIT_SHA}
              """
            }
          }
        }
      }
    }
  }

  post {
    always {
      // tránh fail post khi checkout fail: chỉ logout nếu có docker
      sh 'docker logout || true'
      cleanWs()
    }
  }
}
