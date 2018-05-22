package com.oradian.pipedream

import hudson.util.VersionNumber

class Plugin {
    static compareVersion(Closure<Boolean> predicate, VersionNumber version) {
        def plugins = Jenkins.instance.pluginManager.plugins
        def plugin = plugins.find(predicate)
        if (plugin == null)
            return null

        return plugin.versionNumber.compareTo(version)
    }

    static compareVersion(Closure<Boolean> predicate, String version) {
        return Plugin.compareVersion(predicate, new VersionNumber(version))
    }

    static compareVersion(String shortName, String version) {
        return compareVersion({ it.shortName == shortName }, version)
    }
}
