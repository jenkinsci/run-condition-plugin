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

/**
 * An enum that models the options for the <code>when</code> field.
 * 
 * @author Anthony Wat
 */
public enum When {

	/**
	 * Equal to.
	 */
	EQ("eq", Messages.When_eq_displayName()) {
		@Override
		boolean compare(long l1, long l2) {
			return l1 == l2;
		}
	},

	/**
	 * Greater than or equal to.
	 */
	GE("ge", Messages.When_ge_displayName()) {
		@Override
		boolean compare(long l1, long l2) {
			return l1 >= l2;
		}
	},

	/**
	 * Greater than.
	 */
	GT("gt", Messages.When_gt_displayName()) {
		@Override
		boolean compare(long l1, long l2) {
			return l1 > l2;
		}
	},

	/**
	 * Less than or equal to.
	 */
	LE("le", Messages.When_le_displayName()) {
		@Override
		boolean compare(long l1, long l2) {
			return l1 <= l2;
		}
	},

	/**
	 * Less than.
	 */
	LT("lt", Messages.When_lt_displayName()) {
		@Override
		boolean compare(long l1, long l2) {
			return l1 < l2;
		}
	},

	/**
	 * Not equal to.
	 */
	NE("ne", Messages.When_ne_displayName()) {
		@Override
		boolean compare(long l1, long l2) {
			return l1 != l2;
		}
	};

	/**
	 * The display name.
	 */
	private final String displayName;

	/**
	 * The name.
	 */
	private final String name;

	/**
	 * Constructs a <code>When</code> enum.
	 * 
	 * @param name
	 *            The name.
	 * @param displayName
	 *            The display name.
	 */
	When(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	/**
	 * Compares two longs and return whether the left long operand conforms with
	 * the comparison criteria represented by this enum.
	 * 
	 * @param l1
	 *            The left long operand.
	 * @param l2
	 *            The right long operand.
	 * @return <code>true</code> if the left long operand conforms with the
	 *         comparison criteria represented by this enum; <code>false</code>
	 *         otherwise.
	 */
	abstract boolean compare(long l1, long l2);

	/**
	 * Returns the display name.
	 * 
	 * @return The display name.
	 */
	String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the name.
	 * 
	 * @return Returns the name.
	 */
	String getName() {
		return name;
	}

}
