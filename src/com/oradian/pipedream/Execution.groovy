package com.oradian.pipedream

import hudson.Launcher
import hudson.model.Node
import hudson.util.StreamTaskListener

import org.jenkinsci.plugins.workflow.cps.CpsScript

import java.util.logging.Logger

// Use Jenkins internals to execute shell commands on a specified Node
class Execution implements Serializable {
    private static Logger logger = Logger.getLogger(Execution.class.name)

    private CpsScript step
    private TaskListener listener
    private transient Launcher launcher
    private Node node
    private boolean debug

    private Execution(CpsScript step, boolean debug = null) {
        this.step = step
        this.debug = debug != null ? debug : step.env.DEBUG_EXECUTION
    }

    // Cannot use constructor, too complicated - CPS restricts it, @NonCPS doesn't work
    static Execution create(CpsScript step, Node node, boolean debug = null) {
        def self = new Execution(step, debug)
        self.listener = step.getContext(TaskListener.class)
        self.launcher = node.createLauncher(self.listener)
        self.node = node

        return self
    }

    private int runWith(Closure<Launcher.ProcStarter> lambda) {
        // TODO: logger doesn't work
        def log = listener.logger

        def pwd = step.pwd()
        def environment = step.currentBuild.rawBuild.getEnvironment(listener)

        def ps = launcher.launch()
        ps = ps.pwd(pwd).envs(environment)
        if (debug)
            ps = ps.stdout(log).stderr(log)

        ps = lambda(ps)
        return ps.start().join()
    }

    int run(String command) {
        def ret = runWith{ ps -> ps.cmdAsSingleString(command) }
        logger.fine "Running '$command' on ${node.nodeName} ended with status $ret"
        return ret
    }
}
