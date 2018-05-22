package com.oradian.pipedream.caching

import com.oradian.pipedream.caching.storage.CacheStorage

import org.jenkinsci.plugins.workflow.cps.CpsScript

import groovy.transform.Memoized
import java.util.logging.Logger

// @Lazy properties don't work under Jenkins' modified Groovy, so we use memoized getters instead
class CacheEntry implements Serializable {
    private static Logger logger = Logger.getLogger(CacheEntry.class.name)

    //@Memoized // Can't be a property because of a CPS
    private CacheStorage getStorage() {
        def config = new CacheConfiguration(step)
        config.storage
    }

    private CpsScript step

    // These methods have to be @NonCPS because they are used in a sort at CacheCollection
    private String key
    @NonCPS String getKey() { key }

    private String prefix
    @NonCPS String getPrefix() { prefix }

    @NonCPS
    def getPath() { "${key}/${prefix}" }

    //@Memoized
    boolean getIsCached() {
        def result = storage.get(path)
        logger.finer "Get cache $path: $result"
        result
    }

    static fromFile(CpsScript step, String path = null) {
        def hasher = (new CacheConfiguration(step)).hasher

        def key = hasher.getSha1(path)
        def prefix = path ?: "project"

        return CacheEntry.create(step, prefix, key)
    }

    static create(CpsScript step, String prefix, String key) {
        def self = new CacheEntry([step: step, prefix: prefix, key: key])
        logger.finest "New CacheEntry ${self.path}"
        return self
    }

    def updateCache() {
        logger.finer "Updating cache ${path}"
        storage.set(path)
    }

    def updateStatus() {
        def url = storage.getUrl(path)

        if (!step.currentBuild.description)
            step.currentBuild.description = ''

        def action = isCached ? "used" : "<b>invalidated</b>"
        def link = url ? "<a target='_blank' href='${url}'>${prefix}</a>" : prefix

        step.currentBuild.description += "Cache entry $action $link<br />"
    }
}
