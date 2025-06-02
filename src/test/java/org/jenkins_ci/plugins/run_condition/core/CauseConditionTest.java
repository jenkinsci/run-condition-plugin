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

package org.jenkins_ci.plugins.run_condition.core;

import hudson.model.Cause.LegacyCodeCause;
import hudson.model.Cause.RemoteCause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserCause;
import hudson.model.*;
import hudson.matrix.TextAxis;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.matrix.AxisList;
import hudson.tasks.BuildStep;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockBuilder;

import hudson.triggers.TimerTrigger.TimerTriggerCause;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner.Run;

import org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder;

import java.util.Collections;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithJenkins
class CauseConditionTest {

    private JenkinsRule jenkinsRule;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
    }

    //-------------------------------------------------------
    // UserCause deprecated after Jenkins 1.427
    @Test
    void testUCAnyUser() throws Exception {
        List<Cause> buildCauses = new ArrayList<>();

        // tests with no users defined

        RunCondition condition = new CauseCondition("USER_CAUSE", false);

        // started by SYSTEM user
        buildCauses.add(new UserCause());
        runTest(buildCauses, condition, true);

        // started by user
        buildCauses.clear();
        buildCauses.add(createUserCause("fred"));
        runTest(buildCauses, condition, true);

        // started by different causes
        buildCauses.clear();
        buildCauses.add(new LegacyCodeCause());
        runTest(buildCauses, condition, false);


        // started by mltiple users including requested
        buildCauses.clear();
        buildCauses.add(createUserCause("tom"));
        buildCauses.add(createUserCause("fred"));
        buildCauses.add(createUserCause("harry"));

        runTest(buildCauses, condition, true);


        // started by different causes
        buildCauses.clear();
        buildCauses.add(new LegacyCodeCause());
        runTest(buildCauses, condition, false);

        // multiple different causes
        // add a second cause
        buildCauses.add(new RemoteCause("dummy_host", "dummynote") );
        runTest(buildCauses, condition, false);


        // test with Exclusive set
        condition = new CauseCondition("USER_CAUSE", true);

        // started by correct user
        buildCauses.clear();
        buildCauses.add(createUserCause("fred"));
        runTest(buildCauses, condition, true);

        // started by several users
        buildCauses.clear();
        buildCauses.add(createUserCause("eviloverlord"));
        buildCauses.add(createUserCause("fred"));
        runTest(buildCauses, condition, false);

        // started by several causes
        buildCauses.clear();
        buildCauses.add(createUserCause("eviloverlord"));
        buildCauses.add(createUserCause("fred"));
        buildCauses.add(new LegacyCodeCause());
        buildCauses.add(new RemoteCause("dummy_host", "dummynote") );
        buildCauses.add(new TimerTriggerCause());
        runTest(buildCauses, condition, false);
    }

    @Issue("JENKINS-14438")
    @Test
    void testMatrixUpstreamCause() throws Exception {
        // setup some Causes
        FreeStyleProject upProject = jenkinsRule.createFreeStyleProject("firstProject");
        FreeStyleBuild upBuild = upProject.scheduleBuild2(0).get();

        Cause upstreamCause = new UpstreamCause(upBuild);
        Cause userCause = createUserCause("testUser");

        // test upstream condition
        RunCondition condition = new CauseCondition("UPSTREAM_CAUSE", false);
        runMatrixTest(upstreamCause, condition, true);
        runMatrixTest(userCause, condition, false);

        //test User condition
        condition = new CauseCondition("USER_CAUSE", false);
        runMatrixTest(upstreamCause, condition, false);
        runMatrixTest(userCause, condition, true);
    }

    @Issue("JENKINS-14438")
    @Test
    void testMatrixUserCause() throws Exception {
        // setup some Causes
        FreeStyleProject upProject = jenkinsRule.createFreeStyleProject("secondProject");
        FreeStyleBuild upBuild = upProject.scheduleBuild2(0).get();

        Cause upstreamCause = new UpstreamCause(upBuild);
        Cause userCause = createUserCause("testUser");

        // test User condition
        RunCondition condition = new CauseCondition("USER_CAUSE", false);
        runMatrixTest(upstreamCause, condition, false);
        runMatrixTest(userCause, condition, true);
    }

    private void runMatrixTest(Cause buildTrigger, RunCondition condition, Boolean builderRuns) throws Exception {
        MatrixProject matrixProject = createMatrixProject();

        // if the builder should run the result for each subbuild should be unstable
        // if the builder is not to run the result for each subbuild should be success.
        Result testResult = builderRuns ? Result.UNSTABLE : Result.SUCCESS;

        // create conditional build step requirements
        List<BuildStep> builders = Collections.singletonList(new MockBuilder(Result.UNSTABLE));

        BuildStepRunner runner = new Run();

        // add conditional build step
        matrixProject.getBuildersList().add(new ConditionalBuilder(condition, runner, builders));

        MatrixBuild matrixBuild = matrixProject.scheduleBuild2(0, buildTrigger).get();

        List<MatrixRun> runs = matrixBuild.getRuns();
        assertEquals(4,runs.size());
        for (MatrixRun run : runs) {
            jenkinsRule.assertBuildStatus(testResult, run);
        }
    }

    private MatrixProject createMatrixProject() throws IOException {
        MatrixProject p = jenkinsRule.createProject(MatrixProject.class);

        // set up 2x2 matrix
        AxisList axes = new AxisList();
        axes.add(new TextAxis("db","mysql","oracle"));
        axes.add(new TextAxis("direction","north","south"));
        p.setAxes(axes);

        return p;
    }

    private void runTest(List<Cause> causes, RunCondition condition, boolean expected) throws Exception  {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        FreeStyleBuild build;

        if (!causes.isEmpty()) {
            build = project.scheduleBuild2(5, causes.remove(0)).get();
        } else {
            build = project.scheduleBuild2(5).get();
        }
        if (!causes.isEmpty()) {
            // add other causes
            CauseAction act = build.getAction(CauseAction.class);

            List<Cause> all = new LinkedList<>(act.getCauses());
            all.addAll(causes);
            CauseAction newAct = new CauseAction(all);

            build.replaceAction(newAct);
        }

        System.out.println(build.getDisplayName()+" completed");
        List<Cause> buildCauses = build.getCauses();
        for (Cause cause2 : buildCauses) {
            System.out.println("DESC:" + cause2.getShortDescription());
            if (cause2 instanceof UserCause userCause) {
                System.out.println("UN:" + userCause.getUserName());
            }
        }
        StreamBuildListener listener = new StreamBuildListener(System.out,Charset.defaultCharset());

        boolean testresult = condition.runPerform(build, listener);
        assertEquals(expected, testresult);
    }

    private UserCause createUserCause(String userid) {
        User.get(userid, true, Map.of()).impersonate2();
        return new UserCause();
    }
}

