#!/usr/bin/env groovy
final String REGION = "us-west-2"

node('master') {
    stage("Cleaning existing resources"){
        def newVersionName = params.VersionName
        def environment = params.Environment
        def recentChangeEs = params.recentChangeEs
        def recentChangeEn = params.recentChangeEn
        def deployToStore = params.DeployToStore
        def currentVersionName
        cleanWs()
    }
    if(environment == "prod"){
        input 'This build will be affect production env, you want to continue?'
    }

    stage("Setup"){
        checkout scm
        gitCommitMessage = sh (script: 'git log -1 --pretty=%B ${GIT_COMMIT}', returnStdout: true).trim().replaceAll(" ", "-").replaceAll("/", "-").replaceAll(":", "-").replaceAll("_", "-").replaceAll("\\(.*?\\)", "").replaceAll("\n", "").replaceAll("\r", "").trim()
    }

    if(gitCommitMessage.contains("Increasing-version-to")){
        echo "Increased version build finishing early"
        currentBuild.result = "ABORTED"
        return
    }

    stage("Downloading JKS"){
        s3Download(file:'app/pepito.jks', bucket:'critical-resources', path:'pepito.jks', force:true)
    }

    stage("Generating version") {
        currentVersionName = sh(script: "cat app/build.gradle | grep \"versionName\" | sed 's/\"//g' | sed 's/versionName//g'", returnStdout: true).trim()
        if (params.VersionName?.trim()) {
            newVersionName = "${params.VersionName}"
        } else {
            newVersionName = "${gitCommitMessage}"
        }

        currentVersionCode = sh(script: "cat app/build.gradle | grep \"versionCode\" | sed 's/\"//g' | tr -d \" \\t\" | sed 's/versionCode//g'", returnStdout: true).trim().toInteger()
        if (params.VersionCode?.trim()) {
            input 'This parameter is going to change the version code of the deploy are you sure?'
            newVersionCode = "${params.VersionCode}"
        } else {
            newVersionCode = currentVersionCode + 1
        }
        finalVersionName = "1.1.${newVersionCode}-${newVersionName}"
        sh("sed -i 's/versionCode ${currentVersionCode}/versionCode ${newVersionCode}/g' app/build.gradle")
        sh("sed -i 's/${currentVersionName}/${finalVersionName}/g' app/build.gradle")
    }

    stage("Building"){
        echo "VersionName ${finalVersionName}"
        echo "VersionCode ${newVersionCode}"
        sh(script:"chmod +x ./gradlew")
        sh(script:"./gradlew clean bundle")
        script {
            archiveArtifacts allowEmptyArchive: true,
                    artifacts: '**/*.apk, **/*.aab, app/build/**/mapping/**/*.txt, app/build/**/logs/**/*.txt, app/build/**/bundle'
        }
    }

    if (deployToStore == true) {
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
                sh("sed -i 's/versionCode ${currentVersionCode}/versionCode ${newVersionCode}/g' app/build.gradle")
                sh("sed -i 's/${currentVersionName}/${finalVersionName}/g' app/build.gradle")
                sh("git add app/build.gradle")
                sh("git commit -a -m \"Increasing-version-to-${newVersionCode}\"")
                sh("git push")
            }
        }

        stage("Slack notification") {
            HORA = sh(script: "date +%T", returnStdout: true).trim();
            slackSend(botUser: true, color: '#A4C639', channel: "desarrollo", tokenCredentialId: 'slack-token', message: "nuevo-napoleon-secret-chat-android ha actualizado al VersionName *${finalVersionName}* con código de version *${newVersionCode}* en el build ${env.BUILD_NUMBER} hoy a las ${HORA}. ${env.BUILD_URL}")
        }
    }
    cleanWs()
}
