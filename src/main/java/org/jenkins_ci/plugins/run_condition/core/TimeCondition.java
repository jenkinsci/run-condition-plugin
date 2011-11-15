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
import hudson.util.FormValidation;
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeCondition extends RunCondition {

    private static final Pattern TIME = Pattern.compile("^((?:0?\\d)|(?:1\\d)|(?:2[0-3])):([0-5]\\d)$");
    private static final String TIME_STR_FORMAT = "%02d:%02d";
    private final int earliestHours;
    private final int earliestMinutes;
    private final int latestHours;
    private final int latestMinutes;
    private final boolean useBuildTime;

    @DataBoundConstructor
    public TimeCondition(final String earliest, final String latest, final boolean useBuildTime) {
        final Matcher earliestM = TIME.matcher(earliest);
        if (!earliestM.matches()) throw new RuntimeException(Messages.timeCondition_validation_invalid(earliest));
        final Matcher latestM = TIME.matcher(latest);
        if (!latestM.matches()) throw new RuntimeException(Messages.timeCondition_validation_invalid(latest));
        this.earliestHours = Integer.parseInt(earliestM.group(1));
        this.earliestMinutes = Integer.parseInt(earliestM.group(2));
        this.latestHours = Integer.parseInt(latestM.group(1));
        this.latestMinutes = Integer.parseInt(latestM.group(2));
        this.useBuildTime = useBuildTime;
    }

    public int getEarliestHours() {
        return earliestHours;
    }

    public int getEarliestMinutes() {
        return earliestMinutes;
    }

    public int getLatestHours() {
        return latestHours;
    }

    public int getLatestMinutes() {
        return latestMinutes;
    }

    public boolean isUseBuildTime() {
        return useBuildTime;
    }

    public String getEarliest() {
        return String.format(TIME_STR_FORMAT, earliestHours, earliestMinutes);
    }

    public String getLatest() {
        return String.format(TIME_STR_FORMAT, latestHours, latestMinutes);
    }

    @Override
    public final boolean runPrebuild(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        return useBuildTime ? isInHours(getTimestamp(build), listener) : true;
    }

    @Override
    public final boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        return useBuildTime ? isInHours(getTimestamp(build), listener) : isInHours(getNow(), listener);
    }

    protected Calendar getNow() {
        return Calendar.getInstance();
    }

    protected Calendar getTimestamp(final AbstractBuild<?, ?> build) {
        return build.getTimestamp();
    }

    private final boolean isInHours(final Calendar testTime, final BuildListener listener) {
        final Calendar lower = (Calendar) testTime.clone();
        final Calendar upper = (Calendar) lower.clone();
        setTime(lower, earliestHours, earliestMinutes);
        setTime(upper, latestHours, latestMinutes);
        floor(lower);
        floor(upper);
        lower.add(Calendar.SECOND, -1);
        upper.add(Calendar.MINUTE, 1);
        final String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(testTime.getTime());
        listener.getLogger().println(Messages.timeCondition_console_testing(getEarliest(), time, getLatest()));
        return lower.before(testTime) && upper.after(testTime);
    }

    private static void floor(final Calendar calendar) {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private static void setTime(final Calendar calendar, final int hours, final int minutes) {
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
    }

    public static boolean isTimeValid(final String time) {
        return TIME.matcher(time).matches();
    }

    @Extension
    public static class TimeConditionDescriptor extends RunConditionDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.timeCondition_displayName();
        }
        public FormValidation doCheckEarliest(@QueryParameter final String value) {
            return checkTime(value);
        }
        public FormValidation doCheckLatest(@QueryParameter final String value) {
            return checkTime(value);
        }
        private FormValidation checkTime(final String time) {
            return isTimeValid(time) ? FormValidation.ok()
                    : FormValidation.error(Messages.timeCondition_validation_invalid(time));
        }
    }

}
