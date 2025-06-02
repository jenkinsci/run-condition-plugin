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
import org.junit.jupiter.api.AfterEach;

import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeConditionTest {

    private static final int LOWER_HOURS = 9;
    private static final int LOWER_MINUTES = 0;
    private static final int UPPER_HOURS = 18;
    private static final int UPPER_MINUTES = 30;
    private final Calendar buildTime = Calendar.getInstance();
    private final Calendar currentTime = Calendar.getInstance();
    private final IMocksControl mockControl = EasyMock.createNiceControl();
    private final BuildListener listener = mockControl.createMock(BuildListener.class);
    private final PrintStream logger = new PrintStream(new ByteArrayOutputStream());

    @BeforeEach
    void setUp() {
        setDate(buildTime);
        setDate(currentTime);
        expect(listener.getLogger()).andReturn(logger).anyTimes();
        mockControl.replay();
    }

    @AfterEach
    void tearDown() {
        logger.close();
    }

    @Test
    void testFalseIfBeforeLowerTime() throws Exception {
        setTimeOutsideRange(buildTime);
        setTime(currentTime, LOWER_HOURS, LOWER_MINUTES);
        floor(currentTime);
        currentTime.add(Calendar.SECOND, -1);
        assertCurrent(createCurrentTimeTimeCondition(), false);
    }

    @Test
    void testTrueIfSameAsLowerTime() throws Exception {
        setTimeOutsideRange(buildTime);
        setTime(currentTime, LOWER_HOURS, LOWER_MINUTES);
        floor(currentTime);
        assertCurrent(createCurrentTimeTimeCondition(), true);
    }

    @Test
    void testTrueIfAfterLowerTime() throws Exception {
        setTimeOutsideRange(buildTime);
        setTime(currentTime, LOWER_HOURS, LOWER_MINUTES);
        floor(currentTime);
        currentTime.add(Calendar.SECOND, 1);
        assertCurrent(createCurrentTimeTimeCondition(), true);
    }

    @Test
    void testFalseIfAfterUpperTime() throws Exception {
        setTimeOutsideRange(buildTime);
        setTime(currentTime, UPPER_HOURS, UPPER_MINUTES);
        ceiling(currentTime);
        currentTime.add(Calendar.SECOND, 1);
        assertCurrent(createCurrentTimeTimeCondition(), false);
    }

    @Test
    void testTrueIfSameAsUpperTime() throws Exception {
        setTimeOutsideRange(buildTime);
        setTime(currentTime, UPPER_HOURS, UPPER_MINUTES);
        ceiling(currentTime);
        assertCurrent(createCurrentTimeTimeCondition(), true);
    }

    @Test
    void testTrueIfBeforeUpperTime() throws Exception {
        setTimeOutsideRange(buildTime);
        setTime(currentTime, UPPER_HOURS, UPPER_MINUTES);
        ceiling(currentTime);
        currentTime.add(Calendar.SECOND, -1);
        assertCurrent(createCurrentTimeTimeCondition(), true);
    }

    @Test
    void testFalseIfBuildTimeBeforeLowerTime() throws Exception {
        setTimeOutsideRange(currentTime);
        setTime(buildTime, LOWER_HOURS, LOWER_MINUTES);
        floor(buildTime);
        buildTime.add(Calendar.SECOND, -1);
        assertBuild(createBuildTimeTimeCondition(), false);
    }

    @Test
    void testTrueIfBuildTimeSameAsLowerTime() throws Exception {
        setTimeOutsideRange(currentTime);
        setTime(buildTime, LOWER_HOURS, LOWER_MINUTES);
        floor(buildTime);
        assertBuild(createBuildTimeTimeCondition(), true);
    }

    @Test
    void testTrueIfBuildTimeAfterLowerTime() throws Exception {
        setTimeOutsideRange(currentTime);
        setTime(buildTime, LOWER_HOURS, LOWER_MINUTES);
        floor(buildTime);
        buildTime.add(Calendar.SECOND, 1);
        assertBuild(createBuildTimeTimeCondition(), true);
    }

    @Test
    void testFalseIfBuildTimeAfterUpperTime() throws Exception {
        setTimeOutsideRange(currentTime);
        setTime(buildTime, UPPER_HOURS, UPPER_MINUTES);
        ceiling(buildTime);
        buildTime.add(Calendar.SECOND, 1);
        assertBuild(createBuildTimeTimeCondition(), false);
    }

    @Test
    void testTrueIfBuildTimeSameAsUpperTime() throws Exception {
        setTimeOutsideRange(currentTime);
        setTime(buildTime, UPPER_HOURS, UPPER_MINUTES);
        ceiling(buildTime);
        assertBuild(createBuildTimeTimeCondition(), true);
    }

    @Test
    void testTrueIfBuildTimeBeforeUpperTime() throws Exception {
        setTimeOutsideRange(currentTime);
        setTime(buildTime, UPPER_HOURS, UPPER_MINUTES);
        ceiling(buildTime);
        buildTime.add(Calendar.SECOND, -1);
        assertBuild(createBuildTimeTimeCondition(), true);
    }

    @Test
    void testTimeStringTest() {
        for (int hours = 0; hours < 24; hours++) {
            for (int seconds = 0; seconds < 60; seconds++) {
                final String timeString = String.format("%d:%02d", hours, seconds);
                assertTrue(TimeCondition.isTimeValid(timeString), timeString);
            }
        }
        for (int hours = 0; hours < 10; hours++) {
            for (int seconds = 0; seconds < 60; seconds++) {
                final String timeString = String.format("%02d:%02d", hours, seconds);
                assertTrue(TimeCondition.isTimeValid(timeString), timeString);
            }
        }
    }

    private void assertBuild(final RunCondition condition, final boolean expected) throws Exception {
        assertCondition(condition, expected, expected);
    }

    private void assertCurrent(final RunCondition condition, final boolean expectedRunPerform) throws Exception {
        assertCondition(condition, true, expectedRunPerform);
    }

    private void assertCondition(final RunCondition condition, final boolean expectedRunPrebuild, final boolean expectedRunPerform)
                                                                                                                        throws Exception {
        assertEquals(expectedRunPrebuild, condition.runPrebuild(null, listener));
        assertEquals(expectedRunPerform, condition.runPerform(null, listener));
    }

    private TimeCondition createCurrentTimeTimeCondition() {
        return new TimeConditionForTest(LOWER_HOURS, LOWER_MINUTES, UPPER_HOURS, UPPER_MINUTES, false);
    }

    private TimeCondition createBuildTimeTimeCondition() {
        return new TimeConditionForTest(LOWER_HOURS, LOWER_MINUTES, UPPER_HOURS, UPPER_MINUTES, true);
    }

    private void setTimeOutsideRange(final Calendar calendar) {
        setTime(calendar, UPPER_HOURS, UPPER_MINUTES);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
    }

    private static void floor(final Calendar calendar) {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private static void ceiling(final Calendar calendar) {
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    private static void setTime(final Calendar calendar, final int hours, final int minutes) {
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
    }

    private static void setDate(final Calendar calendar) {
        calendar.set(Calendar.YEAR, 2011);
        calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 14);
    }

    private class TimeConditionForTest extends TimeCondition {

        public TimeConditionForTest(final int earliestHours, final int earliestMinutes, final int latestHours, final int latestMinutes,
                                    final boolean useBuildTime) {
            super(String.format("%d:%02d", earliestHours, earliestMinutes), String.format("%d:%2d", latestHours, latestMinutes),
                    useBuildTime);
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
