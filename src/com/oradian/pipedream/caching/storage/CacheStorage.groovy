package com.oradian.pipedream.caching.storage

interface CacheStorage extends Serializable {
    String getUrl(String path)

    Boolean get(String path)
    void set(String path)
}
