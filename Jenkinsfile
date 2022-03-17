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
@Field def g_utils = ''
@Field def g_releaseChannel = ''
@Field def g_buildNumber = ''

pipeline
{
    agent {
        label 'dpc-builder'
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    sh 'ls -la'
                    g_utils=load './jenkins/Utils.groovy'
                    g_releaseChannel = g_BranchToReleaseTypeMap[env.BRANCH_NAME] ?: 'alpha'
                    time_stamp = new Date().format('yyyyMMddHHmm')
                    g_buildNumber = [g_releaseChannel, time_stamp].join('-')
                    echo "Build_Number: ${g_buildNumber}: ${g_utils}"
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
                        echo "${g_utils}"
                        g_utils.buildApps(g_DpcVersionBuildNumber,g_releaseChannel)
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
                        def jsonData = readJSON file: 'app/build-output-pg-enabled/outputs/apk/release/output-metadata.json'
                        g_dpcApkFile = "${pwd()}/app/build-output-pg-enabled/outputs/apk/release/app-release.apk"
                        g_dpcVersionCode = jsonData.elements[0].versionCode
                        g_dpcVersionName = jsonData.elements[0].versionName
                        g_DpcApkFilename = "shoonya_dpc_v${g_dpcVersionCode}_${g_dpcVersionName}.apk"
                        echo "DPC for has versionCode = ${g_dpcVersionCode} and versionName = ${g_dpcVersionName}"
                    }
                }
            }
        }
    }
}