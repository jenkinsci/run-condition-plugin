package org.jenkins_ci.plugins.run_condition.core;

import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static hudson.model.Result.*;
import static hudson.model.Result.NOT_BUILT;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class VariableExistsConditionTest {


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