pipeline {
    agent any

    tools {
        maven 'Maven 3'   
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/karinasayta1/url-shortener.git',
                credentialsId: 'github-token'
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t urlshortener:latest .'
            }
        }

        stage('Deploy') {
            steps {
                
                sh '''
            # Stop and remove old container if exists
            docker stop urlshortener-app || true
            docker rm urlshortener-app || true

            # Run new container using the freshly built image
            docker run -d \\
                --name urlshortener-app \\
                --network urlshortner_urlshortener-net \\
                -p 8081:8080 \\
                -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/urlshortener?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" \\
                -e SPRING_DATASOURCE_USERNAME=user \\
                -e SPRING_DATASOURCE_PASSWORD=userpassword \\
                -e SPRING_DATA_REDIS_HOST=redis \\
                -e SPRING_DATA_REDIS_PORT=6379 \\
                urlshortener:latest
        '''
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}