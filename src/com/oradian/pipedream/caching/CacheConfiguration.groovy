package com.oradian.pipedream.caching

import com.oradian.pipedream.caching.storage.CacheStorage
import com.oradian.pipedream.caching.storage.NexusCache

import com.oradian.pipedream.caching.hash.FilesystemHasher
import com.oradian.pipedream.caching.hash.GitHasher

import org.jenkinsci.plugins.workflow.cps.CpsScript;

class CacheConfiguration implements Serializable {
    private CpsScript step

    CacheConfiguration(CpsScript step) {
        this.step = step
    }

    def getCacheRepository() {
        step.env.NEXUS_CACHE_REPOSITORY ?: "build_cache"
    }

    def getIgnoreCache() {
        step.env.BUILD_IGNORE_CACHE ?: false
    }

    CacheStorage getStorage() {
        def storage = step.env.CACHE_STORAGE ?: 'nexus'
        switch (storage) {
            case 'nexus':
                return new NexusCache(step)

            default:
                throw new Exception("Caching storage '${storage}' not implemented! Check your CACHE_STORAGE environment.")
        }
    }

    FilesystemHasher getHasher() {
        def hasher = step.env.CACHE_HASHER ?: 'git'
        switch (hasher) {
            case 'git':
                return new GitHasher(step)

            default:
                throw new Exception("Cache hasher '${hasher}' not implemented! Check your CACHE_HASHER environment.")
        }
    }
}
