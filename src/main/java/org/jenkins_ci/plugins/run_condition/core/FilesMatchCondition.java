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
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.jenkins_ci.plugins.run_condition.common.BaseDirectory;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

public class FilesMatchCondition extends RunCondition {

    final String includes;
    final String excludes;
    final BaseDirectory baseDir;

    @DataBoundConstructor
    public FilesMatchCondition(final String includes, final String excludes, final BaseDirectory baseDir) {
        this.includes = includes;
        this.excludes = excludes;
        this.baseDir = baseDir;
    }

    public BaseDirectory getBaseDir() {
        return baseDir;
    }

    public String getIncludes() {
        return includes;
    }

    public String getExcludes() {
        return excludes;
    }

    @Override
    public boolean runPrebuild(final AbstractBuild<?, ?> build, final BuildListener listener) {
        return true;
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        final FilePath directory = baseDir.getBaseDirectory(build);
        final int matched = directory.exists()
                ? baseDir.getBaseDirectory(build).list(getFixedUpIncludes(), Util.fixEmptyAndTrim(excludes)).length
                : 0;
        listener.getLogger().println(Messages.filesMatchCondition_console_matched(matched));
        return matched > 0;
    }

    private String getFixedUpIncludes() {
        final String trimmedIncludes = Util.fixEmptyAndTrim(includes);
        return trimmedIncludes == null ? "**" : trimmedIncludes;
    }

    @Extension
    public static class FilesMatchConditionDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.filesMatchCondition_displayName();
        }

        public List<? extends Descriptor<? extends BaseDirectory>> getBaseDirectories() {
            return Hudson.getInstance().<BaseDirectory, BaseDirectory.BaseDirectoryDescriptor>getDescriptorList(BaseDirectory.class);
        }

    }

}
