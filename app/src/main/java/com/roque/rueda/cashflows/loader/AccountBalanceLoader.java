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
package com.roque.rueda.cashflows.loader;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.roque.rueda.cashflows.database.AccountManager;
import com.roque.rueda.cashflows.database.observer.DataBaseObserver;

/**
 * Loads the balance of an account.
 *
 * @author Roque Rueda
 * @since 13/11/2014
 * @version 1.0
 *
 */
public class AccountBalanceLoader extends AsyncTaskLoader<Cursor>
        implements DataBaseObserver {

    private static final String TAG = "AccountBalanceLoader";
    private static final boolean DEBUG = true;

    /**
     * Determines whether this class is observing for changes.
     */
    private boolean mIsObserving = false;

    /**
     * Reference to data.
     */
    private Cursor mAccountBalance;

    /**
     * Account id that will be retrieve.
     */
    private long mIdAccount;

    /**
     * Creates an instance of this class with the given context.
     * @param context Context of the application.
     */
    public AccountBalanceLoader(Context context) {
        super(context);
    }

    /**
     * Set the account id in order to filter the balance.
     * @param id Id if the account.
     */
    public void setIdAccount(long id) {
        mIdAccount = id;
    }

    /**
     * Loads the information on a background thread, generates
     * the {@code Cursor} that we will use to hold the data and pass to the
     * client.
     *
     * @return {@code Cursor} with the balance.
     */
    @Override
    public Cursor loadInBackground() {
        if (DEBUG) {
            Log.i(TAG, "== loadInBackground() start to load the balance account. ==");
        }

        AccountManager manager = new AccountManager(getContext());
        Cursor data = manager.getAccountBalance(mIdAccount);
        return data;
    }

    /**
     * Use to display
     * @param data data that will be release.
     */
    @Override
    public void deliverResult(Cursor data) {

        // If we are on reset status release resources.
        if (isReset()) {
            if (DEBUG) {
                Log.i(TAG, "Deliver result on a reset status.");
            }

            if (data != null) {
                releaseResources(data);
            }
            return;
        }

        // Keep a reference to avoid garbage collection.
        Cursor oldData = mAccountBalance;
        mAccountBalance = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            if (DEBUG) {
                Log.i(TAG, "Release old data");
            }

            releaseResources(oldData);
        }
    }

    /**
     * Release the give cursor.
     * @param cursor Cursor that will be release.
     */
    private void releaseResources(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

    /**
     * Called when this loader starts.
     */
    @Override
    protected void onStartLoading() {

        if (DEBUG) {
            Log.i(TAG, "onStartLoading()!");
        }

        if (mAccountBalance != null) {
            // Deliver the result.
            deliverResult(mAccountBalance);
        }

        if (!mIsObserving) {
            mIsObserving = true;
        }

        if (takeContentChanged()) {
            if (DEBUG) {
                Log.i(TAG, "Data has been changed, starting force load...");
            }

            // Load data again.
            forceLoad();
        } else if (mAccountBalance == null) {
            if (DEBUG) {
                Log.i(TAG, "No data, starting force load.");
            }

            forceLoad();
        }
    }

    /**
     * Called when the loader is going to stop.
     */
    @Override
    protected void onStopLoading() {
        if (DEBUG) {
            Log.i(TAG, "Stop account balance loader.");
        }

        cancelLoad();
    }

    /**
     * Called when the loader will be reset.
     */
    @Override
    protected void onReset() {

        if (DEBUG) {
            Log.i(TAG, "Reset the account loader.");
        }

        // Make sure that the loader is stopped.
        onStopLoading();

        if (mAccountBalance != null) {
            releaseResources(mAccountBalance);
        }

        if (mIsObserving) {
            mIsObserving = false;
        }
    }

    @Override
    public void onCanceled(Cursor data) {
        if (DEBUG) {
            Log.i(TAG, "Cancel loader.");
        }

        // Cancel this loader.
        super.onCanceled(data);

        releaseResources(data);
    }

    //////////////////////////////////////////////////////////////
    // DataObserver members.
    //////////////////////////////////////////////////////////////

    /**
     * Method used to get a notification when a change
     * on the table is made.
     *
     */
    @Override
    public void notifyTableChange(String tableName) {
        // Send a notification that the contend has being changed.
        if (mIsObserving) {
            onContentChanged();
        }

    }

    /**
     * Method used to notify for any change on the database.
     */
    @Override
    public void notifyDatabaseChange() {
        if (mIsObserving) {
            onContentChanged();
        }
    }


}
