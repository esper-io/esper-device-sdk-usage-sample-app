/*************************************************************************************************
 *
 * (c) 2022 All Rights Reserved.
 *
 *  This JenkinsFile calls the real build job to build the DPC/SDK code. This file will live in the
 *  the repo for Jenkins' hooks to pick it up and start builds for PRs, merges, etc.
 *
 *************************************************************************************************/

import groovy.transform.Field
import jenkins.model.CauseOfInterruption
import hudson.model.*
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import groovy.json.JsonSlurperClassic

@Field def g_BranchToReleaseTypeMap = [
    'develop' : 'alpha',
    'staging' : 'beta',
    'master' : 'prod',
    'RelCandidate' : 'Experimental',
    'SHN-15702-ci': 'alpha'
]
@Field def g_BranchToStacksTypeMap = [
    'develop': 'development',
    'staging': 'staging',
    'master': 'production',
    'RelCandidate': 'rel_candidate',
    'SHN-15702-ci': 'development'
]
@Field def g_branchToFileName = [
    'develop': 'develop',
    'RelCandidate' : 'Experimental',
    'staging' : 'staging',
    'master' : 'master',
    'SHN-15702-ci': 'development'
]

@Field def slackMessageTitle = 'DPC Build Notification'
@Field def slackMessageChannel = '#device-deployments'
@Field def ShoonyDpcPublisherCredId = 'shoonya_jenkins_cloud_ui_build_user'
@Field def ShoonyDpcPublisherAwsRegion = 'ap-south-1'
@Field def ShoonyDpcPublisherS3Bucket = 'shoonya-dpc'
@Field def JenkinsCloudApiAccessCreds = 'jenkins_cloud_api_access_creds'
@Field def releaseBranch = 'master'
@Field def g_releaseChannel = ''
@Field def g_buildNumber = ''

def sendSlackMessage(titleText, messageText, messageColor, channelName) {
    JSONArray attachments = new JSONArray()
    JSONObject attachment = new JSONObject()

    attachment.put('title', titleText.toString())
    attachment.put('text', messageText.toString())
    attachment.put('color', messageColor)

    attachments.add(attachment)
    slackSend(botUser: true, channel: channelName, attachments: attachments.toString())
}

def injectCreds() {
    // Copy the secret file locally for inclusion into the container. It will be cleaned up automatically
    // during the cleanup() stage
    withCredentials([file(credentialsId: 'shoonya-dpc-key-jks-file', variable: 'SECRET_FILE')])
    {
        sh "cp ${SECRET_FILE} app/secret.file"
    }

    // Copy the keystore.properties file for inclusion into the container. It already points to the secret file as
    // "storeFile=/application/secret.file"
    withCredentials([file(credentialsId: 'keystore.properties', variable: 'KS_PROPS_FILE')])
    {
        sh "cp ${KS_PROPS_FILE} app/keystore.properties"
    }

    withCredentials([[$class: 'UsernamePasswordMultiBinding',
        credentialsId: 'jfrog-artifactorycreds',
        usernameVariable: 'USERNAME',
        passwordVariable: 'PASSWORD']])
    {
        g_arifactoryUsername = "${USERNAME}"
        g_arifactoryPassword = "${PASSWORD}"
    }

    echo 'Secrets in place, listing files.'
    sh 'ls -al app/'
}

def runBuildInDocker(String dpcBuildNumber, String releaseChannel) {
    def latestCommit = "${env.GIT_COMMIT}"
    //SDK Publish is separated into different job
    def publishEsperDeviceSDK = false
    // ecr vars to use for pull docker image
    def dockerEcrUrl = '973484954817.dkr.ecr.us-west-2.amazonaws.com'
    def dockerEcrParms = 'ecr:us-west-2:c1981b34-fa8a-4d88-b3ce-a7adb5c7f71c'

    // login into ECR and get ready to pull image
    docker.withRegistry("https://${dockerEcrUrl}", dockerEcrParms)
    {
        // pull image (only if it's not already on the box)
        def currImage = docker.image('shoonya-android-builder-image:latest')
        currImage.pull()

        // get the current user's id -- we will pass this to the container for gosu to kick it in. Note the trim()
        def currUserId = sh(returnStdout: true, script: 'id -u $USER').trim()
        echo "Current runner's UID = ${currUserId}"

        // make the requested script executable, before mapping it into the container
        sh 'chmod a+x jenkins/build_app.sh'

        // set the docker image & tag for use for the pull/run
        def dockerImageNameAndTag = "${dockerEcrUrl}/shoonya-android-builder-image:latest"
        echo "dockerImageNameAndTag= ${dockerImageNameAndTag}"

        // massage the build numbers
        def buildNumberAsInt = dpcBuildNumber as Integer
        def fourDigitBuildNumber = String.format('%04d', buildNumberAsInt)

        // first get cached-files from S3, if any, to prevent unecessary dependency downloads during the build
        // note that we are making the cli only compare the size and not the timestamp to prevent unnecessary
        // downloads from S3. The size is ~275MB.
        dir('/buildspace/gradle_dependency_cache')
        {
            sh 'aws s3 sync s3://shoonya-devops-bucket/GradleDependencyCache/ . --size-only'
        }

        // 
        //
        // Start the main build in the downloaded docker image
        // -v /buildspace/gradle_dependency_cache:/gradle_dependency_cache \
        //

        sh "docker run --rm -e BUILD_NUMBER=${buildNumberAsInt} \
                            -e FOURDIGIT_BUILD_NUMBER=${fourDigitBuildNumber} \
                            -e PUBLISH_ESPER_DEVICE_SDK=${publishEsperDeviceSDK} \
                            -e ARTIFACTORY_USERNAME=${g_arifactoryUsername} \
                            -e ARTIFACTORY_PASSWORD=${g_arifactoryPassword} \
                            -e RELEASE_CHANNEL=${releaseChannel} \
                            -e LOCAL_USER_ID=${currUserId} \
                            -v /buildspace/gradle_dependency_cache:/gradle_dependency_cache \
                            -v ${pwd()}/jenkins/build_app.sh:/build_app.sh \
                            -v ${pwd()}/app:/application '${dockerImageNameAndTag}' \
                            sh -c /build_app.sh"
    }
}

