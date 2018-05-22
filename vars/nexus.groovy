import com.oradian.pipedream.Nexus

import java.nio.file.Paths

def uploadFileToRaw(String repository, String destination, String filepath = null) {
    return (new Nexus(this)).uploadFileToRaw(repository, destination, filepath)
}

def getFromRaw(String repository, String path) {
    return (new Nexus(this)).getFromRaw(repository, path)
}

def getFromRaw(String repository, String path, String destination) {
    (new Nexus(this)).getFromRaw(repository, path, destination)
}
