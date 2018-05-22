package com.oradian.pipedream

import org.jenkinsci.plugins.workflow.cps.CpsScript;

class BuildUtil implements Serializable {
    private CpsScript steps;

    BuildUtil(CpsScript steps) {
        this.steps = steps
    }

    def getEpoch() {
        (new Date()).format("yyMMddHHmmss") + steps.currentBuild.number
    }

    def abortPreviousBuilds() {
        for (def build = steps.currentBuild.previousBuild; build != null; build = build.previousBuild) {
            def rb = build.rawBuild
            if (rb.isInProgress() && rb.isBuilding()) {
                steps.echo "Aborting previous build #${rb.id}"
                rb.executor.interrupt()
            }
        }
    }

    def cleanupWorkspace(cleanWhenFailure = false) {
        // Try to use the plugin first, if installed.
        try {
            steps.step([$class: 'WsCleanup', cleanWhenFailure: cleanWhenFailure])
        } catch(_) { }

        // We don't trust the plugin anyways, so lets just make sure everything is cleaned up.
        if (cleanWhenFailure || steps.currentBuild.result != false) {
            [
                steps.env.WORKSPACE,
                steps.pwd(tmp: true)
            ].each{
                try {
                    steps.dir(it) {
                        steps.deleteDir()
                        steps.writeFile file:'.empty', text:''
                    }
                } catch(_) { }
            }
        }
    }
}
