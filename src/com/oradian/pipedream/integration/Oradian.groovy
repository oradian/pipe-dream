package com.oradian.pipedream.integration

import org.jenkinsci.plugins.workflow.cps.CpsScript;

class Oradian implements Integration {
    private def ciImage
    private CpsScript steps

    Oradian(CpsScript steps) {
        this.steps = steps
    }

    static Oradian create(CpsScript steps) {
        return new Oradian(steps)
    }

    def setCiImage(value) {
        ciImage = value
    }

    def getCiImage() {
        return ciImage ?: steps.docker.image("oradian_ci:master")
    }

    def notifyBuildInProgress() {
        def self = this
        steps.stage('Notify build in progress') {
            steps.withCredentials([steps.usernamePassword(credentialsId: 'OAUTH_BITBUCKET_JENKINS', passwordVariable: 'BITBUCKET_OAUTH_ACCESS_TOKEN', usernameVariable: 'BITBUCKET_OAUTH_CLIENT_ID')]) {
                steps.singlenode {
                    self.ciImage.inside {
                        steps.sh 'python /usr/src/app/main.py pr_cleanup pr_build_in_progress'
                    }
                }
            }
        }
    }

    def notifyBuildStatus(Boolean status) {
        def self = this
        steps.stage('Notify build status') {
            steps.withEnv(["BUILD_OK=$status"]) {
                steps.withCredentials([steps.usernamePassword(credentialsId: 'OAUTH_BITBUCKET_JENKINS', passwordVariable: 'BITBUCKET_OAUTH_ACCESS_TOKEN', usernameVariable: 'BITBUCKET_OAUTH_CLIENT_ID')]) {
                    steps.singlenode {
                        self.ciImage.inside {
                            steps.sh 'python /usr/src/app/main.py pr_build_status pr_approve'
                        }
                    }
                }
            }
        }
        steps.currentBuild.result = status ? 'SUCCESS' : 'FAILURE'
    }

    def commentPR() {
        // TODO
    }
}
