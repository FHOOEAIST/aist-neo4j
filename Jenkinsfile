pipeline {
    agent {
      label 'master'
    }

    options {
        gitLabConnection('Gitlab') 
        gitlabBuilds(builds: ['setup', 'build'])
    }

    stages {
        stage('prepare') {
            steps {
                script{
                    try {
                        sh '''
                        containerId=$( head -1 /proc/self/cgroup|cut -d/ -f3 )
                        docker network create neo4jtest
                        docker network connect neo4jtest $containerId
                        docker container run --rm --network neo4jtest --name neo4j-graph-db -d aist.fh-hagenberg.at:18444/repository/docker-util/aist-neo4j-with-apoc
                        '''
                    } catch(ex) {
                        echo 'could not start docker container, trying to reboot it...'
                        sh '''
                        containerId=$( head -1 /proc/self/cgroup|cut -d/ -f3 )
                        docker network disconnect neo4jtest $containerId
                        docker network rm neo4jtest
                        docker container stop neo4j-graph-db
                        docker container run --rm --name neo4j-graph-db -d aist.fh-hagenberg.at:18444/repository/docker-util/aist-neo4j-with-apoc
                        '''
                        currentBuild.result = 'SUCCESS' // If we get this far, override the FAILURE build result from the exception
                    } finally {
                        sh 'sleep 30s'
                    }
                
                }
                
            }
            post {
                failure {
                    updateGitlabCommitStatus name: 'setup', state: 'failed'
                }
                success {
                    updateGitlabCommitStatus name: 'setup', state: 'success'
                }
                unstable {
                    updateGitlabCommitStatus name: 'setup', state: 'failed'
                }
            }
        }
       

        stage('build') {
            steps {
                sh '''
                mvn -Dmaven.javadoc.skip=true -Pjenkins -Psonar-coverage clean install sonar:sonar -Dsonar.scm.disabled=true
                '''
            }
            post {
                always {
                  junit '**/target/surefire-reports/TEST-*.xml' 
                   sh '''
                  containerId=$( head -1 /proc/self/cgroup|cut -d/ -f3 )
                  docker network disconnect neo4jtest $containerId
                    docker container stop neo4j-graph-db
                    docker network rm neo4jtest 
                '''
                }
                failure {
                    updateGitlabCommitStatus name: 'build', state: 'failed'
                }
                success {
                    updateGitlabCommitStatus name: 'build', state: 'success'
                }
                unstable {
                    updateGitlabCommitStatus name: 'build', state: 'failed'
                }
            }
        }
    }
}

