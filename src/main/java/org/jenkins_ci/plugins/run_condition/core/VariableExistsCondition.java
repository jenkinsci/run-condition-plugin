/*
 * The MIT License
 *
 * Copyright (C) 2013 by Oleg Nenashev, Synopsys Inc.
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
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

public class VariableExistsCondition extends AlwaysPrebuildRunCondition {

    private String variableName;

    @DataBoundConstructor
    public VariableExistsCondition(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }
    
    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        final String varName = TokenMacro.expandAll(build, listener, variableName);
        listener.getLogger().println(Messages.variableExistsCondition_check(varName));
        return build.getEnvironment(listener).containsKey(varName);
    }

    @Extension
    public static class StringsMatchDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.variableExistsCondition_displayName();
        }
    }
}
