import java.io.File

def call(def paramsMap = [:]) {
    def directory = paramsMap["directory"] ?: ""
    def version   = paramsMap["version"]   ?: "9.6"
    def template  = paramsMap["template"]  ?: "postgresql.conf.j2"
    def argfile   = paramsMap["argfile"]   ?: "argfile"
    def name      = paramsMap["name"]      ?: "pgdata"

    stage("Run Postgresql") {
        dir(directory) {
            def PWD = pwd()
            def datadir = pwd(tmp: true) + "/$name"
            sh "mkdir -p '${datadir}'"

            def config = jinja template: template, argFile: "argfile"

            def uid = sh(script: "id -u", returnStdout: true).trim()
            def gid = sh(script: "id -g", returnStdout: true).trim()
            def args = "--env-file $PWD/$argfile --expose 5432 -v ${PWD}:/docker-entrypoint-initdb.d:ro -v $datadir:/var/lib/postgresql/data --shm-size=512M -u $uid:$gid -v /etc/passwd:/etc/passwd:ro"

            return docker.image("postgres:${version}-alpine").run(args)
        }
    }
}

def withRun(def paramsMap = [:], def lambda) {
    def container = null
    try {
        container = postgres(paramsMap)
        lambda(container.id)
    } finally {
        if (container)
            container.stop()
    }
}
