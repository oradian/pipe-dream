package com.oradian.pipedream.integration

import org.jenkinsci.plugins.workflow.cps.CpsScript

interface Integration extends Serializable {
    def notifyBuildInProgress()
    def notifyBuildStatus(Boolean status)
    def commentPR(String message)
}
