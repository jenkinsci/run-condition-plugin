/*
 * The MIT License
 *
 * Copyright (C) 2014 by Oleg Nenashev
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

import antlr.ANTLRException;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.AbstractProject.AbstractProjectDescriptor;
import hudson.model.AutoCompletionCandidates;
import hudson.model.BuildListener;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.model.labels.LabelExpression;
import hudson.util.FormValidation;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Allows to set conditions based on label expressions.
 * @author Oleg Nenashev <o.v.nenashev@gmail.com>
 * @since TODO: define the version 
 */
public class NodeLabelCondition extends AlwaysPrebuildRunCondition {
    
    private final String labelExpression;

    @DataBoundConstructor
    public NodeLabelCondition(final String labelExpression) {
        this.labelExpression = labelExpression;
    }

    public String getLabelExpression() {
        try {
            LabelExpression.parseExpression(labelExpression);
            return labelExpression;
        } catch (ANTLRException e) {
            // must be old label or host name that includes whitespace or other unsafe chars
            return LabelAtom.escape(labelExpression);
        }
    }

    
    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        Node node = build.getBuiltOn();
        if (node == null) { // Missing node (should never happen)
            return false;
        }
        
        final String expandedExpression = Util.fixEmptyAndTrim(TokenMacro.expandAll(build, listener, labelExpression));
        listener.getLogger().println(Messages.nodeLabelCondition_check(node.getDisplayName(), expandedExpression));
        if (expandedExpression == null) { // Empty labels
            return true;
        }
        
        Label l = Jenkins.getInstance().getLabel(expandedExpression);
        return l == null || l.contains(node);
    }

    @Extension
    public static class DescriptorImpl extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.nodeLabelCondition_displayName();
        }
        
        private @Nonnull AbstractProjectDescriptor getAbstractProjectDescriptor() {
            AbstractProjectDescriptor d = 
                    Jenkins.getInstance().getDescriptorByType(AbstractProjectDescriptor.class);
            if (d == null) {
                throw new IllegalStateException("Cannot retrieve the "+AbstractProjectDescriptor.class);
            }
            return d;
        }
        
        public FormValidation doCheckAssignedLabelExpression(@QueryParameter String value) {
            return getAbstractProjectDescriptor().doCheckAssignedLabelString(value);
        }
       
        public AutoCompletionCandidates doAutoCompleteAssignedLabelExpression(@QueryParameter String value) {
            return getAbstractProjectDescriptor().doAutoCompleteAssignedLabelString(value);
        }
    }
}
