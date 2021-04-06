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
    stage("Building"){
        VERSION = sh(script:"cat app/build.gradle | grep \"versionName\" | sed 's/\"//g' | tr -d \" \\t\" | sed 's/versionName//g'",returnStdout: true).trim()
        VERSIONCODE = sh(script:"cat app/build.gradle | grep \"versionCode\" | sed 's/\"//g' | tr -d \" \\t\" | sed 's/versionCode//g'",returnStdout: true).trim()
        echo "VersionName ${VERSION}"
        echo "VersionCode ${VERSIONCODE}"
        sh(script:"chmod +x ./gradlew")
        sh(script:"./gradlew clean bundle")
        sh(script:"ls -la")
    }
    script {
        archiveArtifacts allowEmptyArchive: true,
                artifacts: '**/*.apk, **/*.aab, app/build/**/mapping/**/*.txt, app/build/**/logs/**/*.txt, app/build/**/bundle'
    }
    stage("Slack notification"){
        HORA = sh(script:"date +%T", returnStdout: true).trim();
        slackSend (botUser: true, color: '#FFFF00', channel: "desarrollo", tokenCredentialId: 'slack-token', message: "nuevo-napoleon-secret-chat-android ha compilado satisfactoriamente el VersionName *${VERSION}* con código de version *${VERSIONCODE}* en el build ${env.BUILD_NUMBER} hoy a las ${HORA}. ${env.BUILD_URL}")
    }
    cleanWs()
}


