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
package com.roque.rueda.cashflows.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.roque.rueda.cashflows.R;
import com.roque.rueda.cashflows.R.layout;
import com.roque.rueda.cashflows.database.observer.DataBaseObserver;
import com.roque.rueda.cashflows.database.observer.DatabaseMessenger;
import com.roque.rueda.cashflows.loader.BalanceLoader;

import java.util.LinkedHashSet;

/**
 * Activity used to present the list of cash movements.
 * 
 * @author Roque Rueda
 * @since 22/05/2014
 * @version 1.0
 * 
 */
public class MovementsListFragment extends ListFragment implements DatabaseMessenger,
        LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = "MovementsListFragment";
	private static final boolean DEBUG = true;

    private static final int LOADER_BALANCE = 2;
    private LinkedHashSet<DataBaseObserver> mObservers;
    private TextView mTotalBalance;
    private BalanceLoader mLoader;
	
	/**
	 * Constant used to retrieve the account id from the extras.
	 */
	public static final String ARG_ACCOUNT_ID = "AccountId";

    /**
     * Value indicating if this activity is showing the content as
     * two pane.
     */
    public static final String ARG_TWO_PANE = "IsTwoPane";
	
	private long mIdAccount = -1;
    private boolean mShowBalance = false;
	
	/**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovementsListFragment() {
    	// This is required by Android.
    }
	
	/**
	 * Called when the activity is created.
	 * @param saveInstanceState			State of the application.
	 */
	@Override
	public void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);
		
		if (DEBUG) {
			Log.i(TAG, "== AccountMovementsActivity starting ==");
		}
		
		if (getArguments().containsKey(ARG_ACCOUNT_ID)) {
			// Get the id.
			mIdAccount = getArguments().getLong(ARG_ACCOUNT_ID);
		} else {
			if (DEBUG) {
				// Print a message when no Id is found.
				Log.w(TAG, "== No id is found for AccountMovementsActivity ==");
			}
		}

        if (getArguments().containsKey(ARG_TWO_PANE)) {
            mShowBalance = getArguments().getBoolean(ARG_TWO_PANE);
        }

        mObservers = new LinkedHashSet<DataBaseObserver>();

	}
	
	/**
	 * 
	 * Handle the creation of the view.
	 * 
	 * @param inflater Inflater used to generate the view
	 * @param container View that is used as the parent of this fragment.
	 * @param savedInstanceState Arguments used to create the view.
	 * @return View that will present this fragment.
	 */
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		if (DEBUG) {
			Log.i(TAG, "== onCreateView() ==");
		}

        View rootView = inflater.inflate(R.layout.cash_movements, container, false);

        if (mShowBalance) {
            rootView.findViewById(R.id.balance_container).setVisibility(View.VISIBLE);
            mTotalBalance = (TextView) rootView.findViewById(R.id.total_balance);
        }

        mLoader = (BalanceLoader) getLoaderManager().
                initLoader(LOADER_BALANCE, null, this);
        register(mLoader);

        return rootView;
    }

    ////////////////////////////////////////////////////////////////////
    // DatabaseMessenger Interface members..
    ////////////////////////////////////////////////////////////////////

    /**
     * Register a new object into the list of observers to be notify by this instance.
     *
     * @param observer Observer that will be add to the list.
     */
    @Override
    public void register(DataBaseObserver observer) {
        mObservers.add(observer);
    }

    /**
     * Removes a existing observer from the list of observers to be notified
     * by this instance.
     *
     * @param observer Observer that will be removed of the list.
     */
    @Override
    public void unregister(DataBaseObserver observer) {
        mObservers.remove(observer);
    }

    /**
     * Sends a notification to the list of observers with the table
     * name that has been change.
     *
     * @param tableName Name of the table that has been change.
     */
    @Override
    public void sendNotification(String tableName) {
        for (DataBaseObserver observer : mObservers) {
            observer.notifyTableChange(tableName);
        }
    }

    /**
     * Notify the list of observer when a change on the database
     * has been made.
     */
    @Override
    public void sendNotification() {
        for (DataBaseObserver observer : mObservers) {
            observer.notifyDatabaseChange();
        }
    }

    ////////////////////////////////////////////////////////////////////
    // LoaderCallbacks Interface members..
    ////////////////////////////////////////////////////////////////////

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOADER_BALANCE: {
                if (DEBUG) {
                    Log.i(TAG, "== onCreateLoader() Creating a new balance loader. ==");
                }

                BalanceLoader balance = new BalanceLoader(getActivity());
                return balance;
            }
            default: {

                // We must handle null loaders.
                return null;
            }
        }

    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link android.app.FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p/>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p/>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link android.database.Cursor}
     * and you place it in a {@link android.widget.CursorAdapter}, use
     * the {@link android.widget.CursorAdapter#CursorAdapter(android.content.Context,
     * android.database.Cursor, int)} constructor <em>without</em> passing
     * in either {@link android.widget.CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link android.widget.CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link android.database.Cursor} from a {@link android.content.CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link android.widget.CursorAdapter}, you should use the
     * {@link android.widget.CursorAdapter#swapCursor(android.database.Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (DEBUG) {
            Log.i(TAG, "== onLoadFinished() Load Balance complete. ==");
        }

        data.moveToFirst();
        mTotalBalance.setText(String.format("%.2f", data.getDouble(0)));
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (DEBUG) {
            Log.i(TAG, "== onLoaderReset() Reset Balance loader. ==");
        }

        mTotalBalance.setText(getResources().getString(R.string.loading));
    }

}
