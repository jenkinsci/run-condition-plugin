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

    private final BuildListener listener = createMock(BuildListener.class);
    private final PrintStream logger = new PrintStream(new ByteArrayOutputStream());
    private final FreeStyleBuild build = createMock(FreeStyleBuild.class);

    private static Result[] BuildResults = {
            Result.SUCCESS,
            Result.UNSTABLE,
            Result.FAILURE,
            Result.NOT_BUILT,
            Result.ABORTED};

    private static int RESULTCOUNT = 5;

    @Before
    public void setUp() throws Exception {
        expect(listener.getLogger()).andReturn(logger).anyTimes();
        replay(listener);
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
        /*    worstResult,    bestResult,     buildResult,    expected
        Unstable         Success         Success         true
        Unstable         Success         Unstable        true
        Unstable         Success         Failure         false
        Unstable         Success         Not_built       false
        Unstable         Success         Aborted         false
        */
        boolean[] expresults = {
                true,
                true,
                false,
                false,
                false};


        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.UNSTABLE, Result.SUCCESS, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testFailureSuccess() throws Exception {
        /*    worstResult,    bestResult,     buildResult,    expected
        Failure         Success         Success         true
        Failure         Success         Unstable        true
        Failure         Success         Failure         true
        Failure         Success         Not_built       false
        Failure         Success         Aborted         false
        */
        boolean[] expresults = {
                true,
                true,
                true,
                false,
                false};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.FAILURE, Result.SUCCESS, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testNotBuiltSuccess() throws Exception {
        /*    worstResult,    bestResult,     buildResult,    expected
        Not_built         Success         Success         true
        Not_built         Success         Unstable        true
        Not_built         Success         Failure         true
        Not_built         Success         Not_built       true
        Not_built         Success         Aborted         false
        */
        boolean[] expresults = {
                true,
                true,
                true,
                true,
                false};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.NOT_BUILT, Result.SUCCESS, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testAbortedSuccess() throws Exception {
        /*    worstResult,    bestResult,     buildResult,    expected
        Aborted         Success         Success         true
        Aborted         Success         Unstable        true
        Aborted         Success         Failure         true
        Aborted         Success         Not_built       true
        Aborted         Success         Aborted         true
        */
        boolean[] expresults = {
                true,
                true,
                true,
                true,
                true};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.ABORTED, Result.SUCCESS, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testSuccessUnstable() throws Exception {
        /* never can be valid as possible statuses not inclusive
            worstResult,    bestResult,     buildResult,    expected
        Success         Unstable         Success         false
        Success         Unstable         Unstable        false
        Success         Unstable         Failure         false
        Success         Unstable         Not_built       false
        Success         Unstable         Aborted         false
        */
        boolean[] expresults = {
                false,
                false,
                false,
                false,
                false};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.SUCCESS, Result.UNSTABLE, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testUnstableUnstable() throws Exception {
        /* worstResult,    bestResult,     buildResult,    expected
        Unstable         Unstable         Success         false
        Unstable         Unstable         Unstable        true
        Unstable         Unstable         Failure         false
        Unstable         Unstable         Not_built       false
        Unstable         Unstable         Aborted         false
        */
        boolean[] expresults = {
                false,
                true,
                false,
                false,
                false};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.UNSTABLE, Result.UNSTABLE, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testFailureUnstable() throws Exception {
        /* worstResult,    bestResult,     buildResult,    expected
        Failure         Unstable         Success         false
        Failure         Unstable         Unstable        true
        Failure         Unstable         Failure         true
        Failure         Unstable         Not_built       false
        Failure         Unstable         Aborted         false
        */
        boolean[] expresults = {
                false,
                true,
                true,
                false,
                false};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.FAILURE, Result.UNSTABLE, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testNotbuiltUnstable() throws Exception {
        /* worstResult,    bestResult,     buildResult,    expected
        Not_built         Unstable         Success         false
        Not_built         Unstable         Unstable        true
        Not_built         Unstable         Failure         true
        Not_built         Unstable         Not_built       true
        Not_built         Unstable         Aborted         false
        */
        boolean[] expresults = {
                false,
                true,
                true,
                true,
                false};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.NOT_BUILT, Result.UNSTABLE, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testAbortedUnstable() throws Exception {
        /* worstResult,    bestResult,     buildResult,    expected
        Aborted         Unstable         Success         false
        Aborted         Unstable         Unstable        true
        Aborted         Unstable         Failure         true
        Aborted         Unstable         Not_built       true
        Aborted         Unstable         Aborted         true
        */
        boolean[] expresults = {
                false,
                true,
                true,
                true,
                true};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.ABORTED, Result.UNSTABLE, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testSuccessFailure() throws Exception {
        /* never can be valid as possible statuses not inclusive
            worstResult,    bestResult,     buildResult,    expected
        Success         Failure         Success         false
        Success         Failure         Unstable        false
        Success         Failure         Failure         false
        Success         Failure         Not_built       false
        Success         Failure         Aborted         false
        */
        boolean[] expresults = {
                false,
                false,
                false,
                false,
                false};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.SUCCESS, Result.FAILURE, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testUnstableFailure() throws Exception {
        /* never can be valid as possible statuses not inclusive
            worstResult,    bestResult,     buildResult,    expected
        Unstable         Failure         Success         false
        Unstable         Failure         Unstable        false
        Unstable         Failure         Failure         false
        Unstable         Failure         Not_built       false
        Unstable         Failure         Aborted         false
        */
        boolean[] expresults = {
                false,
                false,
                false,
                false,
                false};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.UNSTABLE, Result.FAILURE, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testFailureFailure() throws Exception {
        /* worstResult,    bestResult,     buildResult,    expected
        Failure         Failure         Success         false
        Failure         Failure         Unstable        false
        Failure         Failure         Failure         true
        Failure         Failure         Not_built       false
        Failure         Failure         Aborted         false
        */
        boolean[] expresults = {
                false,
                false,
                true,
                false,
                false};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.FAILURE, Result.FAILURE, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testNotbuiltFailure() throws Exception {
        /* worstResult,    bestResult,     buildResult,    expected
        Not_built         Failure         Success         false
        Not_built         Failure         Unstable        false
        Not_built         Failure         Failure         true
        Not_built         Failure         Not_built       true
        Not_built         Failure         Aborted         false
        */
        boolean[] expresults = {
                false,
                false,
                true,
                true,
                false};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.NOT_BUILT, Result.FAILURE, BuildResults[i], expresults[i]);
        }
    }

    @Test
    public void testAbortedFailure() throws Exception {
        /* worstResult,    bestResult,     buildResult,    expected
        Aborted         Failure         Success         false
        Aborted         Failure         Unstable        false
        Aborted         Failure         Failure         true
        Aborted         Failure         Not_built       true
        Aborted         Failure         Aborted         true
        */
        boolean[] expresults = {
                false,
                false,
                true,
                true,
                true};

        for (int i = 0; i < RESULTCOUNT; i++) {
            testResultcase(Result.ABORTED, Result.FAILURE, BuildResults[i], expresults[i]);
        }
    }

    private void testResultcase(Result worstresult, Result bestresult, Result buildresult, boolean expected) throws Exception {
        assertRunResult(new StatusCondition(worstresult, bestresult), buildresult, expected);
    }

    private void assertRunResult(StatusCondition condition, Result buildResult, boolean expected) throws Exception {
        reset(build);
        expect(build.getResult()).andReturn(buildResult).anyTimes();
        replay(build);

        assertTrue(condition.runPrebuild(build, listener));
        assertEquals(expected, condition.runPerform(build, listener));
        verify(build);
    }

}
