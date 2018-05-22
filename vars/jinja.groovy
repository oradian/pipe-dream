import org.apache.commons.io.FilenameUtils

import com.oradian.pipedream.docker.Docker

def call(def paramsMap) {
    def templatePath = paramsMap["template"]
    if (!templatePath)
        throw new IllegalArgumentException("Can't call jinja step without the mandatory 'template' parameter")

    def argFile = paramsMap["argFile"]
    def dockerArgs = ""
    if (argFile)
        dockerArgs = "--env-file '${pwd()}/${argFile}'"

    def pathWithoutExtension = FilenameUtils.getFullPath(templatePath) + FilenameUtils.getBaseName(templatePath)
    def destinationPath = paramsMap["destination"] ?: pathWithoutExtension

    def _docker = Docker.create(this)
    if (_docker.isInsideDocker()) {
        println "Warning: calling jinja step while already inside docker. Hoping that image being run has the j2cli installed, and that you passed the --env-file manually (argFile parameter does nothing here)"
        sh "j2 ${templatePath} > ${destinationPath}"
    } else {
        docker.image("build_base").inside(dockerArgs) {
            sh "j2 ${templatePath} > ${destinationPath}"
        }
    }

    return destinationPath
}
