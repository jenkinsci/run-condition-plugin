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
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public final class StatusCondition extends AlwaysPrebuildRunCondition {

    private static final Result[] ALL_RESULTS = new Result[] {Result.SUCCESS, Result.UNSTABLE, Result.FAILURE, Result.NOT_BUILT,
                                                                                                                            Result.ABORTED};

    private static Result resultFormString(final String name) {
        for (Result result : ALL_RESULTS)
            if (result.toString().equals(name))
                return result;
        throw new RuntimeException(Messages.statusCondition_exception_unknownResult(name));
    }

    final Result worstResult;
    final Result bestResult;

    @DataBoundConstructor
    public StatusCondition(final String worstResult, final String bestResult) {
        this(resultFormString(worstResult), resultFormString(bestResult));
    }

    public StatusCondition(final Result worstResult, final Result bestResult) {
        this.worstResult = worstResult;
        this.bestResult = bestResult;
    }
    public Result getBestResult() {
        return bestResult;
    }

    public Result getWorstResult() {
        return worstResult;
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) {
        final Result currentStatus = build.getResult() == null ? Result.SUCCESS : build.getResult();
        return worstResult.isWorseOrEqualTo(currentStatus) && bestResult.isBetterOrEqualTo(currentStatus);
    }

    @Extension
    public static class StatusConditionDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.statusCondition_displayName();
        }

        public FormValidation doCheckBestResult(@QueryParameter final String worstResult, @QueryParameter final String bestResult) {
            if ((Util.fixEmptyAndTrim(worstResult) == null) || (Util.fixEmptyAndTrim(bestResult) == null)) return FormValidation.ok();
            return resultFormString(worstResult).isWorseOrEqualTo(resultFormString(bestResult))
                    ? FormValidation.ok() : FormValidation.error(Messages.statusCondition_validation_bestWorseThanWorst());
        }

        public FormValidation doCheckWorstResult(@QueryParameter final String worstResult, @QueryParameter final String bestResult) {
            if ((Util.fixEmptyAndTrim(worstResult) == null) || (Util.fixEmptyAndTrim(bestResult) == null)) return FormValidation.ok();
            return resultFormString(worstResult).isWorseOrEqualTo(resultFormString(bestResult))
                    ? FormValidation.ok() : FormValidation.error(Messages.statusCondition_validation_worseBetterThanBest());
        }

        public ListBoxModel doFillWorstResultItems() {
            return getResultsModel();
        }

        public ListBoxModel doFillBestResultItems() {
            return getResultsModel();
        }

        private ListBoxModel getResultsModel() {
            ListBoxModel items = new ListBoxModel();
            for (Result result : ALL_RESULTS) {
                items.add(new ListBoxModel.Option(result.color.getDescription(), result.toString()));
            }
            return items;
        }

    }

}
