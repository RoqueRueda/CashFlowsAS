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

import static com.roque.rueda.cashflows.database.AccountTable.ACCOUNT_END_BALANCE;
import static com.roque.rueda.cashflows.database.AccountTable.ACCOUNT_INITIAL_BALANCE;
import static com.roque.rueda.cashflows.database.AccountTable.ACCOUNT_NAME;
import static com.roque.rueda.cashflows.database.AccountTable.ID_PERIOD;
import static com.roque.rueda.cashflows.database.AccountTable.TABLE_ACCOUNTS;
import static com.roque.rueda.cashflows.database.AccountTable.PHOTO_NUMBER;
import static com.roque.rueda.cashflows.database.PeriodTable.ACTIVE;
import static com.roque.rueda.cashflows.database.PeriodTable.END_DATE;
import static com.roque.rueda.cashflows.database.PeriodTable.NAME;
import static com.roque.rueda.cashflows.database.PeriodTable.START_DATE;
import static com.roque.rueda.cashflows.database.PeriodTable.TABLE_PERIODS;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.roque.rueda.cashflows.R;

/**
 * Class used to handle the accounts of the cash flows.
 * 
 * @author Roque Rueda
 * @since 06/04/2014
 * @version 1.0
 *
 */
public class AccountManager {
	
	private static final String TAG = "AccountManager";

    /**
     * Inner Join sentence.
     */
    public static final String JOIN = " INNER JOIN ";

    private CashFlowsOpenHelper mOpenHelper;
    private Resources mResources;
    private String periodName;
    private String bank_account;
    private String cashAccount;
	
	/**
	 * Creates an instance with the default values.
	 */
	public AccountManager() {
		// Nothing...
	}

	/**
	 * Creates an account manager to fetch data from the SQLite database.
     *
	 * @param context {@link android.content.Context} Context of the application.
	 */
	public AccountManager(Context context) {
		mResources = context.getResources();
        mOpenHelper = new CashFlowsOpenHelper(context);
	}
	
	/**
	 * Get all the accounts for the current active period.
	 * @return Cursor instance with the accounts.
	 */
	public Cursor getAccountsForCurrentPeriod() {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_PERIODS + JOIN + TABLE_ACCOUNTS +
				" ON " + AccountTable.ID_PERIOD + " = " + PeriodTable.FULL_ID);
		
		String orderBy = AccountTable.ACCOUNT_NAME + " DESC";
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (db != null) {
            return qb.query(db, null, PeriodTable.ACTIVE + " = 1", null, null, null, orderBy);
        } else {
            throw new IllegalStateException("Can't get the accounts from the database. SQLiteDatabase is null.");
        }
    }

    public Cursor getFinalBalance() {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_PERIODS + JOIN + TABLE_ACCOUNTS +
                " ON " + AccountTable.ID_PERIOD + " = " + PeriodTable.FULL_ID);
        qb.appendWhere(PeriodTable.ACTIVE + " = 1");

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (db != null) {
            return qb.query(db, new String[] { "SUM(" + AccountTable.FULL_ACCOUNT_END_BALANCE + ")" },
                    null, null, null, null, null);
        } else {
            throw new IllegalStateException("Can't get the total balance from the database. SQLiteDatabase is null.");
        }

    }

    /**
     * Initial load of information to the database.
     * @param db {@link android.database.sqlite.SQLiteDatabase} that will be used to insert
     *           data into the database.
     * @param rs {@link android.content.res.Resources} where this initial load will get the propert string names for
     *           each account.
     *
     */
	public void initialLoad(SQLiteDatabase db, Resources rs) {
		// Insert the default values in the accounts.
		try {
            db.beginTransaction();

			ContentValues initialValues = new ContentValues();
			
			/**
			 * Insert the first period.
			 */
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
			String todayDate = sdf.format(new Date());
			initialValues.put(START_DATE, todayDate);
            periodName = rs.getString(R.string.initial_period);
			initialValues.put(NAME, periodName);
			initialValues.put(ACTIVE, 1);
			initialValues.put(END_DATE, todayDate);
			
			long periodId = db.insert(TABLE_PERIODS, null, initialValues);
			
			/**
			 * Insert the first account.
			 */
			initialValues.clear();
			initialValues.put(ACCOUNT_INITIAL_BALANCE, 0.00);
            bank_account = rs.getString(R.string.bank_account);
			initialValues.put(ACCOUNT_NAME, bank_account);
			initialValues.put(ACCOUNT_END_BALANCE, 0.00);
			initialValues.put(PHOTO_NUMBER, 1);
			initialValues.put(ID_PERIOD, periodId);
			
			db.insert(TABLE_ACCOUNTS, null, initialValues);
			
			/**
			 * Insert the second account.
			 */
			initialValues.clear();
			initialValues.put(ACCOUNT_INITIAL_BALANCE, 0.00);
            cashAccount = rs.getString(R.string.cash_account);
			initialValues.put(ACCOUNT_NAME, cashAccount);
			initialValues.put(ACCOUNT_END_BALANCE, 0.00);
			initialValues.put(PHOTO_NUMBER, 2);
			initialValues.put(ID_PERIOD, periodId);
			
			db.insert(TABLE_ACCOUNTS, null, initialValues);

            db.setTransactionSuccessful();

		} catch (SQLException sqlEx) {
			Log.wtf(TAG, "Error inserting initial values, " + sqlEx.getMessage());
            throw sqlEx;
		} finally {
            db.endTransaction();
        }
    }
	
	
}
