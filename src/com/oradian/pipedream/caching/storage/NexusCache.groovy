package com.oradian.pipedream.caching.storage

import com.oradian.pipedream.Nexus
import com.oradian.pipedream.caching.CacheConfiguration

import org.jenkinsci.plugins.workflow.cps.CpsScript

import groovy.transform.Memoized
import java.nio.file.InvalidPathException

class NexusCache implements CacheStorage {
    private CpsScript step
    private Nexus nexus
    private CacheConfiguration config

    NexusCache(CpsScript step) {
        this.step = step
        this.nexus = new Nexus(step)
        this.config = new CacheConfiguration(step)
    }

    String getUrl(String path) {
        return "${nexus.nexusUrl}/#browse/search/custom=name.raw=${path} AND repository_name=${config.cacheRepository}"
    }

    //@Memoized
    Boolean get(String path) {
        def result = true
        try {
            nexus.getFromRaw(config.cacheRepository, path)
        } catch (InvalidPathException e) {
            step.println "Found uncached build ${path}..."
            result = false
        }

        return result
    }

    void set(String path) {
        nexus.uploadFileToRaw(config.cacheRepository, path)
    }
}
