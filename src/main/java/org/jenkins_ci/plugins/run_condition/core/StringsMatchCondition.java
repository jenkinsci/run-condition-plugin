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

package org.jenkins_ci.plugins.run_condition.core;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.Map;
import java.util.logging.Logger;

public class StringsMatchCondition extends AlwaysPrebuildRunCondition {

    final String arg1;
    final String arg2;
    boolean ignoreCase;
    boolean environmentVariables;

    @DataBoundConstructor
    public StringsMatchCondition(final String arg1, final String arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;


    }
    @DataBoundSetter
    public void setIgnoreCase(Boolean ignoreCase){

        this.ignoreCase=ignoreCase;
    }
    @DataBoundSetter
    public void setEnvironmentVariables(Boolean enviornmentVariables){

        this.environmentVariables=enviornmentVariables;
    }

    public boolean isEnvironmentVariables() {
        return environmentVariables;
    }

    public String getArg2() {
        return arg2;
    }

    public String getArg1() {
        return arg1;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {

        if(environmentVariables == true){

             final String expanded1 = TokenMacro.expandAll(build, listener, "$"+"{"+arg1+"}");
             final String expanded2 = TokenMacro.expandAll(build, listener, "$"+"{"+arg2+"}");

             listener.getLogger().println(Messages.stringsMatchCondition_console_args(expanded1, expanded2));
             if (expanded1 == null) return false;
             return ignoreCase ? expanded1.equalsIgnoreCase(expanded2) : expanded1.equals(expanded2);
        }
        else {
            final String expanded1 = TokenMacro.expandAll(build, listener, arg1);
            final String expanded2 = TokenMacro.expandAll(build, listener, arg2);


            listener.getLogger().println(Messages.stringsMatchCondition_console_args(expanded1, expanded2));
            if (expanded1 == null) return false;
            return ignoreCase ? expanded1.equalsIgnoreCase(expanded2) : expanded1.equals(expanded2);
        }
    }

    @Extension
    public static class StringsMatchDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.stringsMatchCondition_displayName();
        }

    }

}
