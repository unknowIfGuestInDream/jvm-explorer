pipeline {
    agent any
    options {
        timeout(time: 1, unit: "HOURS")
    }
    environment {
        USER_NAME = 'Jenkins'
    }
    tools {
        jdk "jdk21"
    }

    stages {
        stage('Check change') {
            when {
                expression { currentBuild.previousSuccessfulBuild != null }
                expression { currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause) == null }
            }
            steps {
                echo "Current commit: ${GIT_COMMIT}"
                echo "Current URL: ${env.GIT_URL}"
                script {
                    def prevBuild = currentBuild.previousSuccessfulBuild
                    def prevCommitId = ""
                    def actions = prevBuild.rawBuild.getActions(hudson.plugins.git.util.BuildData.class)
                    for (action in actions) {
                        if (action.getRemoteUrls().toString().contains(env.GIT_URL)) {
                            prevCommitId = action.getLastBuiltRevision().getSha1String()
                            break
                        }
                    }
                    if (prevCommitId == "") {
                        echo "prevCommitId does not exist."
                    } else {
                        echo "Previous successful commit: ${prevCommitId}"
                        if (prevCommitId == GIT_COMMIT) {
                            echo "no change, skip build"
                            currentBuild.getRawBuild().getExecutor().interrupt(Result.NOT_BUILT)
                            sleep(1)
                        }
                    }
                }
            }
        }

        stage('Prepare JRE') {
            steps {
                sh 'rm -f *linux*21*.tar.gz *mac*21*.tar.gz *windows*21*.zip || true'
                copyArtifacts filter: '*linux*21*,*mac*21*,*windows*21*', fingerprintArtifacts: true, projectName: 'env/JRE', selector: lastSuccessful()
                sh 'java -version'
                sh "$M2_HOME/bin/mvn -version"
            }
            post {
                failure {
                    echo 'Prepare JRE failed'
                }
                aborted {
                    echo 'Build aborted'
                }
            }
        }

        stage('Install Modules') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    sh "$M2_HOME/bin/mvn -B --no-transfer-progress -s $M2_HOME/conf/settings.xml -Dmaven.test.skip=true -Dmaven.compile.fork=true -Duser.name=${USER_NAME} -pl agent,launch-agent -am clean install"
                }
            }
            post {
                failure {
                    echo 'Install Modules failed'
                }
                aborted {
                    echo 'Build aborted'
                }
            }
        }

        stage('Prepare Windows Build') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    sh "$M2_HOME/bin/mvn -B --no-transfer-progress -s $M2_HOME/conf/settings.xml -Djavafx.platform=win -Dmaven.test.skip=true -Dmaven.compile.fork=true -Duser.name=${USER_NAME} clean package"
                    sh "rm -rf jretemp && mkdir -v jretemp && unzip -q *windows*21*.zip -d jretemp"
                    sh """
                        JRE_DIR=\$(ls -d jretemp/jdk-* 2>/dev/null || ls -d jretemp/jre-* 2>/dev/null)
                        mv \${JRE_DIR} jretemp/jre
                    """
                }
            }
        }

        stage('Build jvm-explorer-windows') {
            steps {
                script {
                    packageApp('win')
                }
            }

            post {
                success {
                    archiveArtifacts 'jvm-explorer*win*.zip'
                }
                failure {
                    echo 'Build jvm-explorer-windows failed'
                }
                aborted {
                    echo 'Build aborted'
                }
            }
        }

        stage('Prepare Mac Build') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    sh "$M2_HOME/bin/mvn -B --no-transfer-progress -s $M2_HOME/conf/settings.xml -Djavafx.platform=mac -Dmaven.test.skip=true -Dmaven.compile.fork=true -Duser.name=${USER_NAME} clean package"
                    sh "rm -rf jretemp && mkdir -v jretemp && tar -xzf *mac*21*.tar.gz -C jretemp"
                    sh """
                        JRE_DIR=\$(ls -d jretemp/jdk-*/Contents/Home 2>/dev/null || ls -d jretemp/jre-*/Contents/Home 2>/dev/null)
                        mv \${JRE_DIR} jretemp/jre
                    """
                }
            }
        }

        stage('Build jvm-explorer-mac') {
            steps {
                script {
                    packageApp('mac')
                }
            }

            post {
                success {
                    archiveArtifacts 'jvm-explorer*mac*.zip'
                }
                failure {
                    echo 'Build jvm-explorer-mac failed'
                }
                aborted {
                    echo 'Build aborted'
                }
            }
        }

        stage('Prepare Linux Build') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    sh "$M2_HOME/bin/mvn -B --no-transfer-progress -s $M2_HOME/conf/settings.xml -Djavafx.platform=linux -Dmaven.test.skip=true -Dmaven.compile.fork=true -Duser.name=${USER_NAME} clean package"
                    sh "rm -rf jretemp && mkdir -v jretemp && tar -xzf *linux*21*.tar.gz -C jretemp"
                    sh """
                        JRE_DIR=\$(ls -d jretemp/jdk-* 2>/dev/null || ls -d jretemp/jre-* 2>/dev/null)
                        mv \${JRE_DIR} jretemp/jre
                    """
                }
            }
        }

        stage('Build jvm-explorer-linux') {
            steps {
                script {
                    packageApp('linux')
                }
            }

            post {
                success {
                    archiveArtifacts 'jvm-explorer*linux*.zip'
                }
                failure {
                    echo 'Build jvm-explorer-linux failed'
                }
                aborted {
                    echo 'Build aborted'
                }
            }
        }

    }

    post {
        always {
            cleanWs()
        }
    }
}

def packageApp(os) {
    def version = sh(
        script: "${M2_HOME}/bin/mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
        returnStdout: true
    ).trim()
    def scriptDir = "scripts/${os}"
    sh "rm -rf staging && mkdir -p staging/agent"
    sh "cp explorer/target/explorer.jar staging/"
    sh "cp -r explorer/target/lib staging/"
    sh "cp agent/target/agent.jar staging/agent/"
    sh "cp launch-agent/target/launch-agent.jar staging/agent/"
    sh "cp README.md staging/"
    sh "cp ${scriptDir}/* staging/"
    sh "cp -r jretemp/jre staging/"
    sh "cd staging && zip -qr ../jvm-explorer-${os}_${version}_b${BUILD_NUMBER}_\$(date +%Y%m%d).zip . && cd .."
    sh "rm -rf staging"
}
