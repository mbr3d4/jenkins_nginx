pipeline {
    agent any
    
    environment {
        DEPLOYMENT_NAME = 'nginx'
        IMAGE_NAME = 'nginx:stable-perl'
        GIT_REPO_URL = 'https://github.com/mbr3d4/jenkins_nginx.git'
        KUBECONFIG_PATH = 'config'  // Caminho relativo ao repositório clonado
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
                sh 'mkdir -p $HOME/.kube'
                sh "cp ${KUBECONFIG_PATH} $HOME/.kube/config"
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                sh "kubectl apply -f nginx.yaml"
                
            }
        }
        
        stage('Verify Deployment') {
            steps {
                sh "kubectl rollout status deployment/${DEPLOYMENT_NAME}"
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
