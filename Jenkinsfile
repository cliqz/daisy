#!/bin/env groovy
node('master'){
    def imageName = 'daisy'
    def scmVars = [:]
    stage('Checkout'){
        scmVars = checkout scm
    }
    stage('Build docker image') {
        docker.build(imageName, '--build-arg UID=`id -u` --build-arg GID=`id -g` .')
    }
    docker.image(imageName).inside() {
        try {
            withEnv([
                "GIT_COMMIT=${scmVars.GIT_COMMIT}"
                ]) {
                withCredentials([
                        file(credentialsId: '263e59fb-e9de-4e51-962c-0237c6ee167b', variable: 'ANDROID_STORE_FILE'),
                        string(credentialsId: '60354bba-8ed0-4df9-8f8e-5be7454c1680', variable: 'ANDROID_STORE_PWD'),
                        file(credentialsId: '2939d2e1-dd9a-4097-adc2-430e3d67157a', variable: 'PLAY_STORE_CERT'),
                        file(credentialsId: 'sentry_dsn_daisy', variable: 'SENTRY_DSN_FILE')
                ]) {
                    stage('Compile and Upload') {
                        sh '''#!/bin/bash -l
                            set -x
                            set -e
                            export ANDROID_KEY_ALIAS=browser
                            export ANDROID_KEY_PWD="$ANDROID_STORE_PWD"
                            export FASTLANE_DISABLE_COLORS=1
                            bundle config set path vendor/bundle
                            bundle install
                            bundle exec fastlane android build
                        '''
                    }
                }
            }
        } finally {
            stage('Upload Artifacts and Clean Up') {
                archiveArtifacts allowEmptyArchive: true, artifacts: 'app/build/**/*.apk'
                sh'''#!/bin/bash -l
                    set -x
                    set -e
                    rm -rf build/ app/build freshtab/build|| true
                '''
            }
        }
    }
}
