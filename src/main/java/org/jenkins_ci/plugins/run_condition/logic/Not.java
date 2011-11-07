/*
 * The MIT License
 *
 * Copyright (C) 2011 by Anthony Robinson
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

package org.jenkins_ci.plugins.run_condition.logic;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

public class Not extends RunCondition {

    private final RunCondition condition;
    
    @DataBoundConstructor
    public Not(final RunCondition condition) {
        this.condition = condition;
    }

    public RunCondition getCondition() {
        return condition;
    }

    @Override
    public boolean runPrebuild(final AbstractBuild<?, ?> build, final BuildListener listener) {
        return !condition.runPrebuild(build, listener);
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) {
        return !condition.runPerform(build, listener);
    }

    @Extension(ordinal = -1000002)
    public static class NotDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.logic_not_displayName();
        }

        public List<? extends Descriptor<? extends RunCondition>> getRunConditions() {
            return RunCondition.all();
        }

    }

}
