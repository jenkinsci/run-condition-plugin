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
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.util.FormValidation;
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;

public class NumericalComparisonCondition extends RunCondition {

    final String lhs;
    final String rhs;
    final Comparator comparator;

    @DataBoundConstructor
    public NumericalComparisonCondition(final String lhs, final String rhs, final Comparator comparator) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.comparator = comparator;
    }

    public String getLhs() {
        return lhs;
    }

    public String getRhs() {
        return rhs;
    }

    public Comparator getComparator() {
        return comparator;
    }

    @Override
    public boolean runPrebuild(final AbstractBuild<?, ?> build, final BuildListener listener) {
        return true;
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) throws Exception {
        final String leftString = TokenMacro.expand(build, listener, lhs);
        final double left = Double.parseDouble(leftString);
        final String rightString = TokenMacro.expand(build, listener, rhs);
        final double right = Double.parseDouble(rightString);
        listener.getLogger().println(Messages.numericalComparison_console_args(left, comparator.getDescriptor().getDisplayName(), right));
        return comparator.isTrue(left, right);
    }

    @Extension
    public static class NumericalComparisonConditionDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.numericalComparison_displayName();
        }

        public List<? extends Descriptor<? extends Comparator>> getComparators() {
            return Hudson.getInstance().<Comparator, Comparator.ComparatorDescriptor>getDescriptorList(Comparator.class);
        }

        public FormValidation doCheckLhs(@QueryParameter final String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckRhs(@QueryParameter final String value) {
            return FormValidation.validateRequired(value);
        }

    }

    public static abstract class Comparator implements Describable<Comparator> {
        public abstract boolean isTrue(double lhs, double rhs);
        public Descriptor<Comparator> getDescriptor() {
            return (Descriptor) Hudson.getInstance().getDescriptor(getClass());
        }
        public static abstract class ComparatorDescriptor extends Descriptor<Comparator> {
        }
    }

    public static class LessThan extends Comparator {
        @DataBoundConstructor public LessThan() {}
        public boolean isTrue(double lhs, double rhs) {
            return lhs < rhs;
        }
        @Extension(ordinal = 0)
        public static class LessThanDescriptor extends ComparatorDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.numericalComparison_lessThan();
            }
        }
    }

    public static class GreaterThan extends Comparator {
        @DataBoundConstructor public GreaterThan() {}
        public boolean isTrue(double lhs, double rhs) {
            return lhs > rhs;
        }
        @Extension(ordinal = -1)
        public static class GreaterThanDescriptor extends ComparatorDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.numericalComparison_greaterThan();
            }
        }
    }

    public static class EqualTo extends Comparator {
        @DataBoundConstructor public EqualTo() {}
        public boolean isTrue(double lhs, double rhs) {
            return lhs == rhs;
        }
        @Extension(ordinal = -2)
        public static class EqualToDescriptor extends ComparatorDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.numericalComparison_equalTo();
            }
        }
    }

    public static class NotEqualTo extends Comparator {
        @DataBoundConstructor public NotEqualTo() {}
        public boolean isTrue(double lhs, double rhs) {
            return lhs != rhs;
        }
        @Extension(ordinal = -3)
        public static class NotEqualToDescriptor extends ComparatorDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.numericalComparison_notEqualTo();
            }
        }
    }

    public static class LessThanOrEqualTo extends Comparator {
        @DataBoundConstructor public LessThanOrEqualTo() {}
        public boolean isTrue(double lhs, double rhs) {
            return lhs <= rhs;
        }
        @Extension(ordinal = -4)
        public static class LessThanOrEqualToDescriptor extends ComparatorDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.numericalComparison_lessThanOrEqualTo();
            }
        }
    }

    public static class GreaterThanOrEqualTo extends Comparator {
        @DataBoundConstructor public GreaterThanOrEqualTo() {}
        public boolean isTrue(double lhs, double rhs) {
            return lhs >= rhs;
        }
        @Extension(ordinal = -5)
        public static class GreaterThanOrEqualToDescriptor extends ComparatorDescriptor {
            @Override
            public String getDisplayName() {
                return Messages.numericalComparison_greaterThanOrEqualTo();
            }
        }
    }

}
