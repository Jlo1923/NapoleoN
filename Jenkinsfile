#!/usr/bin/env groovy
final String REGION = "us-west-2"

node('master') {
    stage("Cleaning existing resources"){
        cleanWs()
    }
    stage("Setup"){
        checkout scm
        GIT_COMMIT_MSG = sh (script: 'git log -1 --pretty=%B ${GIT_COMMIT}', returnStdout: true).trim().replace(" ", "-").replace("/", "-").replace(":", "-")
        if(GIT_COMMIT_MSG.contains("Increasing-version-to")){
            echo "Increased version build finishing early"
            currentBuild.result = 'ABORTED'
            error('Stopping early…')
        }
    }
    stage("Downloading JKS"){
        s3Download(file:'app/pepito.jks', bucket:'critical-resources', path:'pepito.jks', force:true)
    }

    VERSION = sh(script:"cat app/build.gradle | grep \"versionName\" | sed 's/\"//g' | tr -d \" \\t\" | sed 's/versionName//g'",returnStdout: true).trim()
    VERSIONCODE = sh(script:"cat app/build.gradle | grep \"versionCode\" | sed 's/\"//g' | tr -d \" \\t\" | sed 's/versionCode//g'",returnStdout: true).trim().toInteger()
    INCREASEDVERSION = VERSIONCODE + 1
    FINALVERSIONNAME = "1.1.${INCREASEDVERSION}-${GIT_COMMIT_MSG}"
    sh("sed -i 's/versionCode ${VERSIONCODE}/versionCode ${INCREASEDVERSION}/g' app/build.gradle")
    sh("sed -i 's/${VERSION}/${FINALVERSIONNAME}/g' app/build.gradle")
    sh("cat app/build.gradle")
    stage("Building"){
        echo "VersionName ${FINALVERSIONNAME}"
        echo "VersionCode ${INCREASEDVERSION}"
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
                        [language: 'en-GB', text: "Version ${INCREASEDVERSION}."],
                        [language: 'es-ES', text: "Version ${INCREASEDVERSION}."]
                ]
    }
    withCredentials([usernamePassword(credentialsId: 'jenkinsbitbucket', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
        sh("git clone https://${GIT_USERNAME}:${GIT_PASSWORD}@bitbucket.org/napoteam/nuevo-napoleon-secret-chat-android.git")
        sh("cd nuevo-napoleon-secret-chat-android")
        sh("git checkout development")
        sh("sed -i 's/versionCode ${VERSIONCODE}/versionCode ${INCREASEDVERSION}/g' app/build.gradle")
        sh("sed -i 's/${VERSION}/${FINALVERSIONNAME}/g' app/build.gradle")
        sh("cat app/build.gradle")
        echo "${GIT_COMMIT_MSG}"
        sh("git add app/build.gradle")
        sh("git commit -a -m \"Increasing version to ${INCREASEDVERSION}\"")
        sh("git push")
    }

    stage("Slack notification"){
        HORA = sh(script:"date +%T", returnStdout: true).trim();
        slackSend (botUser: true, color: '#A4C639', channel: "desarrollo", tokenCredentialId: 'slack-token', message: "nuevo-napoleon-secret-chat-android ha actualizado al VersionName *${FINALVERSIONNAME}* con código de version *${INCREASEDVERSION}* en el build ${env.BUILD_NUMBER} hoy a las ${HORA}. ${env.BUILD_URL}")
    }
    cleanWs()
}