/*
 * The MIT License
 *
 * Copyright (C) 2011 by Anthony Robinson
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

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.util.FormValidation;
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.jenkins_ci.plugins.run_condition.common.BaseDirectory;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;

public final class FileExistsCondition extends AlwaysPrebuildRunCondition {

    final String file;
    final BaseDirectory baseDir;

    @DataBoundConstructor
    public FileExistsCondition(final String file, final BaseDirectory baseDir) {
        this.file = file;
        this.baseDir = baseDir;
    }

    public BaseDirectory getBaseDir() {
        return baseDir;
    }

    public String getFile() {
        return file;
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        final String expandedFile = TokenMacro.expandAll(build, listener, file);
        return baseDir.getBaseDirectory(build).child(expandedFile).exists();
    }

    @Extension
    public static class FileExistsConditionDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.fileExistsCondition_displayName();
        }

        public List<? extends Descriptor<? extends BaseDirectory>> getBaseDirectories() {
            return Hudson.getInstance().<BaseDirectory, BaseDirectory.BaseDirectoryDescriptor>getDescriptorList(BaseDirectory.class);
        }

        public FormValidation doCheckFile(@QueryParameter final String value) {
            return FormValidation.validateRequired(value);
        }

    }

}
