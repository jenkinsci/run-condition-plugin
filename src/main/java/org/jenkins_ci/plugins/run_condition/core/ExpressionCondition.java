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
import org.jenkins_ci.plugins.run_condition.Messages;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.regex.Pattern;

public class ExpressionCondition extends RunCondition {

    final String expression;
    final String label;

    @DataBoundConstructor
    public ExpressionCondition(final String expression, final String label) {
        this.expression = expression;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public boolean runPrebuild(final AbstractBuild<?, ?> build, final BuildListener listener) {
        return true;
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) {
        if (expression == null) return false;
        final String expandedExpression = expand(build, listener, expression);
        String expandedLabel = expand(build, listener, label);
        if (expandedLabel == null) expandedLabel = "";
        listener.getLogger().println(Messages.expressionCondition_console_args(expandedExpression, expandedLabel));
        if (expandedExpression == null) return false;
        return expandedLabel.matches(expandedExpression);
    }

    private String expand(final AbstractBuild build, final BuildListener listener, final String template) {
        try {
            return TokenMacro.expand(build, listener, template);
        } catch (MacroEvaluationException mee) {
            throw new RuntimeException(mee);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    @Extension
    public static class ExpressionConditionDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.expressionCondition_displayName();
        }

    }

}
