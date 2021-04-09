#!/usr/bin/env groovy
final String REGION = "us-west-2"

node('master') {
    stage("Cleaning existing resources"){
        def versionName = params.VersionName
        dev environment = params.Environment
        def recentChangeEs = params.recentChangeEs
        def recentChangeEn = params.recentChangeEn
        def version
        cleanWs()
    }

    stage("Setup"){
        checkout scm
        GIT_COMMIT_MSG = sh (script: 'git log -1 --pretty=%B ${GIT_COMMIT}', returnStdout: true).trim().replaceAll(" ", "-").replaceAll("/", "-").replaceAll(":", "-").replaceAll("_", "-").replaceAll("\\(.*?\\)", "").replaceAll("\n", "").replaceAll("\r", "").trim()
    }

    if(GIT_COMMIT_MSG.contains("Increasing-version-to")){
        echo "Increased version build finishing early"
        currentBuild.result = "ABORTED"
        return
    }

    stage("Downloading JKS"){
        s3Download(file:'app/pepito.jks', bucket:'critical-resources', path:'pepito.jks', force:true)
    }

    stage("Generating version") {
        versionName = "${GIT_COMMIT_MSG}"
        if (params.VersionName?.trim()) {
            versionName = ${params.VersionName}
        }
        version = sh(script: "cat app/build.gradle | grep \"versionName\" | sed 's/\"//g' | tr -d \" \\t\" | sed 's/versionName//g'", returnStdout: true).trim()
        versionCode = sh(script: "cat app/build.gradle | grep \"versionCode\" | sed 's/\"//g' | tr -d \" \\t\" | sed 's/versionCode//g'", returnStdout: true).trim().toInteger()
        increasedVersion = versionCode + 1
        finalVersionName = "1.1.${increasedVersion}-${versionName}"
        sh("sed -i 's/versionCode ${versionCode}/versionCode ${increasedVersion}/g' app/build.gradle")
        sh("sed -i 's/${version}/${finalVersionName}/g' app/build.gradle")
    }

    stage("Building"){
        echo "VersionName ${finalVersionName}"
        echo "VersionCode ${increasedVersion}"
        sh(script:"chmod +x ./gradlew")
        sh(script:"./gradlew clean bundle")
        sh(script:"ls -la")
        script {
            archiveArtifacts allowEmptyArchive: true,
                    artifacts: '**/*.apk, **/*.aab, app/build/**/mapping/**/*.txt, app/build/**/logs/**/*.txt, app/build/**/bundle'
        }
    }

    stage("Upload to Play Store") {
        androidApkUpload googleCredentialsId: "Google-Play", filesPattern: "app/build/outputs/bundle/${environment}/*.aab", trackName: "internal", releaseName: "${finalVersionName}", rolloutPercentage: "100", inAppUpdatePriority: "5",
                recentChangeList: [
                        [language: "en-US", text: "${recentChangeEs}."],
                        [language: "es-ES", text: "${recentChangeEn}."]
                ]
    }

    stage("Increasing version") {
        withCredentials([usernamePassword(credentialsId: 'jenkinsbitbucket', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
            sh("git clone https://${GIT_USERNAME}:${GIT_PASSWORD}@bitbucket.org/napoteam/nuevo-napoleon-secret-chat-android.git")
            sh("cd nuevo-napoleon-secret-chat-android")
            sh("git checkout development")
            sh("sed -i 's/versionCode ${versionCode}/versionCode ${increasedVersion}/g' app/build.gradle")
            sh("sed -i 's/${version}/${finalVersionName}/g' app/build.gradle")
            sh("git add app/build.gradle")
            sh("git commit -a -m \"Increasing version to ${increasedVersion}\"")
            sh("git push")
        }
    }

    stage("Slack notification"){
        HORA = sh(script:"date +%T", returnStdout: true).trim();
        slackSend (botUser: true, color: '#A4C639', channel: "desarrollo", tokenCredentialId: 'slack-token', message: "nuevo-napoleon-secret-chat-android ha actualizado al VersionName *${finalVersionName}* con código de version *${increasedVersion}* en el build ${env.BUILD_NUMBER} hoy a las ${HORA}. ${env.BUILD_URL}")
    }

    cleanWs()
}