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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.drm.DrmStore;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.roque.rueda.cashflows.R;
import com.roque.rueda.cashflows.adapters.AccountSpinnerAdapter;
import com.roque.rueda.cashflows.database.observer.DataBaseObserver;
import com.roque.rueda.cashflows.database.observer.DatabaseMessenger;
import com.roque.rueda.cashflows.hepers.DecimalDigitsInputFiler;
import com.roque.rueda.cashflows.hepers.NumberTextWatcher;
import com.roque.rueda.cashflows.loader.SpinnerAccountLoader;
import com.roque.rueda.cashflows.util.StringFormatter;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.Date;
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
		DatabaseMessenger, LoaderCallbacks<Cursor> {

	// Tag for this class.
	private static final String TAG = "AddMovementFragment";
	private static final boolean DEBUG = true;

    private static final int LOADER_SPINNER = 3;

	// Used to store the observers.
	private LinkedHashSet<DataBaseObserver> mObservers;
    private Spinner mAccountsSpinner;
    private AccountSpinnerAdapter mAdapter;
    private TextView mDateText;
    private Button mButtonAmount;
    private EditText mAmmountTextDiaglo;
    private AlertDialog mInputMoneyDialog;


	/**
	 * Called when the activity is created.
	 * @param savedInstanceState Bundle that contains all the
     *                           information for this activity.
	 *
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_SPINNER, null, this);
        mAdapter = new AccountSpinnerAdapter(getActivity(), null);

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
        customizeAmountButton(rootView);
        displayInputMoneyDialog(0);

        // Get adapter this will be filled by the loader.
        mAccountsSpinner = (Spinner) rootView.findViewById(R.id.accounts_spinner);
        mAccountsSpinner.postInvalidate();
        mAccountsSpinner.setAdapter(mAdapter);

        // Set the current date as a formattedString.
        mDateText = (TextView) rootView.findViewById(R.id.current_date);
        Date currentDate = new Date();
        String formattedDate = StringFormatter.formatDate(currentDate);
        mDateText.setText(formattedDate);

        createActionBar(inflater);

        return rootView;
    }

    /**
     * Show a custom action bar in the activity.
     * @param inflater Inflater used to create the widgets.
     */
    private void createActionBar(LayoutInflater inflater) {
        final View customActionBarView = inflater.inflate(R.layout.actionbar_done_cancel, null);
        customActionBarView.findViewById(R.id.actionbar_done).
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Done.
                Toast.makeText(getActivity(), R.string.accept, Toast.LENGTH_SHORT).show();
            }
        });

        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cancel
                getActivity().finish();
            }
        });

        final ActionBar actionBar = getActivity().getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                    ActionBar.DISPLAY_SHOW_TITLE);

            actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Used to get the amount button and assign the initial values.
     * @param rootView View that contains the button.
     */
    private void customizeAmountButton(View rootView) {
        // Set default amount.
        mButtonAmount = (Button) rootView.findViewById(R.id.amount_text);
        setDefaultAmount();
        buildInputMoneyDialog(getActivity());

        mButtonAmount.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                String amountText = mButtonAmount.getText().toString();
                displayInputMoneyDialog(StringFormatter.getDecimalValue(amountText));
            }


        });
    }

    /**
     * Sets the default amount "0" in the amount button.
     */
    private void setDefaultAmount() {
        mButtonAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        Double defaultCurrency = 0d;
        final String currency = StringFormatter.formatCurrency(defaultCurrency);
        mButtonAmount.setText(currency);
    }

    /**
     * Creates the input amount dialog.
     * @param parentActivity Activity used to access application resources.
     */
    private void buildInputMoneyDialog(Activity parentActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity,
                AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(getString(R.string.amount_dialog_title));
        builder.setMessage(getString(R.string.amount_dialog_label));
        builder.setPositiveButton(getString(R.string.save_amount), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Set text of the dialog edit text to the amount button.
                double amountText = 0;
                String dialogText = mAmmountTextDiaglo.getText().toString();
                if(dialogText != null && !dialogText.isEmpty()) {
                    amountText = Double.valueOf(dialogText);
                }

                // Close this dialog.
                dialog.dismiss();
                mButtonAmount.setText(StringFormatter.formatCurrency(amountText));
            }
        });

        builder.setNegativeButton(getString(R.string.cancel_amount), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel this dialog.
                dialog.cancel();
            }
        });

        // Setup the input for the user.
        mAmmountTextDiaglo = new EditText(parentActivity);
        mAmmountTextDiaglo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        mAmmountTextDiaglo.setGravity(Gravity.RIGHT);
        mAmmountTextDiaglo.setFilters(new InputFilter[] {new DecimalDigitsInputFiler(16,2)});
        mAmmountTextDiaglo.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_CLASS_NUMBER);
        mAmmountTextDiaglo.setTextColor(getResources().getColor(R.color.text_white));

        mAmmountTextDiaglo.setTypeface(StringFormatter.createLightFont());

        // Assign this view to the dialog.
        builder.setView(mAmmountTextDiaglo);
        mInputMoneyDialog = builder.create();

        displayDialogKeyboard();

    }

    /**
     * Display the keyboard on the dialog.
     */
    private void displayDialogKeyboard() {
        mInputMoneyDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    /**
     * Display the input amount dialog to the user.
     * @param amount Amount that will be display to the user.
     */
    private void displayInputMoneyDialog(double amount) {
        // Display dialog.
        mInputMoneyDialog.show();

        // Set focus on the input text dialog.
        mAmmountTextDiaglo.setText(Double.valueOf(amount).toString());
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


    /////////////////////////////////////////////////////////////////////
    // LoaderCallbacks Interface members..
    ///////////////////////////////////////////////////////////////////

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
            case LOADER_SPINNER: {

                if (DEBUG) {
                    Log.i(TAG, "== onCreateLoader() Creating a new " +
                            "Spinner loader. ==");
                }

                SpinnerAccountLoader loader = new SpinnerAccountLoader(getActivity());
                return loader;
            }

            default: {

                // We should handle null loaders.
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

        mAdapter.swapCursor(data);
        mAdapter.notifyDataSetChanged();
        mAccountsSpinner.setAdapter(mAdapter);

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
            Log.i(TAG, "== onLoaderReset() Reset the loader. ==");
        }

        mAdapter.swapCursor(null);
    }
}
