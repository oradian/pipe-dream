package com.oradian.pipedream.docker

import com.oradian.pipedream.Execution
import com.oradian.pipedream.NodeUtil

import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.docker.workflow.ImageNameTokens

import java.util.logging.Logger

// TODO: this is copypasta from https://github.com/jenkinsci/docker-workflow-plugin/blob/master/src/main/resources/org/jenkinsci/plugins/docker/workflow/Docker.groovy.
class Image implements Serializable {
    private static Logger logger = Logger.getLogger(Image.class.name)

    CpsScript step;
    public String id
    private ImageNameTokens parsedId
    private Execution execution

    private Image(CpsScript step, String id) {
        this.step = step
        this.id = id
        this.parsedId = new ImageNameTokens(id)
    }

    static Image create(CpsScript step, String id) {
        def self = new Image(step, id)

        // Cannot use constructor, too complicated - CPS restricts it, @NonCPS doesn't work
        def nu = new NodeUtil(step)
        self.execution = Execution.create(step, nu.node, true)

        return self
    }

    private String toQualifiedImageName(String imageName) {
        return new org.jenkinsci.plugins.docker.commons.credentials.DockerRegistryEndpoint(step.env.DOCKER_REGISTRY_URL, null).imageName(imageName)
    }

    public def tag(String tagName = parsedId.tag, boolean force = true) {
        def taggedImageName = toQualifiedImageName(parsedId.userAndRepo + ':' + tagName)
        // TODO as of 1.10.0 --force is deprecated; for 1.12+ do not try it even once
        def status = execution.run("docker tag --force=${force} ${id} ${taggedImageName}")
        if (status != 0) {
            status = execution.run("docker tag ${id} ${taggedImageName}")
        }
        if (status != 0) {
            throw new Exception("Couldn't tag Docker image '${id}' as ${taggedImageName}: ${e.stderr}")
        }
        return taggedImageName;
    }

    public def push(String tagName = parsedId.tag, boolean force = true) {
        // The image may have already been tagged, so the tagging may be a no-op.
        // That's ok since tagging is cheap.
        def taggedImageName = tag(tagName, force)
        step.println("Pushing ${taggedImageName}")
        logger.fine("Pushing ${taggedImageName}")
        def status = execution.run("docker push ${taggedImageName}")
        if (status != 0) {
            throw new Exception("Couldn't push Docker image '${taggedImageName}'")
        }
    }

    // Not a copypasta.
    // Convert this fake image to a real Docker.Image object
    public def toDockerImage() {
        def docker = Docker.create(step)
        if (docker.isInsideContainer())
            step.println "WARNING: converting a fake ${this.getClass().getName()} to a real Docker.Image. Most functions probably won't work while still inside of Docker container"

        return step.docker.image(this.id)
    }
}
