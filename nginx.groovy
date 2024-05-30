pipeline {
    agent any
    
    environment {
        DEPLOYMENT_NAME = 'your-deployment-name'
        IMAGE_NAME = 'your/image:tag'
        GIT_REPO_URL = 'https://github.com/yourusername/your-repo.git'
        GIT_CREDENTIAL_ID = 'your-git-credential-id'
        KUBECONFIG_PATH = './kubeconfig-folder/kubeconfig'
    }
    
    stages {
        stage('Clone Git Repository') {
            steps {
                checkout([$class: 'GitSCM', 
                          branches: [[name: '*/master']], 
                          doGenerateSubmoduleConfigurations: false, 
                          extensions: [], 
                          submoduleCfg: [], 
                          userRemoteConfigs: [[credentialsId: "${GIT_CREDENTIAL_ID}", url: "${GIT_REPO_URL}"]]])
            }
        }
        
        stage('Set Kubeconfig') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'your-kubeconfig-credential-id', variable: 'KUBECONFIG')]) {
                        sh "cp $KUBECONFIG_PATH $KUBECONFIG"
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
                    sh "kubectl --kubeconfig=$KUBECONFIG set image deployment/$DEPLOYMENT_NAME $DEPLOYMENT_NAME=$IMAGE_NAME"
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    // Adicione etapas de verificação, se necessário
                    sh "kubectl --kubeconfig=$KUBECONFIG rollout status deployment/$DEPLOYMENT_NAME"
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
