package com.oradian.pipedream.integration

import org.jenkinsci.plugins.workflow.cps.CpsScript

class IntegrationConfiguration implements Serializable {
    static Integration getProvider(CpsScript step) {
        // TODO: do git remote URL matching.
        return Github.create(step)
    }
}
