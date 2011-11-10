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

package org.jenkins_ci.plugins.run_condition.common;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

public abstract class BaseDirectory implements Describable<BaseDirectory> {

    public abstract FilePath getBaseDirectory(AbstractBuild<?, ?> build);

    public Descriptor<BaseDirectory> getDescriptor() {
        return (Descriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    public static abstract class BaseDirectoryDescriptor extends Descriptor<BaseDirectory> {}

    public static class Workspace extends BaseDirectory {
        @DataBoundConstructor public Workspace() {}

        @Override
        public FilePath getBaseDirectory(final AbstractBuild<?, ?> build) {
            return build.getWorkspace();
        }

        @Extension(ordinal = -1)
        public static class WorkspaceDescriptor extends BaseDirectoryDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.baseDirectory_workspace_displayName();
            }
        }

    }

    public static class ArtifactsDir extends BaseDirectory {
        @DataBoundConstructor public ArtifactsDir() {}

        @Override
        public FilePath getBaseDirectory(final AbstractBuild<?, ?> build) {
            return new FilePath(Hudson.getInstance().getRootPath(), build.getArtifactsDir().getAbsolutePath());
        }

        @Extension(ordinal = -2)
        public static class ArtifactsDirDescriptor extends BaseDirectoryDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.baseDirectory_artifacts_displayName();
            }
        }

    }

    public static class JenkinsHome extends BaseDirectory {
        @DataBoundConstructor public JenkinsHome() {}

        @Override
        public FilePath getBaseDirectory(final AbstractBuild<?, ?> build) {
            return Hudson.getInstance().getRootPath();
        }

        @Extension(ordinal = -3)
        public static class JenkinsHomeDescriptor extends BaseDirectoryDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.baseDirectory_jenkinsHome_displayName();
            }
        }

    }


}
