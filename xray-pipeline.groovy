pipeline {
    agent any

    stages {
        
    
        stage('Checking NODE version and NPM') {
            steps {
                sh "node -v"
                sh "npm -v"
            }
        }
        
        stage('Checkout'){
            steps{
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[credentialsId: 'cd4433bd-73ee-4f3c-bb3c-c3f8e10144a8', url: 'https://github.com/singh-anamika/postman-xray.git']])
            }
        }
        
        stage('Build Xray token python script'){
            steps{
                // git branch: 'main', credentialsId: '04bab23d-6247-42cd-a135-155a3e34f5b9', url: 'https://github.com/anshuman0053/postman-newman.git'
                //sh 'python3 postman_upload.py'
                
                script{
                        def token = sh(script: "python3 postman_upload.py", returnStdout: true).trim()
                        env.BEARER_TOKEN = token
                        
                }
                sh "echo $BEARER_TOKEN" 
            }
        }
        
        stage('Running Postman API Collection via Newman'){
            steps{
                script{
                        // Define the command to run the script and pass any required parameters
                        def command = "python3 parallel_newman.py"

                        // Run the script using the sh step and capture the output
                        def output = sh(script: command, returnStdout: true).trim()

                        echo output
                        
                }
                  
            }
        }
        
        stage('Test Pipeline'){
            steps{
                echo "Pipeline Tested Successuly"
            }
        }
    }

    post{
        always{

            sh 'curl -H "Content-Type: text/xml" -X POST -H "Authorization: $BEARER_TOKEN" --data @report.xml https://xray.cloud.getxray.app/api/v2/import/execution/junit?projectKey=CQ'
        }
    }
}

