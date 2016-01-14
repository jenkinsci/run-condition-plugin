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

import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

/**
 * This run condition runs if a string contains another.
 * 
 * @author Anthony Wat
 * 
 */
public class ContainsCondition extends AlwaysPrebuildRunCondition {

	@Extension
	public static class ContainsConditionDescriptor
			extends RunConditionDescriptor {

		/*
		 * (non-Javadoc)
		 * 
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		@Override
		public String getDisplayName() {
			return Messages.ContainsCondition_displayName();
		}

	}

	/**
	 * Whether a case-sensitive comparison should be performed.
	 */
	final boolean caseSensitive;

	/**
	 * The string to search in.
	 */
	final String string;

	/**
	 * The string to search for.
	 */
	final String substring;

	@DataBoundConstructor
	public ContainsCondition(String string, String substring,
			boolean caseSensitive) {
		this.string = string;
		this.substring = substring;
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Returns the string to search in.
	 * 
	 * @return The string to search in.
	 */
	public String getString() {
		return string;
	}

	/**
	 * Returns the string to search for.
	 * 
	 * @return The string to search for.
	 */
	public String getSubstring() {
		return substring;
	}

	/**
	 * Returns whether a case-sensitive comparison should be performed.
	 * 
	 * @return <code>true</code> if a case-sensitive comparison should be
	 *         performed; <code>false</code> otherwise.
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jenkins_ci.plugins.run_condition.RunCondition#runPerform(hudson.model
	 * .AbstractBuild, hudson.model.BuildListener)
	 */
	@Override
	public boolean runPerform(AbstractBuild<?, ?> build, BuildListener listener)
			throws Exception {
		String expandedString = TokenMacro.expandAll(build, listener, string);
		String expandedSubstring = TokenMacro.expandAll(build, listener,
				substring);
		listener.getLogger().println(Messages.ContainsCondition_console_args(
				expandedString, expandedSubstring));
		return caseSensitive ? expandedString.contains(expandedSubstring)
				: expandedString.toLowerCase()
						.contains(expandedSubstring.toLowerCase());
	}

}
