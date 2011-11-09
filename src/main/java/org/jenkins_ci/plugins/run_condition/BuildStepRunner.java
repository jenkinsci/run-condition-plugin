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

package org.jenkins_ci.plugins.run_condition;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.tasks.BuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enable the user to choose what happens when an exception occurs when evaluating a condition.
 */
public abstract class BuildStepRunner implements Describable<BuildStepRunner> {

    private static final Logger LOGGER = Logger.getLogger(BuildStepRunner.class.getName());

    public static DescriptorExtensionList<BuildStepRunner, BuildStepRunnerDescriptor> all() {
        return Hudson.getInstance().<BuildStepRunner, BuildStepRunnerDescriptor>getDescriptorList(BuildStepRunner.class);
    }

    private static void logEvaluateException(final BuildListener listener, final Exception e, final String displayName) {
        final String msg = Messages.runner_console_exception(e.getClass().getName() + ": " + e.getLocalizedMessage(), displayName);
        LOGGER.log(Level.WARNING, msg);
        listener.getLogger().println(msg);
    }

    private static void setResult(final AbstractBuild<?, ?> build, final Result result) {
        if (build.getResult() == null)
            build.setResult(result);
        else
            build.setResult(result.combine(build.getResult()));
    }

    private static String getDisplayName(final Describable describable) {
        return describable.getDescriptor().getDisplayName();
    }

    public abstract boolean conditionalRun(final ConditionAndStep target, final AbstractBuild<?, ?> build, final BuildListener listener)
                                           throws IOException, InterruptedException;

    public final boolean prebuild(final RunCondition condition, final BuildStep buildStep, final AbstractBuild<?, ?> build,
                            final BuildListener listener) {
        try {
            return conditionalRun(new ConditionAndStep() {
                public boolean evaluate() throws Exception {
                    return condition.runPrebuild(build, listener);
                }
                public boolean run() {
                    return buildStep.prebuild(build, listener);
                }
                public void logRunning(final boolean running) {
                    if (running) {
                        listener.getLogger().println(Messages.runner_condition_true(getDisplayName(condition),
                                                                Messages.runner_stage_prebuild(), getDisplayName((Describable) buildStep)));
                    } else {
                        listener.getLogger().println(Messages.runner_condition_false(getDisplayName(condition),
                                                                Messages.runner_stage_prebuild(), getDisplayName((Describable) buildStep)));
                    }
            }
            }, build, listener);
        // these should not be possible as the Exceptions during evaluate will be handled or converted to RE, but need to enable
        // buildStep.perform to be able to throw these exceptions
        } catch (final IOException ioe) {
        } catch (final InterruptedException ie) {
        }
        // should not get here
        return false;
    }

    public final boolean perform(final RunCondition condition, final BuildStep buildStep, final AbstractBuild<?, ?> build,
                           final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        return conditionalRun(new ConditionAndStep() {
            public boolean evaluate() throws Exception {
                return condition.runPerform(build, listener);
            }
            public boolean run() throws IOException, InterruptedException {
                return buildStep.perform(build, launcher, listener);
            }
            public void logRunning(final boolean running) {
                if (running) {
                    listener.getLogger().println(Messages.runner_condition_true(getDisplayName(condition), Messages.runner_stage_perform(),
                                                                                                getDisplayName((Describable) buildStep)));
                } else {
                    listener.getLogger().println(Messages.runner_condition_false(getDisplayName(condition),
                                                                Messages.runner_stage_perform(), getDisplayName((Describable) buildStep)));
                }
            }
        }, build, listener);
    }

    public BuildStepRunnerDescriptor getDescriptor() {
        return (BuildStepRunnerDescriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    public static abstract class BuildStepRunnerDescriptor extends Descriptor<BuildStepRunner> {
        protected BuildStepRunnerDescriptor() {}
    }

    private static interface ConditionAndStep {
        boolean evaluate() throws Exception;
        boolean run() throws IOException, InterruptedException;
        void logRunning(boolean running);
    }

    public static class Fail extends BuildStepRunner {

        @DataBoundConstructor public Fail() {}

        @Override
        public boolean conditionalRun(final ConditionAndStep target, final AbstractBuild<?, ?> build, final BuildListener listener)
                                      throws IOException, InterruptedException {
            boolean run = false;
            try {
                run = target.evaluate();
                target.logRunning(run);
            } catch (final Exception e) {
                logEvaluateException(listener, e, Messages.runner_fail_displayName());
                setResult(build, Result.FAILURE);
                return false;
            }
            return run ? target.run() : true;
        }

        @Extension(ordinal = 0)
        public static class FailDescriptor extends BuildStepRunnerDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.runner_fail_displayName();
            }
        }

    }

    public static class Unstable extends BuildStepRunner {

        @DataBoundConstructor public Unstable() {}

        @Override
        public boolean conditionalRun(final ConditionAndStep target, final AbstractBuild<?, ?> build, final BuildListener listener)
                                      throws IOException, InterruptedException {
            boolean run = false;
            try {
                run = target.evaluate();
                target.logRunning(run);
            } catch (final Exception e) {
                logEvaluateException(listener, e, Messages.runner_unstable_displayName());
                setResult(build, Result.UNSTABLE);
            }
            return run ? target.run() : true;
        }

        @Extension(ordinal = -1)
        public static class UnstableDescriptor extends BuildStepRunnerDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.runner_unstable_displayName();
            }
        }

    }

    public static class RunUnstable extends BuildStepRunner {

        @DataBoundConstructor public RunUnstable() {}

        @Override
        public boolean conditionalRun(final ConditionAndStep target, final AbstractBuild<?, ?> build, final BuildListener listener)
                                      throws IOException, InterruptedException {
            boolean run = true;
            try {
                run = target.evaluate();
                target.logRunning(run);
            } catch (final Exception e) {
                logEvaluateException(listener, e, Messages.runner_runUnstable_displayName());
                setResult(build, Result.UNSTABLE);
            }
            return run ? target.run() : true;
        }

        @Extension(ordinal = -2)
        public static class RunUnstableDescriptor extends BuildStepRunnerDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.runner_runUnstable_displayName();
            }
        }

    }

    public static class Run extends BuildStepRunner {

        @DataBoundConstructor public Run() {}

        @Override
        public boolean conditionalRun(final ConditionAndStep target, final AbstractBuild<?, ?> build, final BuildListener listener)
                                      throws IOException, InterruptedException {
            boolean run = true;
            try {
                run = target.evaluate();
                target.logRunning(run);
            } catch (final Exception e) {
                logEvaluateException(listener, e, Messages.runner_run_displayName());
            }
            return run ? target.run() : true;
        }

        @Extension(ordinal = -3)
        public static class RunDescriptor extends BuildStepRunnerDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.runner_run_displayName();
            }
        }

    }

    public static class DontRun extends BuildStepRunner {

        @DataBoundConstructor public DontRun() {}

        @Override
        public boolean conditionalRun(final ConditionAndStep target, final AbstractBuild<?, ?> build, final BuildListener listener)
                                      throws IOException, InterruptedException {
            boolean run = false;
            try {
                run = target.evaluate();
                target.logRunning(run);
            } catch (final Exception e) {
                logEvaluateException(listener, e, Messages.runner_dontRun_displayName());
            }
            return run ? target.run() : true;
        }

        @Extension(ordinal = -4)
        public static class DontRunDescriptor extends BuildStepRunnerDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.runner_dontRun_displayName();
            }
        }

    }

}

