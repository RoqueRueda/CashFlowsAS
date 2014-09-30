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
package com.roque.rueda.cashflows.util;


import android.content.Context;
import android.graphics.Typeface;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Util class used to format some strings on the application.
 *
 * @author Roque Rueda
 * @since 07/09/2014
 * @version 1.0
 *
 */
public class StringFormatter {

    private static Typeface font;

    /**
     * Format a date to be presented to the user.
     * @param d Date that will be formatted.
     * @return return a String with the formatted value.
     */
    public static String formatDate(Date d) {
        DateFormat dateAndTimeFormat = DateFormat.getDateTimeInstance();
        return dateAndTimeFormat.format(d);
    }

    /**
     * Formats a double to be presented to the user.
     * @param d Double value that will be formatted.
     * @return String with the formatted vale.
     */
    public static String formatCurrency(double d) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        return currencyFormat.format(d);
    }

    /**
     * Gets a double value from a parsed String.
      * @param s String that will be parsed to decimal.
     * @return Double value from the parsed string.
     */
    public static double getDecimalValue(String s) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        try {
            Number result = currencyFormat.parse(s);
            return result.doubleValue();
        } catch (ParseException e) {
            return 0d;
        }
    }

    public static Typeface createLightFont(){
        // Lazy load.
        if (font == null) {
            font = Typeface.create("sans-serif-light", Typeface.NORMAL);

        }

        return font;
    }

}
