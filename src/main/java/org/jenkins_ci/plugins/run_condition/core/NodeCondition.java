/*
 * The MIT License
 *
 * Copyright (C) 2013 by Dominik Bartholdi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkins_ci.plugins.run_condition.core;

import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.ComputerSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jenkins.model.Jenkins;

import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.kohsuke.stapler.DataBoundConstructor;

public final class NodeCondition extends AlwaysPrebuildRunCondition {

    private static final String MASTER = "master";

    final List<String>          allowedNodes;

    @DataBoundConstructor
    public NodeCondition(List<String> allowedNodes) {
        this.allowedNodes = allowedNodes;
    }

    public List<String> getAllowedNodes() {
        return allowedNodes == null ? Collections.<String> emptyList() : allowedNodes;
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) {

        String currentNode = build.getExecutor().getOwner().getName();
        currentNode = "".equals(currentNode) ? MASTER : currentNode;
        listener.getLogger().println(Messages.nodeCondition_check(currentNode, Arrays.toString(getAllowedNodes().toArray())));
        return getAllowedNodes().contains(currentNode);

    }

    /**
     * returns all available nodes plus an identifier to identify all slaves at position one.
     * 
     * @return list of node names
     */
    public static List<String> getNodeNamesForSelection() {
        List<String> slaveNames = NodeConditionDescriptor.getSlaveNames();
        Collections.sort(slaveNames, NodeNameComparator.INSTANCE);
        return slaveNames;
    }

    /**
     * Comparator preferring the master name
     */
    private static final class NodeNameComparator implements Comparator<String> {
        public static final NodeNameComparator INSTANCE = new NodeNameComparator();

        public int compare(String o1, String o2) {
            if (MASTER.endsWith(o1)) {
                return -1;
            }
            return o1.compareTo(o2);
        }
    }

    @Extension
    public static class NodeConditionDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.nodeCondition_displayName();
        }

        /**
         * Gets the names of all configured slaves, regardless whether they are online.
         * 
         * @return list with all slave names
         */
        @SuppressWarnings("deprecation")
        private static List<String> getSlaveNames() {
            ComputerSet computers = Jenkins.getInstance().getComputer();
            List<String> slaveNames = computers.get_slaveNames();

            // slaveNames is unmodifiable, therefore create a new list
            List<String> test = new ArrayList<String>();
            test.addAll(slaveNames);

            // add 'magic' name for master, so all nodes can be handled the same way
            if (!test.contains(MASTER)) {
                test.add(0, MASTER);
            }
            return test;
        }

    }

}
