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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.jenkins_ci.plugins.run_condition.common.BaseDirectory;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;

/**
 * Unit test class for <code>FileLengthCondition</code>.
 * 
 * @author Anthony Wat
 *
 */
@PrepareForTest({ TokenMacro.class, FilePath.class })
@RunWith(PowerMockRunner.class)
public class FileLengthConditionTest {

	/**
	 * The name of a file.
	 */
	public static final String FILE_NAME = "file.txt";

	/**
	 * The size of a file.
	 */
	public static final long FILE_SIZE = 100;

	/**
	 * Creates a mock <code>BaseDirectory</code> object that returns a dummy file size given a dummy file name.
	 * 
	 * @return A mock <code>BaseDirectory</code> object that returns a dummy file size given a dummy file name.
	 * 
	 * @throws Exception
	 */
	private BaseDirectory mockBaseDirectory() throws Exception {
		FilePath filePath = createNiceMock(FilePath.class);
		expect(filePath.length()).andReturn(FILE_SIZE).anyTimes();
		FilePath baseDirFilePath = createNiceMock(FilePath.class);
		expect(baseDirFilePath.child(FILE_NAME)).andReturn(filePath).anyTimes();
		BaseDirectory baseDir = createNiceMock(BaseDirectory.class);
		expect(baseDir.getBaseDirectory(anyObject(AbstractBuild.class))).andReturn(baseDirFilePath).anyTimes();
		replay(filePath, baseDirFilePath, baseDir);
		return baseDir;
	}

	/**
	 * Creates a mock <code>BuildListener</code> object that creates a dummy logger.
	 * 
	 * @return A mock <code>BuildListener</code> object that creates a dummy logger.
	 */
	private BuildListener mockBuildListener() {
		BuildListener listener = createNiceMock(BuildListener.class);
		expect(listener.getLogger()).andReturn(new PrintStream(new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				// Do nothing
			}

		})).anyTimes();
		replay(listener);
		return listener;
	}

	/**
	 * Mock the <code>TokenMacro.expandAll()</code> method to return the given string as-is without any processing.
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

	/**
	 * Runs an assertion of <code>StringLengthCondition.runPerform()</code> given the input and conditions.
	 * 
	 * @param expectedValue
	 *            The expected value from the test.
	 * @param str
	 *            The string to compare length on.
	 * @param trim
	 *            Whether the string should be trimmed before the length comparison.
	 * @param length
	 *            The comparison length.
	 * @param when
	 *            The comparison type.
	 * @throws Exception
	 */
	private void runAssertion(boolean expectedValue, String fileName, long length, When when) throws Exception {
		assertEquals(expectedValue,
				new FileLengthCondition(mockBaseDirectory(), fileName, Long.toString(length), when.getName())
						.runPerform(null, mockBuildListener()));
	}

	@Before
	public void setUp() throws Exception {
		mockTokenMacro(FILE_NAME);
	}

	@Test
	public void testRunPerform_WithEqual_ShouldReturnValidBoolean() throws Exception {
		When when = When.EQ;
		runAssertion(true, FILE_NAME, FILE_SIZE, when);
		runAssertion(false, FILE_NAME, FILE_SIZE + 1, when);
	}

	@Test
	public void testRunPerform_WithGreaterThan_ShouldReturnValidBoolean() throws Exception {
		When when = When.GT;
		runAssertion(false, FILE_NAME, FILE_SIZE, when);
		runAssertion(true, FILE_NAME, FILE_SIZE - 1, when);
	}

	@Test
	public void testRunPerform_WithGreaterThanOrEqual_ShouldReturnValidBoolean() throws Exception {
		When when = When.GE;
		runAssertion(true, FILE_NAME, FILE_SIZE, when);
		runAssertion(true, FILE_NAME, FILE_SIZE - 1, when);
	}

	@Test
	public void testRunPerform_WithLessThan_ShouldReturnValidBoolean() throws Exception {
		When when = When.LT;
		runAssertion(false, FILE_NAME, FILE_SIZE, when);
		runAssertion(true, FILE_NAME, FILE_SIZE + 1, when);
	}

	@Test
	public void testRunPerform_WithLessThanOrEqual_ShouldReturnValidBoolean() throws Exception {
		When when = When.LE;
		runAssertion(true, FILE_NAME, FILE_SIZE, when);
		runAssertion(true, FILE_NAME, FILE_SIZE + 1, when);
	}

	@Test
	public void testRunPerform_WithNotEqual_ShouldReturnValidBoolean() throws Exception {
		When when = When.NE;
		runAssertion(false, FILE_NAME, FILE_SIZE, when);
		runAssertion(true, FILE_NAME, FILE_SIZE + 1, when);
	}

}
