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

import com.roque.rueda.cashflows.database.MovementsManager;
import com.roque.rueda.cashflows.database.observer.DataBaseObserver;

/**
 * Movements loader that is used to present the information of the movements
 * of each account.
 *
 * @author Roque Rueda
 * @since 01/06/2014
 * @version 1.0
 *
 */
public class MovementsLoader extends AsyncTaskLoader<Cursor> implements
        DataBaseObserver {

    private static final String TAG = "MovementsLoader";
    private static final boolean DEBUG = true;

    /**
     * Determines whether this class is observing for data changes or not.
     */
    private boolean mIsObserving = false;

    /**
     * Cursor that will fetch the data for all the movements of the account.
     */
    private Cursor mMovements;

    /**
     * Creates an instance of this class using the given context.
     *
     * @param context Context of the application.
     */
    public MovementsLoader(Context context) {
        super(context);

        // Since a Loader can be use on several Activity's we
        // are not going to hold a reference to the context
        // directly. We can get the reference using getContext().
    }


    /**
     * Loads the account movements on a background thread, generates
     * a {@code Cursor} that fetch data from database.
     *
     * @return {@link android.database.Cursor} with the result of the accounts.
     */
    @Override
    public Cursor loadInBackground() {
         if (DEBUG) {
             Log.i(TAG, "== loadInBackground() start loading movements. ==");
         }

        MovementsManager manager = new MovementsManager(getContext());
        int accountId = 1;
        Cursor data = manager.getCashMovements(accountId);
        return data;
    }

    /**
     * Used to display new data to the client. Super class is used
     * to deliver the data to the registered listeners. The listener
     * forward data to the client using the call to onLoadFinished.
     * @param data The instance that is result of the load.
     */
    @Override
    public void deliverResult(Cursor data) {
        // We need to override this method also in order to
        // deliver the data to the clients. Also we can use
        // several methods to know if this call comes from a
        // reset status, cancel and so on.

        if (isReset()) {
            if (DEBUG) {
                Log.i(TAG, "== Warning! A call to deliver results with a query come " +
                        "on a reset status. ==");
            }

            // The loader has been reset; ignore the result and clean the data.
            if (data != null) {
                releaseResources(data);
            }
            return;
        }

        // Prevent garbage collection.
        Cursor oldData = mMovements;
        mMovements = data;

        if (isStarted()) {
            // Deliver the result to the client.
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            if (DEBUG) {
                Log.i(TAG, "== deliverResult() releasing old movements... ==");
            }

            releaseResources(oldData);
        }
    }

    /**
     * Helper method used to release the resources of this Loader.
     * @param data Data that will be release.
     */
    private void releaseResources(Cursor data) {
        // Release resources.
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
            Log.i(TAG, "== onStartLoading() movements loader ==");
        }

        if (mMovements != null) {
            // Result is already here so we deliver it.
            deliverResult(mMovements);
        }

        if (!mIsObserving) {
            mIsObserving = true;
        }

        // Notify when a change happens on the data source.
        if (takeContentChanged()) {
            // When our Observer detects a event it will call to
            // onContentChanged on this instance, this will cause that
            // the next call to takeContentChanged() return true.
            // In this case or if the current data is null we force a new
            // load.
            if (DEBUG) {
                Log.i(TAG, "== A new change has been submit, starting force " +
                        "load for movements... ==");
            }

            // This method actually loads the data.
            forceLoad();
        } else if (mMovements == null) {
            if (DEBUG) {
                Log.i(TAG, "== No movements where found, starting force load. ==");
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
            Log.i(TAG,"== onStopLoading() Stops movements loader. ==");
        }

        // Cancel any current load.
        cancelLoad();
    }

    /**
     * Called when the loader will be reset.
     */
    @Override
    protected void onReset() {
        if (DEBUG) {
            Log.i(TAG, "== onReset() ==");
        }

        // We need stop the loader.
        onStopLoading();

        if (mMovements != null) {
            releaseResources(mMovements);
            mMovements = null;
        }

        if (mIsObserving) {
            // Stop listening for data source changes.
            mIsObserving = false;
        }
    }

    /**
     * Request to cancel any current task on the loader.
     * @param data Instance that is being load.
     */
    @Override
    public void onCanceled(Cursor data) {
        if (DEBUG) {
            Log.i(TAG, "== onCanceled() Cancel movements loader. ==");
        }
        // Cancel the current load.
        super.onCanceled(data);

        // Release resources.
        releaseResources(data);
    }

    /**
     * Request to load the data on this Loader.
     */
    @Override
    public void forceLoad() {
        if (DEBUG) {
            Log.i(TAG, "== forceLoad() Loading movements. ==");
        }

        super.forceLoad();
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
