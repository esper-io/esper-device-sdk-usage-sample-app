import groovy.json.JsonOutput
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import groovy.json.JsonSlurperClassic
import groovy.transform.Field
import groovy.json.JsonOutput
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import groovy.json.JsonSlurperClassic

@Field def g_slackMessageChannel = '#kservcicdjobtest'
@Field def g_ShoonyDpcPublisherCredId = 'shoonya_jenkins_cloud_ui_build_user'
@Field def g_slackMessageTitle = 'Sampleapp Build Notification'

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
        sh "cp ${SECRET_FILE} secret.file"
    }

    // Copy the keystore.properties file for inclusion into the container. It already points to the secret file as
    // "storeFile=/application/secret.file"
    withCredentials([file(credentialsId: 'keystore.properties', variable: 'KS_PROPS_FILE')])
    {
        sh "cp ${KS_PROPS_FILE} keystore.properties"
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
    sh 'ls -al'
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
                            -v ${pwd()}:/application '${dockerImageNameAndTag}' \
                            sh -c /build_app.sh"
    }
}

def archiveFolders(String buildFolderName, String zipFileSuffix) {
    def archiveFolderMap = [
        "espersdksample/${buildFolderName}" : ['esper-sdk-sample' : ['outputs', 'reports', 'test-results']]
        ]
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
    //archiveFolders('build-output', '')
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

