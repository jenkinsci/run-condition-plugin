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

import java.util.List;

import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.jenkins_ci.plugins.run_condition.common.BaseDirectory;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * This condition runs if the length of a file matches a given criteria.
 * 
 * @author Anthony Wat
 */
public class FileLengthCondition extends AlwaysPrebuildRunCondition {

	@Extension
	public static class FileLengthConditionDescriptor
			extends RunConditionDescriptor {

		/**
		 * Validates the <code>file</code> field.
		 * 
		 * @return <code>FormValidation.ok()</code> if validation is successful;
		 *         <code>FormValidation.error()</code> with an error message
		 *         otherwise.
		 */
		public FormValidation doCheckFile(@QueryParameter String value) {
			return FormValidation.validateRequired(value);
		}

		/**
		 * Validates the <code>length</code> field.
		 * 
		 * @return <code>FormValidation.ok()</code> if validation is successful;
		 *         <code>FormValidation.error()</code> with an error message
		 *         otherwise.
		 */
		public FormValidation doCheckLength(@QueryParameter String value) {
			boolean ok;
			try {
				long longValue = Long.valueOf(value);
				ok = (longValue >= 0);
			} catch (NumberFormatException e) {
				ok = false;
			}
			return ok ? FormValidation.ok()
					: FormValidation.error(Messages
							.FileLengthCondition_doCheckLength_errMsg());
		}

		/**
		 * Returns the <code>ListBoxModel</code> object containing drop-down
		 * options for the <code>when</code> field.
		 * 
		 * @return The <code>ListBoxModel</code> object containing drop-down
		 *         options for the <code>when</code> field.
		 */
		public ListBoxModel doFillWhenItems() {
			ListBoxModel lbm = new ListBoxModel();
			for (When whenOption : When.values()) {
				lbm.add(whenOption.getDisplayName(), whenOption.getName());
			}
			return lbm;
		}

		/**
		 * Returns the list of base directories.
		 * 
		 * @return The list of base directories.
		 */
		public List<? extends Descriptor<? extends BaseDirectory>> getBaseDirectories() {
			return Hudson.getInstance().<BaseDirectory, BaseDirectory
					.BaseDirectoryDescriptor> getDescriptorList(
							BaseDirectory.class);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		@Override
		public String getDisplayName() {
			return Messages.FileLengthCondition_displayName();
		}

	}

	/**
	 * The base directory of the file.
	 */
	final BaseDirectory baseDir;

	/**
	 * The file relative to the base directory to compare length on.
	 */
	final String file;

	/**
	 * The comparison length.
	 */
	final String length;

	/**
	 * The comparison type.
	 */
	final String when;

	/**
	 * Constructs a <code>FileLengthCondition</code> object.
	 * 
	 * @param baseDir
	 *            The base directory of the file.
	 * @param file
	 *            The file relative to the base directory to compare length on.
	 * @param length
	 *            The comparison length.
	 * @param when
	 *            The comparison type.
	 */
	@DataBoundConstructor
	public FileLengthCondition(BaseDirectory baseDir, String file,
			String length, String when) {
		this.baseDir = baseDir;
		this.file = file;
		this.length = length;
		this.when = when;
	}

	/**
	 * Returns the base directory of the file.
	 * 
	 * @return The base directory of the file.
	 */
	public BaseDirectory getBaseDir() {
		return baseDir;
	}

	/**
	 * Returns the file relative to the base directory to compare length on.
	 * 
	 * @return The file relative to the base directory to compare length on.
	 */
	public String getFile() {
		return file;
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
	 * @see
	 * org.jenkins_ci.plugins.run_condition.RunCondition#runPerform(hudson.model
	 * .AbstractBuild, hudson.model.BuildListener)
	 */
	@Override
	public boolean runPerform(AbstractBuild<?, ?> build, BuildListener listener)
			throws Exception {
		String expandedFile = TokenMacro.expandAll(build, listener, file);
		listener.getLogger().println(Messages.FileLengthCondition_console_args(
				baseDir, expandedFile, length, when));
		// Let exception be thrown when file does not exist
		long fileLength = baseDir.getBaseDirectory(build).child(expandedFile)
				.length();
		long lengthValue = Long.valueOf(length);
		for (When whenEnumValue : When.values()) {
			if (whenEnumValue.getName().equals(when)) {
				return whenEnumValue.compare(fileLength, lengthValue);
			}
		}
		return false;
	}

}
