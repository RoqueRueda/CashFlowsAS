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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.roque.rueda.cashflows.database.AccountManager;
import com.roque.rueda.cashflows.database.observer.DataBaseObserver;

/**
 * Account loader gets the information on a spinner for an account.
 *
 * @author Roque Rueda
 * @since 31/07/2014
 * @version 1.0
 *
 */
public class SpinnerAccountLoader extends AsyncTaskLoader<Cursor>
        implements DataBaseObserver {

    private static final String TAG = "SpinnerAccountLoader";
    private static final boolean DEBUG = true;

    /**
     * Indicate if this loader is observing for changes.
     */
    private boolean mIsObserving = false;

    /**
     * Reference to our data.
     */
    private Cursor mAccounts;

    /**
     * Creates a Loader that can be use to fetch data to a account
     * spinner.
     * @param context Context of the application.
     */
    public SpinnerAccountLoader(Context context) {
        super(context);

        // Assign context to our super class
        // since we can use this loader from several
        // activity's.
    }

    /**
     * Method use to load the data on a background thread, generates
     * the {@code Cursor} that we will use to hold the data and pass to the
     * client.
     * @return {@code Cursor} with accounts.
     */
    @Override
    public Cursor loadInBackground() {

        if (DEBUG) {
            Log.i(TAG, "== loadInBackground() start to load the accounts. ==");
        }

        AccountManager manager = new AccountManager(getContext());
        Cursor data = manager.getShortAccountInfo();

        return data;
    }

    /**
     * Used to display new data to the client. Super class is used
     * to deliver the data to the registered listeners. The listener
     * forward data to the client using the call to onLoadFinished.
     */
    @Override
    public void deliverResult(Cursor data) {

        // We need to override this method also in order to
        // deliver the data to the clients. Also we can use
        // several methods to know if this call comes from a
        // reset status, cancel and so on.

        if (isReset()) {
            if (DEBUG) {
                Log.i(TAG, "== Warning! A call to deliver result with a query come on a " +
                        "reset status. ==");
            }

            // The loader has been reset; ignore the result and clean the data.
            if (data != null) {
                releaseResources(data);
            }
            return;
        }

        // Prevent garbage collector.
        Cursor oldData = mAccounts;
        mAccounts = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            if (DEBUG) {
                Log.i(TAG, "== deliverResult() releasing old data... == ");
            }

            releaseResources(oldData);
        }

    }

    /**
     * Release the resources.
     * @param data Cursor that will be release.
     */
    private void releaseResources(Cursor data) {

        if (data != null) {
            data.close();
            data = null;
        }
    }

    /**
     * Handles a request to start this Loader.
     */
    @Override
    protected void onStartLoading() {
        if (DEBUG) {
            Log.i(TAG, "== onStartLoading() ==");
        }

        if (mAccounts != null) {
            // We have the value already.
            deliverResult(mAccounts);
        }

        if (!mIsObserving) {
            mIsObserving = true;
        }

        // A change on our date has just happen.
        if (takeContentChanged()) {

            // When our Observer detects a event it will call to
            // onContentChanged on this instance, this will cause that
            // the next call to takeContentChanged() return true.
            // In this case or if the current data is null we force a new
            // load.

            if (DEBUG) {
                Log.i(TAG, "== A new change has been submit, " +
                        "starting force load... ==");
            }

            // This method actually loads the data.
            forceLoad();
        } else if (mAccounts == null) {
            if (DEBUG) {
                Log.i(TAG, "== No accounts found, starting force load. ==");
            }

            // This method actually loads the data.
            forceLoad();
        }
    }

    /**
     * Called when the loader is going to stop.
     */
    @Override
    protected void onStopLoading() {
        if (DEBUG) {
            Log.i(TAG, "== onStopLoading() Stops the account loader... ==");
        }

        // Stop any current load.
        cancelLoad();
    }

    /**
     * Called when the loader will be reset.
     */
    @Override
    protected void onReset() {
        if (DEBUG) {
            Log.i(TAG, "== onReset() Reset for account loader... ==");
        }

        // We need to make sure that his loader is stopped.
        onStopLoading();

        if (mAccounts != null) {
            releaseResources(mAccounts);
            mAccounts = null;
        }

        // Stop observer.
        if (mIsObserving) {
            mIsObserving = false;
        }

    }

    @Override
    public void onCanceled(Cursor data) {

        if (DEBUG) {
            Log.i(TAG, "== onCanceled() Cancel account loader. ==");
        }

        // Cancel the current load.
        super.onCanceled(data);

        // Release resources.
        releaseResources(data);
    }

    //////////////////////////////////////////////////////////////
    // DataObserver members.
    //////////////////////////////////////////////////////////////

    /**
     * Notify the observer that a change on a table has been made.
     *
     * @param tableName Name of the table that was change.
     */
    @Override
    public void notifyTableChange(String tableName) {
        // Send a notification that the contend has being changed.
        if (mIsObserving) {
            onContentChanged();
        }
    }

    /**
     * Notify that a database change has happen and the information
     * should be re query.
     */
    @Override
    public void notifyDatabaseChange() {
        if (mIsObserving) {
            onContentChanged();
        }
    }
}
