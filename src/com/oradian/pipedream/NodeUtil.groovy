package com.oradian.pipedream

import hudson.model.Node;
import hudson.model.Computer;
import hudson.model.Label;

import org.jenkinsci.plugins.workflow.cps.CpsScript;

class NodeUtil implements Serializable {
    private transient Node node
    def getNode() { return node }

    private transient Computer computer
    def getComputer() { return computer }

    NodeUtil(CpsScript step) {
        computer = step.getContext(Computer.class)
        if (computer != null)
            this.node = computer.node
    }

    boolean isInsideNode() {
        return node != null
    }

    boolean hasLabel(String label) {
        return hasLabel(Label.parseExpression(label))
    }

    boolean hasLabel(Label label) {
        if (node)
            return label.matches(node.assignedLabels)

        return false
    }
}
