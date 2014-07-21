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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.roque.rueda.cashflows.database.CashFlowsOpenHelper;
import com.roque.rueda.cashflows.model.Movement;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.roque.rueda.cashflows.database.MovementsTable.MOVEMENTS_AMOUNT;
import static com.roque.rueda.cashflows.database.MovementsTable.MOVEMENTS_DESCRIPTION;
import static com.roque.rueda.cashflows.database.MovementsTable.MOVEMENTS_DATE;
import static com.roque.rueda.cashflows.database.MovementsTable.MOVEMENTS_SING;
import static com.roque.rueda.cashflows.database.MovementsTable.TABLE_MOVEMENTS;

/**
 * Instance used to store a negative value in the database.
 *
 * @author Roque Rueda
 * @since 26/05/2014
 * @version 1.0
 *
 */
public class AddNegativeCash implements AddCashState{


    private Context mContext;
    private CashFlowsOpenHelper mOpenHelper;

    public AddNegativeCash(Context ctx) { mContext = ctx; }

    /**
     * Save a new cash movement.
     *
     * @param m {@link com.roque.rueda.cashflows.model.Movement} contains a set of variables
     *          used as parameter to set the values on the database.
     * @return true if the operation add a new cash movement.
     */
    @Override
    public boolean saveCashMovement(Movement m) {

        mOpenHelper = new CashFlowsOpenHelper(mContext);

        ContentValues values = new ContentValues();

        // Convert the value into negative.
        if (m.amount > 0) {
            m.amount = m.amount * -1;
        }

        values.put(MOVEMENTS_AMOUNT, m.amount);
        values.put(MOVEMENTS_DESCRIPTION, m.description);

        // Date format to save a date as a string.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());

        values.put(MOVEMENTS_DATE, dateFormat.format(m.date));
        values.put(MOVEMENTS_SING, m.sing);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        m.id = db.insert(TABLE_MOVEMENTS, null, values);

        return (m.id > 0);
    }
}
