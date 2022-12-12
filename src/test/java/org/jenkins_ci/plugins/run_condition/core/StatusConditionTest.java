/*
 * The MIT License
 *
 * Copyright (C) 2012 by Chris Johnson, Anthony Robinson
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

import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static hudson.model.Result.ABORTED;
import static hudson.model.Result.FAILURE;
import static hudson.model.Result.NOT_BUILT;
import static hudson.model.Result.SUCCESS;
import static hudson.model.Result.UNSTABLE;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StatusConditionTest {

    private final BuildListener buildListener = createMock(BuildListener.class);
    private final PrintStream logger = new PrintStream(new ByteArrayOutputStream());
    private final FreeStyleBuild freeStyleBuild = createMock(FreeStyleBuild.class);

    private static Result[] BuildResults = { SUCCESS, UNSTABLE, FAILURE, NOT_BUILT, ABORTED };

    @Before
    public void setUp() throws Exception {
        expect(buildListener.getLogger()).andReturn(logger).anyTimes();
        replay(buildListener);
    }

    @After
    public void tearDown() throws Exception {
        logger.close();
    }

    @Test
    public void testStringConstructorSuccess() throws Exception {
        StatusCondition condition = new StatusCondition("SUCCESS", "SUCCESS");
        assertEquals(Result.SUCCESS, condition.getBestResult());
        assertEquals(Result.SUCCESS, condition.getWorstResult());
    }

    @Test
    public void testStringConstructorUnstable() throws Exception {
        StatusCondition condition = new StatusCondition("UNSTABLE", "UNSTABLE");
        assertEquals(Result.UNSTABLE, condition.getBestResult());
        assertEquals(Result.UNSTABLE, condition.getWorstResult());
    }

    @Test
    public void testStringConstructorFailure() throws Exception {
        StatusCondition condition = new StatusCondition("FAILURE", "FAILURE");
        assertEquals(Result.FAILURE, condition.getBestResult());
        assertEquals(Result.FAILURE, condition.getWorstResult());
    }

    @Test
    public void testStringConstructorNotBuilt() throws Exception {
        StatusCondition condition = new StatusCondition("NOT_BUILT", "NOT_BUILT");
        assertEquals(Result.NOT_BUILT, condition.getBestResult());
        assertEquals(Result.NOT_BUILT, condition.getWorstResult());
    }

    @Test
    public void testStringConstructorAborted() throws Exception {
        StatusCondition condition = new StatusCondition("ABORTED", "ABORTED");
        assertEquals(Result.ABORTED, condition.getBestResult());
        assertEquals(Result.ABORTED, condition.getWorstResult());
    }

    @Test(expected = RuntimeException.class)
    public void testStringConstructorInvalidWorst() throws Exception {
        new StatusCondition("INVALID_BUILD_STATUS", "ABORTED");
    }

    @Test(expected = RuntimeException.class)
    public void testStringConstructorInvalidBest() throws Exception {
        new StatusCondition("ABORTED", "INVALID_BUILD_STATUS");
    }

    @Test
    public void testSuccessSuccess() throws Exception {
        final StatusCondition condition = new StatusCondition(SUCCESS, SUCCESS);
        assertRunResult(condition, SUCCESS, true);
        assertRunResult(condition, UNSTABLE, false);
        assertRunResult(condition, FAILURE, false);
        assertRunResult(condition, NOT_BUILT, false);
        assertRunResult(condition, ABORTED, false);
    }

    @Test
    public void testUnstableSuccess() throws Exception {
        final StatusCondition condition = new StatusCondition(UNSTABLE, SUCCESS);
        assertRunResult(condition, SUCCESS, true);
        assertRunResult(condition, UNSTABLE, true);
        assertRunResult(condition, FAILURE, false);
        assertRunResult(condition, NOT_BUILT, false);
        assertRunResult(condition, ABORTED, false);
    }

    @Test
    public void testFailureSuccess() throws Exception {
        final StatusCondition condition = new StatusCondition(FAILURE, SUCCESS);
        assertRunResult(condition, SUCCESS, true);
        assertRunResult(condition, UNSTABLE, true);
        assertRunResult(condition, FAILURE, true);
        assertRunResult(condition, NOT_BUILT, false);
        assertRunResult(condition, ABORTED, false);
    }

    @Test
    public void testNotBuiltSuccess() throws Exception {
        final StatusCondition condition = new StatusCondition(NOT_BUILT, SUCCESS);
        assertRunResult(condition, SUCCESS, true);
        assertRunResult(condition, UNSTABLE, true);
        assertRunResult(condition, FAILURE, true);
        assertRunResult(condition, NOT_BUILT, true);
        assertRunResult(condition, ABORTED, false);
    }

    @Test
    public void testAbortedSuccess() throws Exception {
        final StatusCondition condition = new StatusCondition(ABORTED, SUCCESS);
        assertRunResult(condition, SUCCESS, true);
        assertRunResult(condition, UNSTABLE, true);
        assertRunResult(condition, FAILURE, true);
        assertRunResult(condition, NOT_BUILT, true);
        assertRunResult(condition, ABORTED, true);
    }

    @Test
    public void testSuccessUnstableNeverTrue() throws Exception {
        assertRunPerformAlwaysFalse(new StatusCondition(SUCCESS, UNSTABLE));
    }

    @Test
    public void testUnstableUnstable() throws Exception {
        final StatusCondition condition = new StatusCondition(UNSTABLE, UNSTABLE);
        assertRunResult(condition, SUCCESS, false);
        assertRunResult(condition, UNSTABLE, true);
        assertRunResult(condition, FAILURE, false);
        assertRunResult(condition, NOT_BUILT, false);
        assertRunResult(condition, ABORTED, false);
    }

    @Test
    public void testFailureUnstable() throws Exception {
        final StatusCondition condition = new StatusCondition(FAILURE, UNSTABLE);
        assertRunResult(condition, SUCCESS, false);
        assertRunResult(condition, UNSTABLE, true);
        assertRunResult(condition, FAILURE, true);
        assertRunResult(condition, NOT_BUILT, false);
        assertRunResult(condition, ABORTED, false);
    }

    @Test
    public void testNotbuiltUnstable() throws Exception {
        final StatusCondition condition = new StatusCondition(NOT_BUILT, UNSTABLE);
        assertRunResult(condition, SUCCESS, false);
        assertRunResult(condition, UNSTABLE, true);
        assertRunResult(condition, FAILURE, true);
        assertRunResult(condition, NOT_BUILT, true);
        assertRunResult(condition, ABORTED, false);
    }

    @Test
    public void testAbortedUnstable() throws Exception {
        final StatusCondition condition = new StatusCondition(ABORTED, UNSTABLE);
        assertRunResult(condition, SUCCESS, false);
        assertRunResult(condition, UNSTABLE, true);
        assertRunResult(condition, FAILURE, true);
        assertRunResult(condition, NOT_BUILT, true);
        assertRunResult(condition, ABORTED, true);
    }

    @Test
    public void testSuccessFailure() throws Exception {
        assertRunPerformAlwaysFalse(new StatusCondition(SUCCESS, FAILURE));
    }

    @Test
    public void testUnstableFailure() throws Exception {
        assertRunPerformAlwaysFalse(new StatusCondition(UNSTABLE, FAILURE));
    }

    @Test
    public void testFailureFailure() throws Exception {
        final StatusCondition condition = new StatusCondition(FAILURE, FAILURE);
        assertRunResult(condition, SUCCESS, false);
        assertRunResult(condition, UNSTABLE, false);
        assertRunResult(condition, FAILURE, true);
        assertRunResult(condition, NOT_BUILT, false);
        assertRunResult(condition, ABORTED, false);
    }

    @Test
    public void testNotbuiltFailure() throws Exception {
        final StatusCondition condition = new StatusCondition(NOT_BUILT, FAILURE);
        assertRunResult(condition, SUCCESS, false);
        assertRunResult(condition, UNSTABLE, false);
        assertRunResult(condition, FAILURE, true);
        assertRunResult(condition, NOT_BUILT, true);
        assertRunResult(condition, ABORTED, false);
    }

    @Test
    public void testAbortedFailure() throws Exception {
        final StatusCondition condition = new StatusCondition(ABORTED, FAILURE);
        assertRunResult(condition, SUCCESS, false);
        assertRunResult(condition, UNSTABLE, false);
        assertRunResult(condition, FAILURE, true);
        assertRunResult(condition, NOT_BUILT, true);
        assertRunResult(condition, ABORTED, true);
    }

    private void assertRunPerformAlwaysFalse(final StatusCondition condition) throws Exception {
        for (Result result : BuildResults)
            assertRunResult(condition, result, false);
    }

    private void assertRunResult(final StatusCondition condition, final Result buildResult, final boolean expected) throws Exception {
        reset(freeStyleBuild);
        expect(freeStyleBuild.getResult()).andReturn(buildResult).anyTimes();
        replay(freeStyleBuild);

        assertTrue(condition.runPrebuild(freeStyleBuild, buildListener));
        assertEquals(expected, condition.runPerform(freeStyleBuild, buildListener));
        verify(freeStyleBuild);
    }

}
