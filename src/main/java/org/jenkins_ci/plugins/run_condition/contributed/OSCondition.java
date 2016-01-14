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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkins_ci.plugins.run_condition.contributed;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * This condition runs if the current operating system is of a given type.
 * 
 * @author Anthony Wat
 */
public class OSCondition extends AlwaysPrebuildRunCondition {

	@Extension
	public static class OSConditionDescriptor extends RunConditionDescriptor {

		/**
		 * The prefix of the names of the constants for OS family in the Ant
		 * <code>Os</code> class.
		 */
		public static final String FAMILY_CONST_PREFIX = "FAMILY_";

		/**
		 * Validates whether at least one of the four fields are specified.
		 * 
		 * @param value
		 *            The value of the family field.
		 * @param name
		 *            The value of the name field.
		 * @param arch
		 *            The value of the architecture field.
		 * @param version
		 *            The value of the version field.
		 * @return <code>FormValidation.ok()</code> if validation is successful;
		 *         <code>FormValidation.error()</code> with an error message
		 *         otherwise.
		 */
		public FormValidation doCheckFamily(@QueryParameter String value,
				@QueryParameter String name, @QueryParameter String arch,
				@QueryParameter String version) {
			// Empty fields are not passed in as null values but empty strings
			// from Jenkins
			if (value.length() == 0 && name.length() == 0 && arch.length() == 0
					&& version.length() == 0) {
				return FormValidation
						.error(Messages.OSCondition_doCheckFamily_errMsg());
			}
			return FormValidation.ok();
		}

		/**
		 * Returns the <code>ListBoxModel</code> object containing drop-down
		 * options for the <code>family</code> field.
		 * 
		 * @return The <code>ListBoxModel</code> object containing drop-down
		 *         options for the <code>family</code> field.
		 * @throws IllegalAccessException
		 */
		public ListBoxModel doFillFamilyItems() throws IllegalAccessException {
			ListBoxModel lbm = new ListBoxModel();

			// Add empty option to the top
			lbm.add("");

			// Introspects the Os class for supported OS family values and
			// dynamically populate the drop-down list options for the family
			// field
			Set<String> familyOptions = new TreeSet<String>();
			Field[] fields = this.clazz.getDeclaredFields();
			for (Field field : fields) {
				if (Modifier.isPublic(field.getModifiers())
						&& Modifier.isStatic(field.getModifiers())
						&& field.getType().equals(String.class)
						&& field.getName().startsWith(FAMILY_CONST_PREFIX)) {
					familyOptions.add((String) field.get(String.class));
				}
			}
			for (String familyOption : familyOptions) {
				lbm.add(familyOption);
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
			return Messages.OSCondition_displayName();
		}

	}

	/*
	 * This constants are copied from Apache Ant 1.9.4's
	 * <code>org.apache.tools.ant.taskdefs.condition.Os</code> class and adapted
	 * for use in this Jenkins run condition.
	 */

	private static final String DARWIN = "darwin";

	public static final String FAMILY_9X = "win9x";

	public static final String FAMILY_DOS = "dos";

	public static final String FAMILY_MAC = "mac";

	public static final String FAMILY_NETWARE = "netware";

	public static final String FAMILY_NT = "winnt";

	public static final String FAMILY_OS2 = "os/2";

	public static final String FAMILY_OS400 = "os/400";

	public static final String FAMILY_TANDEM = "tandem";

	public static final String FAMILY_UNIX = "unix";

	public static final String FAMILY_VMS = "openvms";

	public static final String FAMILY_WINDOWS = "windows";

	public static final String FAMILY_ZOS = "z/os";

	/**
	 * The architecture of the operating system family to expect.
	 */
	final String arch;

	/**
	 * The family of the operating system family to expect.
	 */
	final String family;

	/**
	 * The name of the operating system family to expect.
	 */
	final String name;

	/**
	 * The version of the operating system family to expect.
	 */
	final String version;

	@DataBoundConstructor
	public OSCondition(String family, String name, String arch,
			String version) {
		this.family = family;
		this.name = name;
		this.arch = arch;
		this.version = version;
	}

	/**
	 * The architecture of the operating system family to expect.
	 * 
	 * @return The architecture of the operating system family to expect.
	 */
	public String getArch() {
		return this.arch;
	}

	/**
	 * Returns the family of the operating system family to expect.
	 * 
	 * @return The family of the operating system family to expect.
	 */
	public String getFamily() {
		return this.family;
	}

