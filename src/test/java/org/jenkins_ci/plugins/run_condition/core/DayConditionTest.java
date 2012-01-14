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

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

public class DayConditionTest {

    private final Calendar buildTime = Calendar.getInstance();
    private final Calendar currentTime = Calendar.getInstance();
    private final IMocksControl mockControl = EasyMock.createNiceControl();
    private final BuildListener listener = mockControl.createMock(BuildListener.class);
    private final PrintStream logger = new PrintStream(new ByteArrayOutputStream());
    
    @Before
    public void setUp() throws Exception {
        expect(listener.getLogger()).andReturn(logger).anyTimes();
        mockControl.replay();
    }

    @After
    public void tearDown() throws Exception {
        logger.close();
    }

    @Test
    public void testWeekend() throws Exception {
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        assertWeekend(true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertWeekend(true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertWeekend(true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertWeekend(true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertWeekend(true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        assertWeekend(true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        assertWeekend(true, false);
    }

    @Test
    public void testWeekday() throws Exception {
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        assertWeekday(true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertWeekday(true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertWeekday(true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertWeekday(true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertWeekday(true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        assertWeekday(true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        assertWeekday(true, true);
    }

    @Test
    public void testSelectDaysNoneSelected() throws Exception {
        final DayCondition.SelectDays select = new DayCondition.SelectDays(DayCondition.SelectDays.SelectDaysDescriptor.getAllDays());
        final DayCondition condition = new DayCondition(false, select);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        assertCondition(condition, true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertCondition(condition, true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertCondition(condition, true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertCondition(condition, true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertCondition(condition, true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        assertCondition(condition, true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        assertCondition(condition, true, false);
    }

    @Test
    public void testSelectDaysAllSelected() throws Exception {
        final ArrayList<DayCondition.Day> days = new ArrayList<DayCondition.Day>();
        for (int i = 0; i < 8; i++)
            days.add(new DayCondition.Day(i, true));
        final DayCondition condition = new DayCondition(false, new DayCondition.SelectDays(days));
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        assertCondition(condition, true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertCondition(condition, true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertCondition(condition, true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertCondition(condition, true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertCondition(condition, true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        assertCondition(condition, true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        assertCondition(condition, true, true);
    }

    @Test
    public void testSelectDaysSomeSelected() throws Exception {
        final ArrayList<DayCondition.Day> days = new ArrayList<DayCondition.Day>();
        days.add(new DayCondition.Day(Calendar.MONDAY, true));
        days.add(new DayCondition.Day(Calendar.TUESDAY, false));
        days.add(new DayCondition.Day(Calendar.WEDNESDAY, true));
        days.add(new DayCondition.Day(Calendar.THURSDAY, true));
        days.add(new DayCondition.Day(Calendar.FRIDAY, false));
        days.add(new DayCondition.Day(Calendar.SATURDAY, true));
        days.add(new DayCondition.Day(Calendar.SUNDAY, false));
        final DayCondition condition = new DayConditionForTest(false, new DayCondition.SelectDays(days));
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        assertCondition(condition, true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertCondition(condition, true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertCondition(condition, true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertCondition(condition, true, false);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertCondition(condition, true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        assertCondition(condition, true, true);
        currentTime.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        assertCondition(condition, true, false);
    }

    private void assertWeekday(final boolean expectedPrebuild, final boolean expectedPerform) throws Exception {
        assertCondition(new DayConditionForTest(false, new DayCondition.Weekday()), expectedPrebuild, expectedPerform);
    }

    private void assertWeekend(final boolean expectedPrebuild, final boolean expectedPerform) throws Exception {
        assertCondition(new DayConditionForTest(false, new DayCondition.Weekend()), expectedPrebuild, expectedPerform);
    }

    private void assertCondition(final RunCondition condition, final boolean expectedPrebuild, final boolean expectedPerform)
                                                                                                                        throws Exception {
        assertEquals(expectedPrebuild, condition.runPrebuild(null, listener));
        assertEquals(expectedPerform, condition.runPerform(null, listener));
    }

    private class DayConditionForTest extends DayCondition {

        public DayConditionForTest(final boolean useBuildTime, final DaySelector daySelector) {
            super(useBuildTime, daySelector);
        }

        @Override
        protected Calendar getNow() {
            return currentTime;
        }

        @Override
        protected Calendar getTimestamp(final AbstractBuild<?, ?> build) {
            return buildTime;
        }
    }

}
