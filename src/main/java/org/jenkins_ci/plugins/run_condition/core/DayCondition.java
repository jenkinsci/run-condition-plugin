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
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

public class DayCondition extends RunCondition {

    private final boolean useBuildTime;
    private final DaySelector daySelector;

    @DataBoundConstructor
    public DayCondition(final boolean useBuildTime, final DaySelector daySelector) {
        this.useBuildTime = useBuildTime;
        this.daySelector = daySelector;
    }

    public boolean isUseBuildTime() {
        return useBuildTime;
    }

    public DaySelector getDaySelector() {
        return daySelector;
    }

    @Override
    public final boolean runPrebuild(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        return useBuildTime ? isDayEnabled(getTimestamp(build)) : true;
    }

    @Override
    public final boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        return useBuildTime ? isDayEnabled(getTimestamp(build)) : isDayEnabled(getNow());
    }

    protected Calendar getNow() {
        return Calendar.getInstance();
    }

    protected Calendar getTimestamp(final AbstractBuild<?, ?> build) {
        return build.getTimestamp();
    }

    private final boolean isDayEnabled(final Calendar testTime) {
        return daySelector.isDaySelected(testTime);
    }

    private static boolean isWeekend(final Calendar calendar) {
        final int day = calendar.get(Calendar.DAY_OF_WEEK);
        return (day == Calendar.SATURDAY) || (day == Calendar.SUNDAY);
    }

    @Extension
    public static class DayConditionDescriptor extends RunConditionDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.dayCondition_displayName();
        }
        public List<? extends Descriptor<? extends DaySelector>> getDaySelectors() {
            return Hudson.getInstance().<DaySelector, DaySelector.SelectorDescriptor>getDescriptorList(DaySelector.class);
        }        
    }

    public static abstract class DaySelector implements Describable<DaySelector> {
        public abstract boolean isDaySelected(Calendar testDate);
        public Descriptor<DaySelector> getDescriptor() {
            return (Descriptor) Hudson.getInstance().getDescriptor(getClass());
        }
        public static abstract class SelectorDescriptor extends Descriptor<DaySelector> {
        }
    }

    public static class Weekday extends DaySelector {
        @DataBoundConstructor public Weekday() {}
        @Override
        public boolean isDaySelected(final Calendar testDate) {
            return !isWeekend(testDate);
        }
        @Extension(ordinal = 0)
        public static class WeekdayDescriptor extends SelectorDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.dayCondition_weekday_displayName();
            }
        }
    }

    public static class Weekend extends DaySelector {
        @DataBoundConstructor public Weekend() {}
        @Override
        public boolean isDaySelected(final Calendar testDate) {
            return isWeekend(testDate);
        }
        @Extension(ordinal = -1)
        public static class WeekendDescriptor extends SelectorDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.dayCondition_weekend_displayName();
            }
        }
    }

    public static class SelectDays extends DaySelector {

        private final ArrayList<Day> days;
        private transient TreeMap<Integer, Boolean> lookup;

        @DataBoundConstructor
        public SelectDays(final ArrayList<Day> days) {
            this.days = days;
            createLookup();
        }

        public ArrayList<Day> getDays() {
            return days;
        }

        @Override
        public boolean isDaySelected(final Calendar testDate) {
            return lookup.get(testDate.get(Calendar.DAY_OF_WEEK));
        }

        private void createLookup() {
            lookup = new TreeMap<Integer, Boolean>();
            for (Day day : days)
                lookup.put(day.getDay(), day.isSelected());
        }

        public Object readResolve() {
            createLookup();
            return this;
        }

        @Extension(ordinal = -2)
        public static class SelectDaysDescriptor extends SelectorDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.dayCondition_selectDays_displayName();
            }
            public ArrayList<Day> getDefaultDays() {
                return getAllDays();
            }
            public Day.DayDescriptor getDayDescriptor() {
                return Hudson.getInstance().getDescriptorByType(Day.DayDescriptor.class);
            }
            public static ArrayList<Day> getAllDays() {
                final ArrayList<Day> days = new ArrayList<Day>();
                days.add(new Day(Calendar.MONDAY, false));
                days.add(new Day(Calendar.TUESDAY, false));
                days.add(new Day(Calendar.WEDNESDAY, false));
                days.add(new Day(Calendar.THURSDAY, false));
                days.add(new Day(Calendar.FRIDAY, false));
                days.add(new Day(Calendar.SATURDAY, false));
                days.add(new Day(Calendar.SUNDAY, false));
                return days;
            }
        }
    }

    public static class Day implements Describable<Day> {

        private final int day;
        private final boolean selected;

        @DataBoundConstructor
        public Day(final int day, final boolean selected) {
            this.day = day;
            this.selected = selected;
        }

        public int getDay() {
            return day;
        }

        public boolean isSelected() {
            return selected;
        }

        public Descriptor<Day> getDescriptor() {
            return Hudson.getInstance().getDescriptorByType(DayDescriptor.class);
        }

        @Extension
        public static class DayDescriptor extends Descriptor<Day> {

            @Override
            public String getDisplayName() {
                return "A day, innit.";
            }

            public String getDayName(final int day) {
                final Calendar ref = Calendar.getInstance();
                ref.set(Calendar.DAY_OF_WEEK, day);
                return String.format(Stapler.getCurrentRequest().getLocale(), "%tA", ref);
            }

        }

    }

}
