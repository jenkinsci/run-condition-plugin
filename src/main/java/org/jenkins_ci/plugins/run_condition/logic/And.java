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

import java.util.ArrayList;
import java.util.List;

public class And extends RunCondition {

    private final ArrayList<ConditionContainer> conditions;
    
    @DataBoundConstructor
    public And(final ArrayList<ConditionContainer> conditions) {
        this.conditions = conditions;
    }

    public ArrayList<ConditionContainer> getConditions() {
        return conditions;
    }

    @Override
    public boolean runPrebuild(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        for (ConditionContainer condition : conditions)
            if (!condition.runPrebuild(build, listener))
                return false;
        return true;
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        for (ConditionContainer condition : conditions)
            if (!condition.runPerform(build, listener))
                return false;
        return true;
    }

    @Extension(ordinal = -1000000)
    public static class AndDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.logic_and_displayName();
        }

    }

}
