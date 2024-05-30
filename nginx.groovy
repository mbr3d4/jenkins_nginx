pipeline {
    agent any
    
    environment {
        DEPLOYMENT_NAME = 'nginx'
        IMAGE_NAME = 'nginx:stable-perl'
        GIT_REPO_URL = 'https://github.com/mbr3d4/jenkins_nginx.git'
        KUBECONFIG_PATH = 'config'
    }
    
    stages {
        stage('Clone Git Repository') {
            steps {
                checkout([$class: 'GitSCM', 
                          branches: [[name: '*/main']], 
                          doGenerateSubmoduleConfigurations: false, 
                          extensions: [], 
                          submoduleCfg: [], 
                          userRemoteConfigs: [[url: "${GIT_REPO_URL}"]]])
            }
        }
        
        stage('Set Kubeconfig') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'your-kubeconfig-credential-id', variable: 'KUBECONFIG')]) {
                        sh "cp ${KUBECONFIG_PATH} \$HOME/.kube/config"
                    }
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    // Aqui você pode adicionar etapas para construir sua imagem Docker, se necessário
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh "kubectl apply -f nginx.yaml --kubeconfig=\$HOME/.kube/config"
                    sh "kubectl set image deployment/${DEPLOYMENT_NAME} ${DEPLOYMENT_NAME}=${IMAGE_NAME} --kubeconfig=\$HOME/.kube/config"
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    sh "kubectl rollout status deployment/${DEPLOYMENT_NAME} --kubeconfig=\$HOME/.kube/config"
                }
            }
        }
    }
    
    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
