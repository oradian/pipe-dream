package com.oradian.pipedream;

import org.jenkinsci.plugins.workflow.cps.CpsScript;

class NotUsedButCool {
    private CpsScript steps

    NotUsedButCool(CpsScript steps) {
        this.steps = steps
    }

    static def assignNode(label) {
        return !label ?
            [$class: 'StringParameterValue', name: 'dummy', value: ""] :
            [$class: 'LabelParameterValue',  name: 'node',  label: label]
    }

    // Colors!
    static def printError(error, colorHuman='red') {
        def bold = 'tput -Tansi bold'.execute().text
        def sgr0 = 'tput -Tansi sgr0'.execute().text

        def colors = ["black", "red", "green", "yellow", "blue", "magenta", "cyan", "white"]
        def colorNumber = colors.indexOf(colorHuman)
        def color = colorNumber < 0 ? sgr0 : "tput -Tansi setaf $colorNumber".execute().text

        echo "${color}${bold}${error}${sgr0}"
    }

    // Run a job, or a list of jobs in parallell; handle errors correctly
    // This might seem silly to have, but this is the *only* correct way to do it in Jenkins Pipeline
    // Read the Jenkins Pipeline confluence documentation if you wonder why
    def runJobs(jobs, cleanupJob = null, failFast = false) {
        try {
            if (jobs instanceof Collection) {
                def map = jobs.indexed()
                map.failFast = failFast
                return steps.parallel(map)
            }
            return steps.build(jobs)
        } catch (e) {
            printError e.message
            steps.currentBuild.result = 'FAILURE'

            if (cleanupJob) {
                printError "Running cleanup...", 'green'
                runJobs(cleanupJob)
            }
            throw e
        }
    }
}
