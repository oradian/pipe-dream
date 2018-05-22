package com.oradian.pipedream.caching.hash

import org.jenkinsci.plugins.workflow.cps.CpsScript;

interface FilesystemHasher extends Serializable {
    def getSha1(String path)
}
