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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.roque.rueda.cashflows.database.AccountTable;
import com.roque.rueda.cashflows.database.CashFlowsOpenHelper;
import com.roque.rueda.cashflows.database.MovementsTable;
import com.roque.rueda.cashflows.model.Movement;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.roque.rueda.cashflows.database.MovementsTable.ID_ACCOUNT;
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
    private static final int SUM_COLUMN_INDEX = 0;
    private static final String TAG = "AddNegativeCash";

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
        double amount = m.getAmount();

        // Convert the value into negative.
        if (amount > 0) {
            m.setAmount(amount * -1);
        }

        values.put(MOVEMENTS_AMOUNT, m.getAmount());
        values.put(MOVEMENTS_DESCRIPTION, m.getDescription());

        // Date format to save a date as a string.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());

        values.put(MOVEMENTS_DATE, dateFormat.format(m.getDate()));
        values.put(MOVEMENTS_SING, m.getSing());
        values.put(ID_ACCOUNT, m.getIdAccount());

        // Columns for the sum of the account.
        String[] columns = new String[]{ "SUM(" + MOVEMENTS_AMOUNT + ")" };

        // Get a database.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor totalAccount = db.query(MovementsTable.TABLE_MOVEMENTS, columns,
                    ID_ACCOUNT + " = " + m.getIdAccount(), null, null, null, null);
            double endBalance = 0;
            if (totalAccount.moveToFirst()) {
                endBalance = totalAccount.getDouble(SUM_COLUMN_INDEX);
            }

            // Save the movement.
            m.setId(db.insert(TABLE_MOVEMENTS, null, values));

            // Update final balance
            endBalance -= m.getAmount();
            values.clear();
            values.put(AccountTable.ACCOUNT_END_BALANCE, endBalance);

            Log.i(TAG, "Saving a negative cash movement of " + m.getAmount() +
                    " in the account " + m.getIdAccount());

            Log.i(TAG, "Saving final balance " + endBalance +
                    " in the account " + m.getIdAccount());

            int affectedRows = db.update(AccountTable.TABLE_ACCOUNTS, values,
                    AccountTable._ID + " = " + m.getIdAccount(), null);
            db.setTransactionSuccessful();

            // Return the operation result.
            return (m.getId() > 0 && affectedRows > 0);

        } catch (SQLiteException sqlEx) {
            Log.e(TAG, "Problem saving a negative movement: " + sqlEx.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }
}
