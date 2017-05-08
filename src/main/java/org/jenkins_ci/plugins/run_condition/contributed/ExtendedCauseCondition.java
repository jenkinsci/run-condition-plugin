/*
 * The MIT License
 *
 * Copyright (C) 2012 by Chris Johnson
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

package org.jenkins_ci.plugins.run_condition.contributed;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserCause;
import hudson.model.*;
import hudson.util.QuotedStringTokenizer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;



/**
 * Run condition to use the reason that the build was started,
 * the actual conditions are subclasses that can be extended using the BuildCauseCondition
 *
 * @author Chris Johnson
 */
public class ExtendedCauseCondition extends AlwaysPrebuildRunCondition {

    private BuildCauseCondition condition;
    private boolean exclusiveCause;
    /**
     * Data bound constructor taking a condition and exclusive cause
     *
     * @param condition         Build condition to use
     * @param exclusiveCause    flag to indicate whether builds started by multiple causes are allowed.
     */
    @DataBoundConstructor
    public ExtendedCauseCondition(BuildCauseCondition condition, boolean exclusiveCause) {
        this.condition = condition;
        this.exclusiveCause = exclusiveCause;
    }

    /**
     * Returns the condition for the UI to display
     *
     * @return Condition that is current.
     */
    public BuildCauseCondition getCondition() {
        return condition;
    }

    /**
     * Returns the exclusiveCause for the UI to display
     *
     * @return true if only a single cause can trigger this condition.
     */
    public boolean isExclusiveCause() {
        return exclusiveCause;
    }

    /**
     * Performs the check of the condition and exclusiveCause.
     *
     * @return false if more than single cause for the build
     *         Otherwise the result of the condition runPerform @see BuildCauseCondition
     */
    @Override
    public boolean runPerform(AbstractBuild<?, ?> build, BuildListener listener) throws InterruptedException {
        if (condition != null) {
            if(isExclusiveCause() && build.getCauses().size() == 1 || !isExclusiveCause())
                return condition.runPerform(build, listener);
        }
        return false;
    }

