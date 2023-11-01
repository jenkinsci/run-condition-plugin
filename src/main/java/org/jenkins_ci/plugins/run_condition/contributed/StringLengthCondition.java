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
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * This condition runs if the length of a string matches a given criteria.
 * 
 * @author Anthony Wat
 */
public class StringLengthCondition extends AlwaysPrebuildRunCondition {

	@Extension
	public static class StringLengthConditionDescriptor extends RunConditionDescriptor {

		/**
		 * Validates the <code>length</code> field.
		 * 
		 * @return <code>FormValidation.ok()</code> if validation is successful; <code>FormValidation.error()</code>
		 *         with an error message otherwise.
		 */
		public FormValidation doCheckLength(@QueryParameter String value) {
			boolean ok;
			try {
				long longValue = Long.valueOf(value);
				ok = (longValue >= 0);
			} catch (NumberFormatException e) {
				ok = false;
			}
			return ok ? FormValidation.ok() : FormValidation.error(Messages.FileLengthCondition_doCheckLength_errMsg());
		}

		/**
		 * Returns the <code>ListBoxModel</code> object containing drop-down options for the <code>when</code> field.
		 * 
		 * @return The <code>ListBoxModel</code> object containing drop-down options for the <code>when</code> field.
		 */
		public ListBoxModel doFillWhenItems() {
			ListBoxModel lbm = new ListBoxModel();
			for (When whenOption : When.values()) {
				lbm.add(whenOption.getDisplayName(), whenOption.getName());
			}
			return lbm;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		@Override
		public String getDisplayName() {
			return Messages.StringLengthCondition_displayName();
		}

	}

	/**
	 * The comparison length.
	 */
	final String length;

	/**
	 * The string to compare length on.
	 */
	final String string;

	/**
	 * Whether the string should be trimmed before the length comparison.
	 */
	final boolean trim;

	/**
	 * The comparison type.
	 */
	final String when;

	/**
	 * Constructs a <code>StringLengthCondition</code> object.
	 * 
	 * @param string
	 *            The string to compare length on.
	 * @param trim
	 *            Whether the string should be trimmed before the length comparison.
	 * @param length
	 *            The comparison length.
	 * @param when
	 *            The comparison type.
	 */
	@DataBoundConstructor
	public StringLengthCondition(String string, boolean trim, String length, String when) {
		this.string = string;
		this.trim = trim;
		this.length = length;
		this.when = when;
	}

	/**
	 * Returns the comparison length.
	 * 
	 * @return The comparison length.
	 */
	public String getLength() {
		return length;
	}

	/**
	 * Returns the string to compare length on.
	 * 
	 * @return The string to compare length on.
	 */
	public String getString() {
		return string;
	}

	/**
	 * Returns the comparison type.
	 * 
	 * @return The comparison type.
	 */
	public String getWhen() {
		return when;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jenkins_ci.plugins.run_condition.RunCondition#runPerform(hudson.model .AbstractBuild,
	 * hudson.model.BuildListener)
	 */
	@Override
	public boolean runPerform(AbstractBuild<?, ?> build, BuildListener listener) throws Exception {
		String expandedString = TokenMacro.expandAll(build, listener, string);
		listener.getLogger().println(Messages.StringLengthCondition_console_args(expandedString, trim, length, when));
		// Let exception be thrown when file does not exist
		long stringLength = trim ? expandedString.trim().length() : expandedString.length();
		long lengthValue = Long.valueOf(length);
		for (When whenEnumValue : When.values()) {
			if (whenEnumValue.getName().equals(when)) {
				return whenEnumValue.compare(stringLength, lengthValue);
			}
		}
		return false;
	}

	/**
	 * Returns whether the string should be trimmed before the length comparison.
	 * 
	 * @return <code>true</code> if the string should be trimmed before the length comparison; <code>false</code>
	 *         otherwise.
	 */
	public boolean trim() {
		return trim;
	}

}
