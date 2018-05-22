package com.oradian.pipedream.caching.hash

import org.apache.commons.io.FilenameUtils
import org.jenkinsci.plugins.workflow.cps.CpsScript;

class GitHasher implements FilesystemHasher {
    private CpsScript step

    GitHasher(CpsScript step) {
        this.step = step
    }

    def getSha1InCWD(String file = null) {
        String sha1 = ""
        if (file == null)
            sha1 = step.sh script: "git rev-parse HEAD", returnStdout: true
        else
            sha1 = step.sh script: "git ls-tree HEAD | awk '\$4 == \"${file}\" { print \$3 }'", returnStdout: true

        sha1 = sha1.trim()
        if (sha1.length() == 0)
            throw new IllegalArgumentException("Can't find file ${file} or not inside .git repository (cwd = ${step.pwd()})")

        return sha1
    }

    def getSha1(String path = null) {
        def dirname = FilenameUtils.getFullPath(path)
        def basename = FilenameUtils.getName(path)

        if (dirname == null || dirname == "")
            return getSha1InCWD(path)

        step.dir(dirname) {
            return getSha1InCWD(basename)
        }
    }
}
