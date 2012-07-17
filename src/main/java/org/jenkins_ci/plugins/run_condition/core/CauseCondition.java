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
import hudson.cli.BuildCommand.CLICause;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Cause.RemoteCause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserCause;
import hudson.triggers.SCMTrigger.SCMTriggerCause;
import hudson.triggers.TimerTrigger.TimerTriggerCause;
import hudson.util.ListBoxModel;

import java.util.List;

import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.kohsuke.stapler.DataBoundConstructor;

public final class CauseCondition extends AlwaysPrebuildRunCondition {

    private enum BuildCause {

        USER_CAUSE(UserCause.class, "UserCause") {
            @Override
            public boolean isCausedBy(String className) {
                // if jenkins is greater than 1.427 we need UserIdCause
                return this.causeClassName.equals(className) || "hudson.model.Cause$UserIdCause".equals(className);
            }
        },
        // triggered by command line
        CLI_CAUSE(CLICause.class, "CLICause"),
        // remote call
        REMOTE_CAUSE(RemoteCause.class, "RemoteCause"),
        // change on scm
        SCM_CAUSE(SCMTriggerCause.class, "SCMTrigger"),
        // timer launched job
        TIMER_CAUSE(TimerTriggerCause.class, "TimerTrigger"),
        // upstream job has triggered
        UPSTREAM_CAUSE(UpstreamCause.class, "UpstreamCause"),

        // if XTrigger plugin is installed:
        // file change
        FS_CAUSE("org.jenkinsci.plugins.fstrigger.FSTriggerCause", "FSTrigger"),
        // url change
        URL_CAUSE("org.jenkinsci.plugins.urltrigger.URLTriggerCause", "URLTrigger"),
        // ivy is calling
        IVY_CAUSE("org.jenkinsci.plugins.ivytrigger.IvyTriggerCause", "IvyTrigger"),
        // a user script
        SCRIPT_CAUSE("org.jenkinsci.plugins.scripttrigger.ScriptTriggerCause", "ScriptTrigger"),
        // a specific build result
        BUILDRESULT_CAUSE("org.jenkinsci.plugins.buildresulttrigger.BuildResultTriggerCause", "BuildResultTrigger");

        public final String causeClassName;
        public final String displayName;

        /**
         * Constructor to build causes the cause class is NOT available at build time.
         *
         * @param causeClassName
         * @param displayName
         */
        private BuildCause(String causeClassName, String displayName) {
            this.causeClassName = causeClassName;
            this.displayName = displayName;
        }

        /**
         * allow enum definition to overwrite
         *
         * @param className
         * @return true if this cause is meant by the given className
         */
        boolean isCausedBy(String className) {
            return this.causeClassName.equals(className);
        }

        /**
         * Constructor to build causes the cause class is available at build time.
         *
         * @param clazz
         *            cause class
         * @param displayName
         *            the name to display in the UI
         */
        private BuildCause(Class<? extends Cause> clazz, String displayName) {
            this.causeClassName = clazz.getName();
            this.displayName = displayName;
        }
    }

    private final BuildCause buildCause;
    private final boolean exclusiveCause;

    @DataBoundConstructor
    public CauseCondition(final String buildCause, final boolean exclusiveCause) {
        this.buildCause = BuildCause.valueOf(buildCause);
        this.exclusiveCause = exclusiveCause;
    }

    public BuildCause getBuildCause() {
        return buildCause;
    }

    public boolean isExclusiveCause() {
        return exclusiveCause;
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) {
        final List<Cause> causes = build.getRootBuild().getCauses();
        if (buildCause != null) {
            if (isExclusiveCause()) {
                return causes.size() == 1 && buildCause.isCausedBy(causes.get(0).getClass().getName());
            } else {
                for (Cause cause : causes) {
                    if (buildCause.isCausedBy(cause.getClass().getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Extension
    public static class CauseConditionDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.causeCondition_displayName();
        }

        public ListBoxModel doFillBuildCauseItems() {
            ListBoxModel items = new ListBoxModel();
            for (BuildCause cause : BuildCause.values()) {
                items.add(new ListBoxModel.Option(cause.displayName, cause.name()));
            }
            return items;
        }

    }

}
