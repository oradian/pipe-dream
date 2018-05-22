package com.oradian.pipedream.caching

import com.oradian.pipedream.Hash;

import org.jenkinsci.plugins.workflow.cps.CpsScript;

import java.util.Collection;
import java.util.logging.Logger;
import groovy.transform.Memoized;

class CacheCollection implements Serializable {
    private static Logger logger = Logger.getLogger(CacheCollection.class.name)

    private CpsScript step

    private Collection<CacheEntry> entries
    def getEntries() { return entries }

    private boolean needBuild
    def getNeedBuild() { return needBuild }
    def getCached()    { return !needBuild }
    def ignoreCache()  { this.needBuild = true }

    private String sha1
    def getSha1() { return sha1 }

    private CacheCollection(CpsScript step) {
        this.step = step
    }

    public static fromFiles(CpsScript step, Collection<String> filepaths) {
        def entries = filepaths.collect{CacheEntry.fromFile(step, it)}

        return CacheCollection.create(step, entries)
    }

    // Cannot use constructor, too complicated - CPS restricts it, @NonCPS doesn't work
    public static create(CpsScript step, Collection<CacheEntry> entries) {
        def self = new CacheCollection(step)
        self.entries = entries
        self.sort()

        def sha1Concat = self.entries.collect{it.path}.join()
        self.sha1 = Hash.sha1(step).fromString(sha1Concat).toHex()

        self.entries += CacheEntry.create(step, 'combined_cache', self.sha1)
        self.needBuild = !self.entries.every{it.isCached}

        logger.fine "Combined cache ${self.sha1} needs build: ${self.needBuild}"
        return self
    }

    //@Memoized
    def updateCache() {
        logger.fine "Update combined ${sha1}"
        entries.each{it.updateCache()}
    }

    //@Memoized
    def updateStatus() {
        entries.each{it.updateStatus()}
    }

    // I can't explain enough how much Jenkins' modified Groovy is retarded
    @NonCPS
    private def sort() {
        // To give an explanation that's usefull... Sort doesn't work if not in @NonCPS function... Static functions and constructors can't have @NonCPS (that's why it's a separate function)... Also, not only sort must be in @NonCPS function, everything that this line of code invokes must be in @NonCPS, so... CacheEntry's getPath and all methods it invokes...which means also CacheEntry's getKey and getPrefix... true parameter makes it mutate the original list, otherwise (by default) it would do nothing
        entries.sort(true) {it.path}
    }
}
