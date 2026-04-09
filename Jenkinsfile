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

    // Build jar trước để Dockerfile COPY target/*.jar không bị fail
    MAVEN_CMD = "mvn -B -DskipTests clean package"
  }
  
  tools {
        maven 'Maven3'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'git rev-parse --short=12 HEAD'
      }
    }

    stage('Build Services') {
      steps {
        script {
          def svcs = env.SERVICES_TO_BUILD.split(',') as List
          def pl = svcs.join(',')

          // -pl: chỉ build các service modules bạn chọn
          // -am: tự động build các module phụ thuộc cần thiết (vd common-library)
          sh """
            set -euxo pipefail
            java -version
            javac -version
            mvn -B clean install -pl ${pl} -am -DskipTests
          """
        }
      }
    }

    stage('Build Docker images (latest)') {
      steps {
        script {
          def services = ['customer','cart','order','product','tax','media','search','rating','location','inventory']
          services.each { svc ->
            dir(svc) {
              sh """
                set -euxo pipefail
                echo "=== Docker build: ${svc}:latest ==="
                ls -la target || true
                docker build -t ${DOCKERHUB_NAMESPACE}/${svc}:latest .
              """
            }
          }
        }
      }
    }

    stage('Login & Push (latest)') {
      steps {
        withCredentials([usernamePassword(credentialsId: "${DOCKERHUB_CREDENTIALS_ID}", usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
          sh """
            set -euxo pipefail
            echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
          """
        }

        script {
          def services = ['customer','cart','order','product','tax','media','search','rating','location','inventory']
          services.each { svc ->
            sh "set -euxo pipefail; docker push ${DOCKERHUB_NAMESPACE}/${svc}:latest"
          }
        }
      }
    }
  }

  post {
    always { sh 'docker logout || true' }
  }
}