def archiveFolders(String buildFolderName, String zipFileSuffix) {
    def archiveFoldersList = ['outputs', 'reports', 'test-results']
    def prefix = 'esper-sdk-sample'

    // archive the build-status file--this is at the root of the downloaded code
    archiveArtifacts artifacts: 'build_status.log', fingerprint: true, allowEmptyArchive: true

    dir("app/${buildFolderName}") {
        archiveFoldersList.each { archiveFolder ->
            if (fileExists(archive_folder)) {
                def zipFilename = "${prefix}-build-${archive_folder}${zipFileSuffix}.zip"
                zip zipFile: zipFilename, dir: archive_folder
                archiveArtifacts artifacts: zipFilename, fingerprint: true, allowEmptyArchive: true
            }
        }
    }
}

def performPostBuildActivities() {
    // from the logs, make sure
    def buildSuccessful = sh(script: "grep -Fxq 'Build successful' build_status.log", returnStatus: true) == 0
    if (!buildSuccessful) {
        error('Build failed.')
    }

    //echo 'Running junit on tests..'
    //junit '**/TEST-*.xml'

    echo 'Running the lint-analysis tool..'
    androidLint()

    // archive all interesting folders and attach it to this build
    archiveFolders('build', '')
}

def buildApps(String dpcBuildNumber, String releaseChannel) {
    //
    // At this point, we are already in the folder where the entire source code for this app is
    //

    // first inject the build-related credentials into the file system
    injectCreds()

    // run the build(s) in the designated docker image
    runBuildInDocker(dpcBuildNumber, releaseChannel)

    // ensure build succeeded, and activities such as lint-checks.
    performPostBuildActivities()
}

pipeline
{
    agent {
        label 'dpc-builder'
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    checkout(scm)
                    sh 'ls -la'
                    g_releaseChannel = g_BranchToReleaseTypeMap[env.BRANCH_NAME] ?: 'alpha'
                    time_stamp = new Date().format('yyyyMMddHHmm')
                    g_buildNumber = [g_releaseChannel, time_stamp].join('-')
                    echo "Build_Number: ${g_buildNumber}"
                }
            }
        }
        stage('Branch indexing: abort') {
            when {
                allOf {
                    triggeredBy cause: 'BranchIndexingCause'
                    not {
                        changeRequest()
                    }
                }
            }
            steps {
                script {
                    echo 'Branch discovered by branch indexing. No way this build is gonna run!'
                    currentBuild.result = 'SUCCESS'
                    error 'Caught branch indexing...'
                }
            }
        }
        stage('Abort running build')  {
            when {
                expression {
                    return env.BRANCH_NAME ==~ /(develop|RelCandidate|staging|SHN-15702-ci)/ || changeRequest()
                }
            }
            steps  {
                script
                {
                    def item = Jenkins.instance.getItemByFullName(env.JOB_NAME)
                    item.getAllJobs().each { job ->
                        job._getRuns().iterator().each { run ->
                            def exec = run.getExecutor()
                            //if the run is not a current build and it has executor (running) then stop it
                            if ( run.number.toString() != env.BUILD_ID && exec != null ) {
                                //prepare the cause of interruption
                                def cause = { "interrupted by build #${run.getId()}" as String } as CauseOfInterruption
                                exec.interrupt(Result.ABORTED, cause)
                            }
                        }
                    }
                }
            }
        } // stage - Abort running build

        stage("Build") {
            steps {
                timestamps {
                    script {
                        // get a Sampleapp version number
                        //def buildJob = build (job: 'DeviceBuilds/sampleapp-versions', propagate: true)
                        g_DpcVersionBuildNumber = "2"
                        echo "Version_name: ${g_DpcVersionBuildNumber}"
                        // let the builder library build the code and archive it
                        buildApps(g_DpcVersionBuildNumber,g_releaseChannel)
                    }
                }
            }
        } //Stage - Build

        stage("Upload APK to S3") {
            when {
                anyOf {
                    expression {
                        env.BRANCH_NAME ==~ /(develop|RelCandidate|staging|SHN-15702-ci)/
                    }
                    branch g_releaseBranch
                }
            }
            steps {
                timestamps {
                    script {
                        sh "ls -al"
                        def g_buildPathS3 = g_releaseChannel
                        sh "ls -al app/"
                        def buildPathS3 = "sampleapp/"+ g_buildPathS3
                        sh "ls -al app/app/build/outputs/apk/release/"
                        def jsonData = readJSON file: 'app/app/build/outputs/apk/release/output-metadata.json'
                        g_dpcApkFile = "${pwd()}/app/app/build/outputs/apk/release/app-release.apk"
                        g_dpcVersionCode = jsonData.elements[0].versionCode
                        g_dpcVersionName = jsonData.elements[0].versionName
                        g_DpcApkFilename = "espersdk_sample_v${g_dpcVersionCode}_${g_dpcVersionName}.apk"
                        echo "DPC for has versionCode = ${g_dpcVersionCode} and versionName = ${g_dpcVersionName}"
                    }
                }
            }
        }
    }
}