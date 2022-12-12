package org.jenkins_ci.plugins.run_condition.core;

import hudson.EnvVars;
import hudson.model.*;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static hudson.model.Result.*;
import static hudson.model.Result.NOT_BUILT;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class VariableExistsConditionTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test

    public void checkingEnvironmentVariables() throws IOException, InterruptedException {

         EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
         EnvVars env = prop.getEnvVars();
         env.put("value", "1");
         jenkinsRule.jenkins.getGlobalNodeProperties().add(prop);
         EnvironmentVariablesNodeProperty values= (EnvironmentVariablesNodeProperty) jenkinsRule.jenkins.getGlobalNodeProperties().get(0);
         assertEquals(false,values.getEnv().get(0).key.equals("Value"));
         assertEquals(true,values.getEnv().get(0).key.equals("value"));
      }

      @Test
    public void VariableExistsConditionSuccess(){

        VariableExistsCondition variable1 = new VariableExistsCondition("Hello");
        VariableExistsCondition variable2 = new VariableExistsCondition(null);
        assertEquals(variable1.getVariableName(),"Hello");
        assertEquals(variable2.getVariableName(),null);
    }
    @Test
    public void VariableExistsConditionComparingResult(){

        VariableExistsCondition variable1 = new VariableExistsCondition("Hello");
        VariableExistsCondition variable2 = new VariableExistsCondition(null);
        assertNotEquals(variable1.getVariableName(),"hello");
        assertNotEquals(variable2.getVariableName(),"Hello");

    }

}