#!/usr/bin/env groovy
final String REGION = "us-west-2"

node('master') {
    stage("Cleaning existing resources"){
        cleanWs()
    }
    stage("Checkout"){
        checkout scm
    }
    stage("Downloading JKS"){
        s3Download(file:'app/pepito.jks', bucket:'critical-resources', path:'pepito.jks', force:true)
    }
    GIT_COMMIT_MSG = sh (script: 'git log -1 --pretty=%B ${GIT_COMMIT}', returnStdout: true).trim().replace(" ", "-")
    VERSION = sh(script:"cat app/build.gradle | grep \"versionName\" | sed 's/\"//g' | tr -d \" \\t\" | sed 's/versionName//g'",returnStdout: true).trim()
    VERSIONCODE = sh(script:"cat app/build.gradle | grep \"versionCode\" | sed 's/\"//g' | tr -d \" \\t\" | sed 's/versionCode//g'",returnStdout: true).trim().toInteger()
    INCREASEDVERSION = VERSIONCODE + 1
    FINALVERSIONNAME = "1.1.${INCREASEDVERSION}-${GIT_COMMIT_MSG}"
    sh("sed -i 's/versionCode ${VERSIONCODE}/versionCode ${INCREASEDVERSION}/g' app/build.gradle")
    sh("sed -i 's/${VERSION}/${FINALVERSIONNAME}/g' app/build.gradle")
    sh("cat app/build.gradle")
    echo "${GIT_COMMIT_MSG}"
    stage("Building"){
        echo "VersionName ${FINALVERSIONNAME}"
        echo "VersionCode ${VERSIONCODE}"
        sh(script:"chmod +x ./gradlew")
        sh(script:"./gradlew clean bundle")
        sh(script:"ls -la")
    }

    script {
        archiveArtifacts allowEmptyArchive: true,
                artifacts: '**/*.apk, **/*.aab, app/build/**/mapping/**/*.txt, app/build/**/logs/**/*.txt, app/build/**/bundle'
    }
    stage('Upload to Play Store') {
        androidApkUpload googleCredentialsId: 'Google-Play', filesPattern: 'app/build/outputs/bundle/pruebaInterna/*.aab', trackName: 'internal', releaseName: "${FINALVERSIONNAME}", rolloutPercentage: '100', inAppUpdatePriority: '5',
                recentChangeList: [
                        [language: 'en-GB', text: "Please test the changes from Jenkins build ${env.BUILD_NUMBER}."],
                        [language: 'de-DE', text: "Bitte die Änderungen vom Jenkins Build ${env.BUILD_NUMBER} testen."]
                ]
    }
    sh("git commit -a -m Increasing version to ${INCREASEDVERSION}")
    withCredentials([usernamePassword(credentialsId: '10525424-276e-4897-9921-abdb95d2735a', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
        sh("git push https://${GIT_USERNAME}:${GIT_PASSWORD}@bitbucket.org/napoteam/nuevo-napoleon-secret-chat-android/ development")
    }

    stage("Slack notification"){
        HORA = sh(script:"date +%T", returnStdout: true).trim();
        slackSend (botUser: true, color: '#A4C639', channel: "desarrollo", tokenCredentialId: 'slack-token', message: "nuevo-napoleon-secret-chat-android ha actualizado al VersionName *${FINALVERSIONNAME}* con código de version *${VERSIONCODE}* en el build ${env.BUILD_NUMBER} hoy a las ${HORA}. ${env.BUILD_URL}")
    }
    cleanWs()
}


