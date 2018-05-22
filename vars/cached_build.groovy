import java.util.Collection;

import com.oradian.pipedream.caching.CacheEntry;
import com.oradian.pipedream.caching.CacheCollection;
import com.oradian.pipedream.caching.CacheConfiguration;

def call(CacheCollection cc, def lambda) {
    def config = new CacheConfiguration(this)
    if (config.ignoreCache) {
        echo "Ignoring build cache as requested!"
        cc.ignoreCache()
    }

    // Compatibility check - remove later
    def params = lambda.parameterTypes
    def compat = params.length == 0 || params[0] != CacheCollection.class

    cc.updateStatus()
    if (cc.needBuild) {
        if (compat) lambda(cc.sha1)
        else lambda(cc)

        cc.updateCache()
    } else {
        echo "Using cached build ${cc.sha1}!"
    }

    return cc
}

def call(Collection<String> filepaths, def lambda) {
    def cc = CacheCollection.fromFiles(this, filepaths)
    return cached_build(cc, lambda)
}

def call(String path = null, boolean chdir = false, def lambda) {
    if (!chdir)
        return cached_build([path], lambda)

    return cached_build([path], { cc ->
        dir(path) {
            lambda(cc)
        }
    })
}

def call(String prefix, String key, def lambda) {
    def entry = CacheEntry.create(this, prefix, key)
    def cc = CacheCollection.create(this, [entry])

    return cached_build(cc, lambda)
}

def getSha1(String filepath = null) {
    return getSha1([filepath])
}

def getSha1(Collection<String> filepaths) {
    return CacheCollection.fromFiles(this, filepaths).sha1
}
