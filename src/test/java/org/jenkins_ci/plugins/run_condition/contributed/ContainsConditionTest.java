/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 Anthony Wat
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

package org.jenkins_ci.plugins.run_condition.contributed;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.resetAll;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;

/**
 * Unit test class for <code>ContainsCondition</code>.
 * 
 * @author Anthony Wat
 *
 */
@PrepareForTest(TokenMacro.class)
@RunWith(PowerMockRunner.class)
public class ContainsConditionTest {

	/**
	 * Creates a mock <code>BuildListener</code> object that creates a dummy logger.
	 * 
	 * @return A mock <code>BuildListener</code> object that creates a dummy logger.
	 */
	private BuildListener mockBuildListener() {
		BuildListener listener = createNiceMock(BuildListener.class);
		expect(listener.getLogger()).andReturn(new PrintStream(new ByteArrayOutputStream())).anyTimes();
		replay(listener);
		return listener;
	}

	/**
	 * Mocks the <code>TokenMacro.expandAll()</code> method to return the given string as-is without any processing.
	 * 
	 * @param strs
	 *            The strings that <code>TokenMacro.expandAll()</code> returns back.
	 * 
	 * @throws Exception
	 */
	private void mockTokenMacro(String... strs) throws Exception {
		mockStatic(TokenMacro.class);
		for (String str : strs) {
			expect(TokenMacro.expandAll(anyObject(AbstractBuild.class), anyObject(TaskListener.class), eq(str)))
					.andReturn(str).anyTimes();
		}
		replay(TokenMacro.class);
	}

	@After
	public void tearDown() {
		resetAll();
	}

	@Test
	public void testRunPerform_withDifferentCaseStrings_shouldReturnValidBoolean() throws Exception {
		String str = "some string";
		String substr = "SoMe";
		mockTokenMacro(str, substr);
		BuildListener listener = mockBuildListener();
		assertEquals(true, new ContainsCondition(str, substr, false).runPerform(null, listener));
		assertEquals(false, new ContainsCondition(str, substr, true).runPerform(null, listener));
	}

	@Test
	public void testRunPerform_withDifferentStrings_shouldReturnValidBoolean() throws Exception {
		String str = "one string";
		String substr = "another string";
		mockTokenMacro(str, substr);
		BuildListener listener = mockBuildListener();
		assertEquals(false, new ContainsCondition(str, substr, false).runPerform(null, listener));
		assertEquals(false, new ContainsCondition(str, substr, true).runPerform(null, listener));
	}

	@Test
	public void testRunPerform_withEmptyStringAndSubstring_shouldReturnValidBoolean() throws Exception {
		String str = "";
		mockTokenMacro(str);
		BuildListener listener = mockBuildListener();
		assertEquals(true, new ContainsCondition(str, str, false).runPerform(null, listener));
	}

	@Test
	public void testRunPerform_withEmptySubstring_shouldReturnValidBoolean() throws Exception {
		String str = "one string";
		String substr = "";
		mockTokenMacro(str, substr);
		BuildListener listener = mockBuildListener();
		assertEquals(true, new ContainsCondition(str, substr, false).runPerform(null, listener));
		assertEquals(true, new ContainsCondition(str, substr, true).runPerform(null, listener));
	}

	@Test
	public void testRunPerform_withSameStringAndSubstring_shouldReturnValidBoolean() throws Exception {
		String str = "same string";
		mockTokenMacro(str);
		BuildListener listener = mockBuildListener();
		assertEquals(true, new ContainsCondition(str, str, true).runPerform(null, listener));
		assertEquals(true, new ContainsCondition(str, str, false).runPerform(null, listener));
	}

	@Test
	public void testRunPerform_withVanillaValidSubstring_ShouldReturnValidBoolean() throws Exception {
		String str = "one string";
		String substr = "str";
		mockTokenMacro(str, substr);
		BuildListener listener = mockBuildListener();
		assertEquals(true, new ContainsCondition(str, substr, false).runPerform(null, listener));
		assertEquals(true, new ContainsCondition(str, substr, true).runPerform(null, listener));
	}

}
