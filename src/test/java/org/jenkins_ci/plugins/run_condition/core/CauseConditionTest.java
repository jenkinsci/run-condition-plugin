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
import hudson.tasks.Builder;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockBuilder;
import org.jvnet.hudson.test.Bug;

import hudson.triggers.TimerTrigger.TimerTriggerCause;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner.Run;

import org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder;

import org.junit.Test;
import java.util.Collections;

import static org.junit.Assert.assertEquals;


public class CauseConditionTest {

    public @Rule
    JenkinsRule j = new JenkinsRule();

    //-------------------------------------------------------
    //UserCause deprecated after Jenkins 1.427
    @Test
    public void testUCAnyUser() throws Exception {

        List<Cause> BuildCauses = new ArrayList<Cause>();

// tests with no users defined

        RunCondition condition = new CauseCondition("USER_CAUSE", false);

        // started by SYSTEM user
        BuildCauses.add(new UserCause());
        runtest(BuildCauses, condition, true);

        // started by user
        BuildCauses.clear();
        BuildCauses.add(createUserCause("fred"));
        runtest(BuildCauses, condition, true);

        // started by different causes
        BuildCauses.clear();
        BuildCauses.add(new LegacyCodeCause());
        runtest(BuildCauses, condition, false);


        // started by mltiple users including requested
        BuildCauses.clear();
        BuildCauses.add(createUserCause("tom"));
        BuildCauses.add(createUserCause("fred"));
        BuildCauses.add(createUserCause("harry"));

        runtest(BuildCauses, condition, true);


        // started by different causes
        BuildCauses.clear();
        BuildCauses.add(new LegacyCodeCause());
        runtest(BuildCauses, condition, false);

        // multiple different causes
        // add a second cause
        BuildCauses.add(new RemoteCause("dummy_host", "dummynote") );
        runtest(BuildCauses, condition, false);


// test with Exclusive set
        condition = new CauseCondition("USER_CAUSE", true);

        // started by correct user
        BuildCauses.clear();
        BuildCauses.add(createUserCause("fred"));
        runtest(BuildCauses, condition, true);

        // started by several users
        BuildCauses.clear();
        BuildCauses.add(createUserCause("eviloverlord"));
        BuildCauses.add(createUserCause("fred"));
        runtest(BuildCauses, condition, false);

        // started by several causes
        BuildCauses.clear();
        BuildCauses.add(createUserCause("eviloverlord"));
        BuildCauses.add(createUserCause("fred"));
        BuildCauses.add(new LegacyCodeCause());
        BuildCauses.add(new RemoteCause("dummy_host", "dummynote") );
        BuildCauses.add(new TimerTriggerCause());
        runtest(BuildCauses, condition, false);
    }


    @Bug(14438)
    @Test
    public void testMatrixUpstreamCause() throws Exception {

        // setup some Causes
        FreeStyleProject upProject = j.createFreeStyleProject("firstProject");
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
    @Bug(14438)
    @Test
    public void testMatrixUserCause() throws Exception {

        // setup some Causes
        FreeStyleProject upProject = j.createFreeStyleProject("secondProject");
        FreeStyleBuild upBuild = upProject.scheduleBuild2(0).get();

        Cause upstreamCause = new UpstreamCause(upBuild);
        Cause userCause = createUserCause("testUser");

        //test User condition
        RunCondition condition = new CauseCondition("USER_CAUSE", false);
        runMatrixTest(upstreamCause, condition, false);
        runMatrixTest(userCause, condition, true);
    }


    private void runMatrixTest(Cause buildTrigger, RunCondition condition, Boolean builderRuns) throws Exception {
        MatrixProject matrixProject = createMatrixProject();

        // if the builder should run the result for each subbuild should be unstable
        // if the builder is not to run the result for each subbuild should be success.
        Result testResult = builderRuns ? Result.UNSTABLE:Result.SUCCESS;

        // create conditional build step requirements
        List<Builder> builders = Collections.singletonList((Builder)new MockBuilder(Result.UNSTABLE));

        BuildStepRunner runner = new Run();

        // add conditional build step
        matrixProject.getBuildersList().add(new ConditionalBuilder(condition, runner, builders));

        MatrixBuild matrixBuild = matrixProject.scheduleBuild2(0, buildTrigger).get();

        List<MatrixRun> runs = matrixBuild.getRuns();
        assertEquals(4,runs.size());
        for (MatrixRun run : runs) {
            j.assertBuildStatus(testResult, run);
        }

    }

    private MatrixProject createMatrixProject() throws IOException {
        MatrixProject p = j.createProject(MatrixProject.class);

        // set up 2x2 matrix
        AxisList axes = new AxisList();
        axes.add(new TextAxis("db","mysql","oracle"));
        axes.add(new TextAxis("direction","north","south"));
        p.setAxes(axes);

        return p;
    }
    private void runtest(List<Cause> causes, RunCondition condition, boolean expected) throws Exception  {

        FreeStyleProject project = j.createFreeStyleProject();
        FreeStyleBuild build;

        if (causes.size() > 0) {
            build = project.scheduleBuild2(5, causes.remove(0)).get();
        } else {
            build = project.scheduleBuild2(5).get();
        }
        if (causes.size() > 0) {
            // add other causes
            try {
                build.getAction(CauseAction.class).getCauses().addAll(causes);
            } catch (UnsupportedOperationException ex) {
                // getCauses() return an unmodifiable collection in newer Jenkins versions
                CauseAction act = build.getAction(CauseAction.class);

                List<Cause> all = new LinkedList<Cause>(act.getCauses());
                all.addAll(causes);
                CauseAction newAct = new CauseAction(all);

                Method m = build.getClass().getMethod("removeAction", Action.class);
                m.invoke(build, act);

                build.addAction(newAct);
            }
        }

        System.out.println(build.getDisplayName()+" completed");
        List<Cause> buildCauses = build.getCauses();
        for (Cause cause2 : buildCauses) {
            System.out.println("DESC:" + cause2.getShortDescription());
            if (Cause.UserCause.class.isInstance(cause2)) {
                UserCause userCause = (UserCause)cause2;
                System.out.println("UN:" + userCause.getUserName());
            }
        }
        StreamBuildListener listener = new StreamBuildListener(System.out,Charset.defaultCharset());

        boolean testresult = condition.runPerform(build, listener);
        assertEquals(expected, testresult);
    }

    private UserCause createUserCause(String userid) {
        Authentication a = new UsernamePasswordAuthenticationToken(userid, userid);

        a = j.jenkins.getSecurityRealm().getSecurityComponents().manager.authenticate(a);

        SecurityContextHolder.getContext().setAuthentication(a);
        UserCause cause = new UserCause();

        return cause;
    }
}

