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
        s3Download(file:'pepito.jks', bucket:'critical-resources', path:'pepito.jks', force:true)
    }
    stage("Building"){
        VERSION = sh(script:"cat app/build.gradle | grep \"versionName\" | sed 's/\"//g' | tr -d \" \\t\" | sed 's/versionName//g'",returnStdout: true).trim()
        echo "${VERSION}"
        sh(script:"./gradlew clean :app:bundleRelease :app:assembleRelease")
        sh(script:"ls -la")
    }
    stage("Slack notification"){
        //HORA = sh(script:"date +%T", returnStdout: true).trim();
        //slackSend (botUser: true, color: '#FFFF00', channel: "desarrollo", tokenCredentialId: 'slack-token', message: "NN-Backend-Secret-Chat ha actualizado a la versión *${VERSION}* en el build ${env.BUILD_NUMBER} hoy a las ${HORA}. ${env.BUILD_URL}")
    }
}


