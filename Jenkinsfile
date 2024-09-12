pipeline {
    agent any

    environment {
        BACKEND_IMAGE = 'backend'
        FRONTEND_BUILD_DIR = 'frontend/dist'  // Assuming your build directory is 'dist' after npm build
    }

    stages {
        // stage('Git Checkout') {
        //     steps {
        //         // GitLab에서 코드를 체크아웃
        //         git branch: 'develop',
        //         credentialsId: 'junsk50',
        //         url: 'https://lab.ssafy.com/s11-fintech-finance-sub1/S11P21A206.git'
        //     }
        // }

        stage('Backend Build') {
            steps {
                script {
                    echo '********** Backend Build Start **********'
                    dir('omg-back') {
                        sh 'docker build -t docker-image/$BACKEND_IMAGE .'
                    }
                    
                    echo '********** Backend Build End **********'
                }
            }
        }

        // stage('Frontend Build') {
        //     steps {
        //         script {
        //             echo '********** Frontend Build Start **********'
        //             dir('frontend') {
        //                 sh 'npm install'  // Install dependencies
        //                 sh 'npm run build'  // Build static files
        //             }

        //             echo '********** Frontend Build End **********'
        //         }
        //     }
        // }

        // stage('Deploy Frontend Static Files') {
        //     steps {
        //         script {
        //             echo '********** Deploy Frontend Static Files Start **********'
        //             // Copy built frontend files to /var/www/html for Nginx
        //             sh 'sudo cp -r frontend/dist/* /var/www/html/'
        //             echo '********** Deploy Frontend Static Files End **********'
        //         }
        //     }
        // }

        // stage('Docker Compose Up') {
        //     steps {
        //         script {
        //             echo '******** Docker Compose Start ************'
        //             sh 'docker compose down'
        //             sh 'docker compose up -d'
                    
        //             echo '********** Docker Compose End ***********'
        //         }
        //     }
        // }

        stage('Delete unnecessary Docker images') {
            steps {
                script {
                    echo '********** Delete unnecessary Docker images Start **********'
                    
                    sh 'docker image prune -a -f'
                    
                    echo '********** Delete unnecessary Docker images End **********'
                }
            }
        }
    }

    // post {
    //     success {
    //     	script {
    //             def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
    //             def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
    //             mattermostSend (color: 'good', 
    //             message: "빌드 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)", 
    //             endpoint: 'https://meeting.ssafy.com/hooks/ceutz8ibfbgm3mwbji6f7yeehy', 
    //             channel: 'jenkins'
    //             )
    //         }
    //     }
    //     failure {
    //     	script {
    //             def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
    //             def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
    //             mattermostSend (color: 'danger', 
    //             message: "빌드 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)", 
    //             endpoint: 'https://meeting.ssafy.com/hooks/ceutz8ibfbgm3mwbji6f7yeehy', 
    //             channel: 'jenkins'
    //             )
    //         }
    //     }
    // }
}
