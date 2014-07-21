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

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.roque.rueda.android.messenger.ListItemClickNotification;
import com.roque.rueda.cashflows.R;
import com.roque.rueda.cashflows.adapters.AccountAdapter;
import com.roque.rueda.cashflows.database.observer.DataBaseObserver;
import com.roque.rueda.cashflows.database.observer.DatabaseMessenger;
import com.roque.rueda.cashflows.loader.AccountLoader;

import java.util.LinkedHashSet;

/**
 * Fragment used to display the form to add a new cash movement.
 * 
 * @author Roque Rueda
 * @since 24/06/2014
 * @version 1.0
 * 
 */
public class AddMovementFragment extends Fragment implements
		DatabaseMessenger {

	// Tag for this class.
	private static final String TAG = "AddMovementFragment";
	private static final boolean DEBUG = true;

	// Used to store the observers.
	private LinkedHashSet<DataBaseObserver> mObservers;


	/**
	 * Called when the activity is created.
	 * @param savedInstanceState Bundle that contains all the
     *                           information for this activity.
	 *
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

    /**
     * Called to have the fragment instantiate its user interface view. This is optional,
     * and non-graphical fragments can return null (which is the default implementation). T
     * his will be called between onCreate(Bundle) and onActivityCreated(Bundle).
     *
     * If you return a View from here, you will later be called in onDestroyView
     * when the view is being released.
     *
     * @param inflater Inflater used to create the widgets.
     * @param container ViewGroup parent of this view.
     * @param savedInstanceState Bundle that contains all the information for this activity.
     * @return View that will be used to present the information.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_amount, container, false);
        return rootView;
    }

    /////////////////////////////////////////////////////////////////////
	// DataBase Messenger Interface members.
    ///////////////////////////////////////////////////////////////////

	/**
	 * Register a new observer to receive the calls when a database change.
	 * @param observer Instance observer that will be notify by this Messenger.
	 */
	@Override
	public void register(DataBaseObserver observer) {
		mObservers.add(observer);
	}

	/**
	 * Unregister a database observer in order to stop receive calls from
	 * changes on the database.
	 * @param observer Instance observer that will be unregister from this messenger.
	 */
	@Override
	public void unregister(DataBaseObserver observer) {
		mObservers.remove(observer);
	}

	/**
	 * Notify to the database observer that a table has change.
	 * @param tableName Name of the table that was changed.
	 */
	@Override
	public void sendNotification(String tableName) {
		for (DataBaseObserver observer : mObservers) {
			observer.notifyTableChange(tableName);
		}
	}

	/**
	 * Notify all the register observers that a database change has been made.
	 */
	@Override
	public void sendNotification() {
		for (DataBaseObserver observer : mObservers) {
			observer.notifyDatabaseChange();
		}
	}
	
}
