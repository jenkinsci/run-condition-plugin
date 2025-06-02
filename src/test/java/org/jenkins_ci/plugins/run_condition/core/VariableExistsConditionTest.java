package org.jenkins_ci.plugins.run_condition.core;

import hudson.EnvVars;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;

import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@WithJenkins
class VariableExistsConditionTest {

    private JenkinsRule jenkinsRule;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @Test
    void checkingEnvironmentVariables() {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars env = prop.getEnvVars();
        env.put("value", "1");
        jenkinsRule.jenkins.getGlobalNodeProperties().add(prop);
        EnvironmentVariablesNodeProperty values= (EnvironmentVariablesNodeProperty) jenkinsRule.jenkins.getGlobalNodeProperties().get(0);
        assertNotEquals("Value", values.getEnv().get(0).key);
        assertEquals("value", values.getEnv().get(0).key);
    }

    @Test
    void variableExistsConditionSuccess(){
        VariableExistsCondition variable1 = new VariableExistsCondition("Hello");
        VariableExistsCondition variable2 = new VariableExistsCondition(null);
        assertEquals("Hello", variable1.getVariableName());
        assertNull(variable2.getVariableName());
    }

    @Test
    void variableExistsConditionComparingResult(){
        VariableExistsCondition variable1 = new VariableExistsCondition("Hello");
        VariableExistsCondition variable2 = new VariableExistsCondition(null);
        assertNotEquals("hello", variable1.getVariableName());
        assertNotEquals("Hello", variable2.getVariableName());
    }

}