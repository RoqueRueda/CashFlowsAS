/*
 * Copyright 2014 Roque Rueda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.roque.rueda.cashflows.hepers;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Input filter used to check the input value on a text edit view.
 *
 * @author Roque Rueda
 * @since 11/09/2014
 * @version 1.0
 *
 */
public class DecimalDigitsInputFiler implements InputFilter {

    private Pattern mPattern;

    /**
     * Creates an instance using the arguments to create a pattern to filter
     * the input.
     * @param integerPositions Number of digits before "." dot.
     * @param decimalPositions Number of digits after "." dot.
     */
    public DecimalDigitsInputFiler(int integerPositions, int decimalPositions) {
        mPattern = Pattern.compile("[0-9]{0," + (integerPositions -1) + "}+" +
                "((\\.[0-9]{0," + (decimalPositions -1) + "})?)||(\\.)?");
    }

    /**
     * This method is called when the buffer is going to replace the
     * range <code>dstart &hellip; dend</code> of <code>dest</code>
     * with the new text from the range <code>start &hellip; end</code>
     * of <code>source</code>.  Return the CharSequence that you would
     * like to have placed there instead, including an empty string
     * if appropriate, or <code>null</code> to accept the original
     * replacement.  Be careful to not to reject 0-length replacements,
     * as this is what happens when you delete text.  Also beware that
     * you should not attempt to make any changes to <code>dest</code>
     * from this method; you may only examine it for context.
     * <p/>
     * Note: If <var>source</var> is an instance of {@link android.text.Spanned} or
     * {@link Spannable}, the span objects in the <var>source</var> should be
     * copied into the filtered result (i.e. the non-null return value).
     * {@link TextUtils#copySpansFrom} can be used for convenience.
     *
     * @param source Source that will be filter
     * @param start start value.
     * @param end end value.
     * @param dest Spanned object that contains the text value.
     * @param dstart
     * @param dend
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        Matcher matcher = mPattern.matcher(dest);

        if(!matcher.matches()) {
            return "";
        }

        return null;
    }
}