    @Extension
    public static class ExtendedCauseConditionDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.ExtendedCauseCondition_DisplayName();
        }

        public List<? extends Descriptor<? extends BuildCauseCondition>> getBuildCauses() {
            return Hudson.getInstance().<BuildCauseCondition, BuildCauseConditionDescriptor>getDescriptorList(BuildCauseCondition.class);
        }
    }

    /**
     * Abstract Build Cause condition that checks the build condition
     *
     * @author Chris Johnson
     */
    public static abstract class BuildCauseCondition implements Describable<BuildCauseCondition>, ExtensionPoint {

        public static DescriptorExtensionList<BuildCauseCondition, BuildCauseConditionDescriptor> all() {
            return Hudson.getInstance().<BuildCauseCondition, BuildCauseConditionDescriptor>getDescriptorList(BuildCauseCondition.class);
        }

        /**
         * Performs the check of the condition
         *
         * @return true if the condition is allowed
         *         false if not allowed to proceed.
         */
        public abstract boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws InterruptedException;

        /**
         * {@inheritDoc}
         */
        @Override
        public Descriptor<BuildCauseCondition> getDescriptor() {
            return (Descriptor) Hudson.getInstance().getDescriptor(getClass());
        }
    }
    public static abstract class BuildCauseConditionDescriptor extends Descriptor<BuildCauseCondition> {

        protected BuildCauseConditionDescriptor() {
        }

        protected BuildCauseConditionDescriptor(Class<? extends BuildCauseCondition> clazz) {
            super(clazz);
        }
    }
    /**
     * Looks to see if any of the causes of the build match the passed in user list
     * When the user list is empty it assumes that any user build matches.
     *
     */
    public static class UserBuildCauseCondition extends BuildCauseCondition {

        private ArrayList<String> values;

        @DataBoundConstructor
        public UserBuildCauseCondition(String users) {
             this.values = new ArrayList<String>(Arrays.asList(Util.tokenize(users, " \t\n\r\f,")));
        }

        /**
         * Used for generating the config UI.
         *
         * @returns String list of users, comma separated.
         */
        public String getUsers() {
            if(values.size()>0) {
                int len=0;
                for (String value : values)
                    len += value.length();
                char delim = ',';
                // Build string connected with delimiter, quoting as needed
                StringBuilder buf = new StringBuilder(len+values.size()*3);
                for (String value : values)
                    buf.append(delim).append(QuotedStringTokenizer.quote(value,""));
                return buf.substring(1);
            } else {
                // no users defined return empty string
                return "";
            }
        }

        /**
         * Performs the check of the condition
         * Looks to see if any of the causes of the build match the passed in user list
         *
         * @return true if the condition is allowed
         *         false if not allowed to proceed.
         */
        public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws InterruptedException {
            List<Cause> buildCauses = build.getCauses();

            for (Cause cause : buildCauses) {
                listener.getLogger().println(cause.getClass().getName());
                try {

                    if (Cause.UserCause.class.isInstance(cause)) {
                        if(values.size()>0) {
                            UserCause userCause = (UserCause)cause;
                            String username = userCause.getUserName();
                            if(values.contains(username)){
                                listener.getLogger().println("UserBuildCause TRUE found username = "+ username);
                                return true;
                            }
                        } else {
                            return true;
                        }
                    } else if(Class.forName("hudson.model.Cause$UserIdCause").isInstance(cause)) {
                        try {
                            if(values.size()>0) {
                                Method meth = cause.getClass().getMethod("getUserId");
                                String username = (String) meth.invoke(cause);
                                if(values.contains(username)){
                                    listener.getLogger().println("UserIdBuildCause TRUE found username = "+ username);
                                    return true;
                                }
                            } else {
                                return true;
                            }
                        } catch (NoSuchMethodException e){
                            // Will happen for causes other than UserIdCause
                            // so ignore this and move to next item
                        } catch (NullPointerException e) {
                            // should not happen as but just ignore this cause
                        } catch (SecurityException se) {
                            // not allowed acces just ignore this cause
                        } catch (IllegalAccessException e) {
                            //cannot access ignore this as well
                        } catch (InvocationTargetException e) {
                        }
                    }
                }
                catch (ClassNotFoundException ce) {
                    // no issues here as we seem to be on a version of jenkins < 1.428
                }
            }
            return false;
        }

        @Extension
        public static class UserBuildCauseConditionDescriptor extends BuildCauseConditionDescriptor {

            @Override
            public String getDisplayName() {
                return Messages.UserBuildCauseCondition_DisplayName();
            }
            /**
             * Autocompletion method
             *
             * @param value
             * @return list of possible users
             */
            public AutoCompletionCandidates doAutoCompleteUsers(@QueryParameter String value) {
                AutoCompletionCandidates candidates = new AutoCompletionCandidates();
                Collection<User> allUsers = User.getAll();
                for (User user: allUsers) {
                    if (user.getId().startsWith(value)) {
                            candidates.add(user.getId());
                    }
                }
                return candidates;
            }
        }
    }
    /**
     * Looks to see if any of the causes of the build match the passed in project list
     * When the project list is empty it assumes that any project build matches.
     *
     */
    public static class UpstreamCauseCondition extends BuildCauseCondition {

        private ArrayList<String> projects;

        @DataBoundConstructor
        public UpstreamCauseCondition(String projects) {
            this.projects = new ArrayList<String>(Arrays.asList(Util.tokenize(projects, " \t\n\r\f,")));
        }

        /**
         * Used for generating the config UI.
         *
         * @returns String list of projects, comma separated.
         */
        public String getProjects() {
            if(projects.size() > 0) {
                int len=0;
                for (String value : projects)
                    len += value.length();
                char delim = ',';
                // Build string connected with delimiter, quoting as needed
                StringBuilder buf = new StringBuilder(len+projects.size()*3);
                for (String value : projects)
                    buf.append(delim).append(QuotedStringTokenizer.quote(value,""));
                return buf.substring(1);
            } else {
                //no items return empty string
                return "";
            }

        }
        /**
         * Performs the check of the condition
         * Looks to see if any of the causes of the build match the passed in project list
         *
         * @return true if the condition is allowed
         *         false if not allowed to proceed.
         */
        public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws InterruptedException {
            List<Cause> buildCauses = build.getCauses();

            for (Cause cause : buildCauses) {
                if (Cause.UpstreamCause.class.isInstance(cause)) {
                    if(projects.size() > 0) {
                        UpstreamCause upstreamCause = (UpstreamCause)cause;
                        String project = upstreamCause.getUpstreamProject();
                        if(projects.contains(project)){
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
            return false;
        }

        @Extension
        public static class UpstreamCauseConditionDescriptor extends BuildCauseConditionDescriptor {

            @Override
            public String getDisplayName() {
                return Messages.UpstreamBuildCauseCondition_DisplayName();
            }
            /**
             * Autocompletion method
             *
             * Copied from hudson.tasks.BuildTrigger.doAutoCompleteChildProjects(String value)
             *
             * @param value
             * @return List of possible projects
             */
            public AutoCompletionCandidates doAutoCompleteProjects(@QueryParameter String value) {
                AutoCompletionCandidates candidates = new AutoCompletionCandidates();
                List<Job> jobs = Hudson.getInstance().getItems(Job.class);
                for (Job job: jobs) {
                    if (job.getFullName().startsWith(value)) {
                        if (job.hasPermission(Item.READ)) {
                            candidates.add(job.getFullName());
                        }
                    }
                }
                return candidates;
            }
        }
    }
}
