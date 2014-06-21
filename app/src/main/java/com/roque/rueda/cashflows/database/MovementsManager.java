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
package com.roque.rueda.cashflows.database;

import static com.roque.rueda.cashflows.database.AccountTable.TABLE_ACCOUNTS;
import static com.roque.rueda.cashflows.database.MovementsTable.TABLE_MOVEMENTS;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

/**
 * Class used to handle the movement of each account .
 *
 * @author Roque Rueda
 * @since 06/04/2014
 * @version 1.0
 *
 */
public class MovementsManager {

    private static final String TAG = "MovementsManager";

    private CashFlowsOpenHelper mOpenHelper;
    private Resources mResources;

    public static final String JOIN = " INNER JOIN ";

    /**
     * Creates a movement manager used to fetch data for cash movements.
     *
     * @param context Context of this application.
     */
    public MovementsManager(Context context) {
        mResources = context.getResources();
        mOpenHelper = new CashFlowsOpenHelper(context);
    }

    /**
     * Gets all the movements for the given account.
     * @param accountId Identifier of the account for witch
     * @return The movements for the current account.
     */
    public Cursor getCashMovements(long accountId) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_ACCOUNTS + JOIN + TABLE_MOVEMENTS + " ON " +
                MovementsTable.ID_ACCOUNT + " = " + AccountTable.FULL_ID);

        String orderBy = MovementsTable.FULL_MOVEMENTS_DATE + " DESC";

        // Get the database.
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (db != null) {
            return qb.query(db, null, null, null, null, null, orderBy);
        } else {
            throw new IllegalStateException("Can't get the movements for the given account.");
        }
    }

}
