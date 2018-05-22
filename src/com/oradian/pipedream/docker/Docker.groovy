package com.oradian.pipedream.docker

import com.oradian.pipedream.Execution;
import com.oradian.pipedream.NodeUtil

import org.jenkinsci.plugins.workflow.cps.CpsScript;

import groovy.transform.Memoized
import java.util.logging.Logger

class Docker implements Serializable {
    private static Logger logger = Logger.getLogger(Docker.class.name)

    private CpsScript step;
    private Execution execution;

    private Docker(CpsScript step) {
        this.step = step
    }

    // Cannot use constructor, too complicated - CPS restricts it, @NonCPS doesn't work
    static Docker create(CpsScript step) {
        def self = new Docker(step)

        def nu = new NodeUtil(step)
        self.execution = Execution.create(step, nu.node, true)

        return self
    }

    //@Memoized
    def getContainerId() {
        // Hostname inside of docker container returns its ID
        def hostname = step.sh(script: 'hostname', returnStdout: true).trim()

        // Check if a hostname is a valid docker container
        def status = execution.run("docker inspect ${hostname}")
        if (status == 0)
            return hostname

        return null
    }

    def isInsideDocker() {
        return containerId != null
    }

    // Commit inside the docker container
    def commit(String image) {
        if (!isInsideDocker()) {
            throw new Exception("Cannot call commit() without arguments outside of Docker container!")
        }

       return commit(containerId, image)
    }

    def commit(String id, String image) {
        logger.finest "Commiting docker image $id as $image"
        def status = execution.run("docker commit ${id} ${image}")
        if (status != 0)
            throw new Exception("Couldn't commit Docker container '${id}' as ${image}: ${e.stderr}")

        if (isInsideDocker())
            return Image.create(step, image)

        return step.docker.image(image)
    }
}
