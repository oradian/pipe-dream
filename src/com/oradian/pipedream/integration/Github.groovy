package com.oradian.pipedream.integration

import org.jenkinsci.plugins.workflow.cps.CpsScript

import com.oradian.pipedream.Credentials

class Github implements Integration {
    private transient CpsScript step
    protected transient def pullRequest = null

    Boolean getIsPR() { return step.env.CHANGE_ID != null }
    String getUrl() { return step.env.JOB_URL }

    Github(CpsScript step) {
        this.step = step
    }

    static Github create(CpsScript step) {
        def self = new Github(step)
        if (self.isPR) {
            self.pullRequest = step.pullRequest
            try {
                def credentials = new Credentials('GITHUB_PERSONAL_TOKEN')
                self.pullRequest.setCredentials(credentials.username, credentials.password)
            } catch(_) { }
        }
        return self
    }

    def notifyBuild(String status) {
        if (pullRequest)
            return pullRequest.createStatus(status: status,
                                            targetUrl: url)
    }

    def notifyBuildInProgress() {
        return notifyBuild('pending')
    }

    def notifyBuildStatus(Boolean status) {
        def human = status ? 'success' : 'failure'
        return notifyBuild(human)
    }

    def commentPR(String message) {
        if (pullRequest)
            return pullRequest.comment(message)
    }
}
