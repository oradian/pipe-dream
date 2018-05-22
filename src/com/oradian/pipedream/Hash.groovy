package com.oradian.pipedream

import hudson.FilePath
import org.jenkinsci.plugins.workflow.cps.CpsScript

import java.security.MessageDigest
import java.math.BigInteger
import java.util.logging.Logger

class Hash implements Serializable {
    private static Logger logger = Logger.getLogger(Hash.class.name)

    private CpsScript step;
    private MessageDigest md;

    // {{{ Construction - Algorithm choice

    Hash(CpsScript step, String algo) {
        this.step = step
        this.md = MessageDigest.getInstance(algo)
    }

    static def sha1(CpsScript step) {
        return new Hash(step, "SHA1")
    }

    // }}}

    // {{{ Digest update

    Hash fromBytes(byte[] input, int off = 0, int len = -1) {
        this.md.update(input, off, len < 0 ? input.length : len)
        return this
    }

    Hash fromInputStream(InputStream is) {
        def buffer = new byte[4*1024]
        int n

        // Stupid retarded Jenkins Groovy - don't try to make this while loop a single line
        // Hint hint... IT WORKS *PARTIALLY*!!!
        while (true) {
            n = is.read(buffer)
            if (n == -1) break
            fromBytes(buffer, 0, n)
        }
        return this
    }

    Hash fromString(String input) {
        logger.finer "Hashing string '$input'"
        return fromBytes(input.getBytes())
    }

    Hash fromFile(String path) {
        logger.finer "Hashing file $path"
        def fp = step.getContext(FilePath.class)
        def is = fp.child(path).read()
        return fromInputStream(is)
    }

    // }}}

    // {{{ Digesting and formatting

    byte[] toBytes() {
        return this.md.digest()
    }

    String toHex() {
        def bytes = toBytes()
        def result = new BigInteger(1, bytes).toString(16)
        logger.finer "Hash result: $result"
        return result
    }

    // }}}
}
