package com.oradian.pipedream

import org.jenkinsci.plugins.workflow.cps.CpsScript;

class Checkout implements Serializable {
    private CpsScript step;

    Checkout(CpsScript step) {
        this.step = step
    }

    def checkout(config) {
        def doTheCheckout = false
        try {
            def currentRemotes = step.sh(script: "git remote -v | awk '{print \$2}'", returnStdout: true).trim().split("\n") as Set
            def targetRemotes = config.scm.userRemoteConfigs.collect{ it.url } as Set

            def remotesAreSame = (currentRemotes == targetRemotes)

            def targetBranch = config.scm.branches[0].name
            def currentCommit = step.sh(script: "git rev-parse HEAD", returnStdout: true).trim()
            def targetCommit = step.sh(script: "git rev-parse --remotes=$targetBranch", returnStdout: true).trim()

            def branchesAreSame = (currentCommit == targetCommit)

            if (remotesAreSame && branchesAreSame) {
                step.println("Already on commit $currentCommit of ${currentRemotes}. Skipping checkout.")
            } else if (!branchesAreSame) {
                step.sh "git checkout $targetCommit"
            } else {
                doTheCheckout = true
            }
        } catch(_) {
            doTheCheckout = true
        }

        if (doTheCheckout)
            step.checkout(config)
    }
}
