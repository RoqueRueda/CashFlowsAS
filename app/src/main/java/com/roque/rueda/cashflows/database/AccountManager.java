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
import static com.roque.rueda.cashflows.database.MovementsTable.ID_ACCOUNT;
import static com.roque.rueda.cashflows.database.MovementsTable.MOVEMENTS_AMOUNT;
import static com.roque.rueda.cashflows.database.MovementsTable.MOVEMENTS_SING;
import static com.roque.rueda.cashflows.database.MovementsTable.TABLE_MOVEMENTS;
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
		
		String orderBy = AccountTable.FULL_ACCOUNT_NAME + " DESC";
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (db != null) {
            String[] columns = new String[] { AccountTable.FULL_ID,
                AccountTable.FULL_INITIAL_BALANCE, AccountTable.FULL_ACCOUNT_NAME,
                AccountTable.FULL_ACCOUNT_END_BALANCE, AccountTable.FULL_ACCOUNT_NUMBER };
            return qb.query(db, columns, PeriodTable.FULL_ACTIVE + " = 1",
                    null, null, null, orderBy);
        } else {
            throw new IllegalStateException("Can't get the accounts from the database. " +
                    "SQLiteDatabase is null.");
        }
    }

    /**
     * Gets the final balance as the sum of all the end balance of each account.
     * @return Cursor with one tow as the sum of all the final balance of each account.
     */
    public Cursor getFinalBalance() {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_PERIODS + JOIN + TABLE_ACCOUNTS +
                " ON " + AccountTable.ID_PERIOD + " = " + PeriodTable.FULL_ID);
        qb.appendWhere(PeriodTable.ACTIVE + " = 1");

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (db != null) {
            return qb.query(db, new String[] { "SUM(" +
                            AccountTable.FULL_ACCOUNT_END_BALANCE + ")" },
                    null, null, null, null, null);
        } else {
            throw new IllegalStateException("Can't get the total balance from the database. " +
                    "SQLiteDatabase is null.");
        }

    }

    /**
     * Gets the basic information for all the accounts for the current period.
     * (Name, Picture and Id).
     *
     * @return Return a cursor with the result data of the query.
     */
    public Cursor getShortAccountInfo() {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_PERIODS + JOIN + TABLE_ACCOUNTS +
                " ON " + AccountTable.ID_PERIOD + " = " + PeriodTable.FULL_ID);

        String orderBy = AccountTable.FULL_ACCOUNT_NAME + " DESC";

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (db != null) {
            String[] columns = new String[] { AccountTable.FULL_ID,
                    AccountTable.FULL_ACCOUNT_NAME, AccountTable.FULL_ACCOUNT_NUMBER };
            return qb.query(db, columns, PeriodTable.FULL_ACTIVE + " = 1",
                    null, null, null, orderBy);
        } else {
            throw new IllegalStateException("Can't get the accounts from the database. " +
                    "SQLiteDatabase is null.");
        }
    }

    /**
     * Gets the balance of the account.
     * @param idAccount Account identifier.
     * @param db Database use to build to perform query's.
     * @return Balance of the account or -1 if an error happens.
     */
    public static double getAccountBalance(long idAccount, SQLiteDatabase db) {

        Cursor positiveBalance = db.rawQuery("SELECT SUM(" + MOVEMENTS_AMOUNT + ") FROM "
                        + TABLE_MOVEMENTS + " WHERE " + ID_ACCOUNT + " = ?" +
                        " AND " + MOVEMENTS_SING + " = '+'", new String[]{ String.valueOf(idAccount) });
        Cursor negativeBalance = db.rawQuery("SELECT SUM(ABS(" + MOVEMENTS_AMOUNT + ")) FROM "
                + TABLE_MOVEMENTS + " WHERE " + ID_ACCOUNT + " = ?" +
                " AND " + MOVEMENTS_SING + " = '-'", new String[]{ String.valueOf(idAccount) });

        double positiveValues = 0;
        if (positiveBalance.moveToFirst()) {
            positiveValues = positiveBalance.getDouble(0);
        }

        double negativeValues = 0;
        if (negativeBalance.moveToFirst()) {
            negativeValues = negativeBalance.getDouble(0);
        }
        final double finalBalance = positiveValues - negativeValues;

        return finalBalance;

        //Cursor balanceFromDatabase = getBalanceCursor(idAccount, db);
        // Initialize the variable with the invalid value.
        // double accountBalance = -1;

        // Check if the cursor have any values
        // if (balanceFromDatabase.moveToFirst()) {
        //     accountBalance = balanceFromDatabase.getDouble(0);
        // }

        // return accountBalance;
    }

    /**
     * Performs a query to the database in order to get the final balance.
     * @param idAccount Account identifier.
     * @param db Database use to build to perform query's.
     * @return android.database.Cursor instance with the result.
     */
    private static Cursor getBalanceCursor(long idAccount, SQLiteDatabase db) {
        Cursor balanceFromDatabase = db.rawQuery("SELECT (SELECT SUM(" + MOVEMENTS_AMOUNT + ") FROM "
                + TABLE_MOVEMENTS + " WHERE " + ID_ACCOUNT + " = ?" +
                " AND " + MOVEMENTS_SING + " = '+') - " +
                "(SELECT SUM(" + MOVEMENTS_AMOUNT + ") FROM "
                + TABLE_MOVEMENTS + " WHERE " + ID_ACCOUNT + " = ?" +
                " AND " + MOVEMENTS_SING + " = '-')", new String[]{ String.valueOf(idAccount),
                String.valueOf(idAccount) });

        return balanceFromDatabase;
    }


    /**
     * Gets the account balance.
     *
     * @param idAccount Account identifier.
     * @return Sum of all account movements minus all negative movements.
     */
    public Cursor getAccountBalance(long idAccount) {
        return getBalanceCursor(idAccount, mOpenHelper.getReadableDatabase());
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