	/**
	 * Return the name of the operating system family to expect.
	 * 
	 * @return The name of the operating system family to expect.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * The version of the operating system family to expect.
	 * 
	 * @return The version of the operating system family to expect.
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * This method is copied from Apache Ant 1.9.4's
	 * <code>org.apache.tools.ant.taskdefs.condition.Os</code> class and adapted
	 * for use in this Jenkins run condition. Determines if the OS on which Ant
	 * is executing matches the given OS family.
	 * 
	 * @param family
	 *            The OS family.
	 * @return <code>true</code> if the OS matches; <code>false</code>
	 *         otherwise.
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private boolean isFamily(String family)
			throws IOException, InterruptedException {
		return isOs(family, null, null, null);
	}

	/**
	 * This method is copied from Apache Ant 1.9.4's
	 * <code>org.apache.tools.ant.taskdefs.condition.Os</code> class and adapted
	 * for use in this Jenkins run condition. Determines if the OS on which Ant
	 * is executing matches the given OS family, name, architecture and version.
	 * 
	 * @param family
	 *            The OS family.
	 * @param name
	 *            The OS name.
	 * @param arch
	 *            The OS architecture.
	 * @param version
	 *            The OS version.
	 * @return <code>true</code> if the OS matches; <code>false</code>
	 *         otherwise.
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private boolean isOs(String family, String name, String arch,
			String version) throws IOException, InterruptedException {
		// Obtain the required JVM system properties from the Computer that is
		// currently running the build
		Computer computer = Computer.currentComputer();
		Map<Object, Object> sysProps = computer.getSystemProperties();
		String sysPropOsName = ((String) sysProps.get("os.name"))
				.toLowerCase(Locale.ENGLISH);
		String sysPropOsArch = ((String) sysProps.get("os.arch"))
				.toLowerCase(Locale.ENGLISH);
		String sysPropOsVersion = ((String) sysProps.get("os.version"))
				.toLowerCase(Locale.ENGLISH);
		String sysPropPathSep = (String) sysProps.get("path.separator");

		boolean retValue = false;

		if (family != null || name != null || arch != null || version != null) {

			boolean isFamily = true;
			boolean isName = true;
			boolean isArch = true;
			boolean isVersion = true;

			if (family != null) {

				// windows probing logic relies on the word 'windows' in
				// the OS
				boolean isWindows = sysPropOsName.indexOf(FAMILY_WINDOWS) > -1;
				boolean is9x = false;
				boolean isNT = false;
				if (isWindows) {
					// there are only four 9x platforms that we look for
					is9x = (sysPropOsName.indexOf("95") >= 0
							|| sysPropOsName.indexOf("98") >= 0
							|| sysPropOsName.indexOf("me") >= 0
							// wince isn't really 9x, but crippled enough to
							// be a muchness. Ant doesn't run on CE, anyway.
							|| sysPropOsName.indexOf("ce") >= 0);
					isNT = !is9x;
				}
				if (family.equals(FAMILY_WINDOWS)) {
					isFamily = isWindows;
				} else if (family.equals(FAMILY_9X)) {
					isFamily = isWindows && is9x;
				} else if (family.equals(FAMILY_NT)) {
					isFamily = isWindows && isNT;
				} else if (family.equals(FAMILY_OS2)) {
					isFamily = sysPropOsName.indexOf(FAMILY_OS2) > -1;
				} else if (family.equals(FAMILY_NETWARE)) {
					isFamily = sysPropOsName.indexOf(FAMILY_NETWARE) > -1;
				} else if (family.equals(FAMILY_DOS)) {
					isFamily = sysPropPathSep.equals(";")
							&& !isFamily(FAMILY_NETWARE);
				} else if (family.equals(FAMILY_MAC)) {
					isFamily = sysPropOsName.indexOf(FAMILY_MAC) > -1
							|| sysPropOsName.indexOf("dawrin") > -1;
				} else if (family.equals(FAMILY_TANDEM)) {
					isFamily = sysPropOsName.indexOf("nonstop_kernel") > -1;
				} else if (family.equals(FAMILY_UNIX)) {
					isFamily = sysPropPathSep.equals(":")
							&& !isFamily(FAMILY_VMS)
							&& (!isFamily(FAMILY_MAC)
									|| sysPropOsName.endsWith("x")
									|| sysPropOsName.indexOf("dawrin") > -1);
				} else if (family.equals(FAMILY_ZOS)) {
					isFamily = sysPropOsName.indexOf(FAMILY_ZOS) > -1
							|| sysPropOsName.indexOf("os/390") > -1;
				} else if (family.equals(FAMILY_OS400)) {
					isFamily = sysPropOsName.indexOf(FAMILY_OS400) > -1;
				} else if (family.equals(FAMILY_VMS)) {
					isFamily = sysPropOsName.indexOf(FAMILY_VMS) > -1;
				} else {
					// Comment out this throw statement and return false instead
					// in the run condition
					/*
					 * throw new BuildException(
					 * "Don\'t know how to detect os family \"" + family +
					 * "\"");
					 */
					return false;
				}
			}
			if (name != null) {
				isName = name.equals(sysPropOsName);
			}
			if (arch != null) {
				isArch = arch.equals(sysPropOsArch);
			}
			if (version != null) {
				isVersion = version.equals(sysPropOsVersion);
			}
			retValue = isFamily && isName && isArch && isVersion;
		}
		return retValue;
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
		// Treat empty values as null
		String expandedName = (name.length() == 0) ? null
				: TokenMacro.expandAll(build, listener, name);
		String expandedArch = (arch.length() == 0) ? null
				: TokenMacro.expandAll(build, listener, arch);
		String expandedVersion = (version.length() == 0) ? null
				: TokenMacro.expandAll(build, listener, version);
		listener.getLogger().println(Messages.OSCondition_console_args(family,
				expandedName, expandedArch, expandedVersion));
		return isOs(family, expandedName, expandedArch, expandedVersion);
	}

}
