pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'devhong/plant-house'
        DOCKER_TAG = "${BUILD_NUMBER}"
        APP_EC2_IP = '43.202.63.50'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build -x test'
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh "docker login -u $DOCKER_USER -p $DOCKER_PASS"
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    sh "docker build -t ${DOCKER_IMAGE}:latest ."
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    sh "docker push ${DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('Deploy') {
            steps {
                sshagent(['app-ec2-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ec2-user@${APP_EC2_IP} '
                            docker pull ${DOCKER_IMAGE}:latest
                            docker stop my-app || true
                            docker rm my-app || true
                            docker run -d --name my-app --env-file ~/.env.docker -p 8080:8080 ${DOCKER_IMAGE}:latest
                        '
                    """
                }
            }
        }
    }
}