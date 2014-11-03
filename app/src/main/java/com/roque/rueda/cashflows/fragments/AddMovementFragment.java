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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.roque.rueda.cashflows.MainActivity;
import com.roque.rueda.cashflows.R;
import com.roque.rueda.cashflows.adapters.AccountSpinnerAdapter;
import com.roque.rueda.cashflows.database.observer.DataBaseObserver;
import com.roque.rueda.cashflows.database.observer.DatabaseMessenger;
import com.roque.rueda.cashflows.hepers.DecimalDigitsInputFiler;
import com.roque.rueda.cashflows.loader.SpinnerAccountLoader;
import com.roque.rueda.cashflows.model.Movement;
import com.roque.rueda.cashflows.util.AddCashState;
import com.roque.rueda.cashflows.util.AddNegativeCash;
import com.roque.rueda.cashflows.util.AddPositiveCash;
import com.roque.rueda.cashflows.util.StringFormatter;

import java.util.Calendar;
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
		DatabaseMessenger, LoaderCallbacks<Cursor>, DatePickerDialog.OnDateSetListener {

	// Tag for this class.
	private static final String TAG = "AddMovementFragment";
	private static final boolean DEBUG = true;

    // Keys to bundle.
    private static final String AMOUNT_KEY = "amount";
    private static final String ACCOUNT_KEY = "account";
    private static final String DATE_KEY = "date";
    private static final String NOTES_KEY = "notes";

    // Loader id.
    private static final int LOADER_SPINNER = 3;

	// Used to store the observers.
	private LinkedHashSet<DataBaseObserver> mObservers;

    // Views
    private Spinner mAccountsSpinner;
    private AccountSpinnerAdapter mAdapter;
    private TextView mDateText;
    private Button mButtonAmount;
    private EditText mNotes;
    private EditText mAmountTextDago;
    private AlertDialog mInputMoneyDialog;
    private TextView mFragmentTitle;

    // Edit data.
    private double mCurrentAmount;
    private int mSelectedAccount;
    private long mCurrentDate;
    private String mCurrentNotes;
    private boolean hideDialog;
    private boolean mIsNegative;

    // State pattern.
    private AddCashState mCashState;


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
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_amount, container, false);

        // Recover the values to restore the edit values.
        if(savedInstanceState != null) {
            mCurrentAmount = savedInstanceState.getDouble(AMOUNT_KEY);
            mSelectedAccount = savedInstanceState.getInt(ACCOUNT_KEY);
            mCurrentDate = savedInstanceState.getLong(DATE_KEY);
            mCurrentNotes = savedInstanceState.getString(NOTES_KEY);
            hideDialog = true;
        }

        customizeAmountButton(rootView, mCurrentAmount);
        if (!hideDialog) {
            displayInputMoneyDialog(mCurrentAmount);
        }

        // Get adapter this will be filled by the loader.
        mAccountsSpinner = (Spinner) rootView.findViewById(R.id.accounts_spinner);
        mAccountsSpinner.postInvalidate();
        mAccountsSpinner.setAdapter(mAdapter);

        // Set the current date as a formattedString.
        final Date currentDate = new Date();
        mDateText = (TextView) rootView.findViewById(R.id.current_date);
        if (mCurrentDate != 0) {
            currentDate.setTime(mCurrentDate);
        }

        // Set the date on the current label.
        setCurrentDateText(currentDate);
        createDateTimeDialog();

        mNotes = (EditText) rootView.findViewById(R.id.notesText);
        if (mCurrentNotes != null) {
            mNotes.setText(mCurrentNotes);
        }

        createActionBar(inflater);
        mFragmentTitle = (TextView) rootView.findViewById(R.id.title_amount);
        createAddCashInstance(getArguments().getBoolean(MainActivity.SUBSTRACT_MOVEMENT));
        return rootView;
    }

    /**
     * Create a date time dialog to allow the user choose the date for the movement.
     *
     */
    private void createDateTimeDialog() {

        // Add a listener to display a dialog when the user taps on this view.
        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set the current date if it's one the saved instance object.
                final Calendar c = Calendar.getInstance();
                c.setTimeInMillis(mCurrentDate);

                // Dialog settings.
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                        AlertDialog.THEME_HOLO_DARK);
                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                builder.setTitle(R.string.time_date_dialog_title);
                // Custom view used to display date picker and time picker.
                View dateTimeLayout = layoutInflater.inflate(R.layout.date_time_layout, null);

                // Text used to display the selected date.
                final TextView selectedDate = (TextView) dateTimeLayout.findViewById(R.id.selected_date);
                final Date selectedDateByUser = new Date(getInputDate());

                selectedDate.setText(StringFormatter.formatDate(selectedDateByUser));
                final DatePicker datePicker = (DatePicker) dateTimeLayout.findViewById(R.id.date_picker);
                final TimePicker timePicker = (TimePicker) dateTimeLayout.findViewById(R.id.time_picker);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedDateByUser);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hourOfTheDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                timePicker.setCurrentHour(hourOfTheDay);
                timePicker.setCurrentMinute(minute);

                // Initialize the date picker, also add a listener to
                // update the label when user change the values.
                datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                        calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                        calendar.set(Calendar.SECOND, 0);
                        Date modifyDate = calendar.getTime();
                        // Update the text view.
                        selectedDate.setText(StringFormatter.formatDate(modifyDate));
                    }
                });

                // Listener used to update the values when time is selected.
                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, datePicker.getYear());
                        calendar.set(Calendar.MONTH, datePicker.getMonth());
                        calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);
                        Date modifyDate = calendar.getTime();
                        // Update the text view.
                        selectedDate.setText(StringFormatter.formatDate(modifyDate));
                    }
                });

                // Set the view to the dialog.
                builder.setView(dateTimeLayout);

                // Accept button behaviour.
                builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar dialogDate = Calendar.getInstance();
                        dialogDate.set(Calendar.YEAR, datePicker.getYear());
                        dialogDate.set(Calendar.MONTH, datePicker.getMonth());
                        dialogDate.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                        dialogDate.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                        dialogDate.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                        dialogDate.set(Calendar.SECOND, 0);

                        setCurrentDateText(dialogDate.getTime());
                        dialog.dismiss();
                    }
                });

                // Cancel button behaviour.
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

                // Create and display the dialog.
                Dialog dateDialog = builder.create();
                dateDialog.show();

            }
        });
    }

    /**
     * Show the date pass by parameter as a user friendly date
     * on the current date label of this view.
     * @param date Date that will be display to the user.
     */
    private void setCurrentDateText(Date date) {
        String formattedDate = StringFormatter.formatDate(date);
        mDateText.setText(formattedDate);
    }

    /**
     * Create the implementation to add a cash statement.
     * @param isSubtract Flag indicating whether this fragment is to add or subtract money.
     */
    private void createAddCashInstance(boolean isSubtract) {
        mIsNegative = isSubtract;
        if (isSubtract) {
            mFragmentTitle.setText(R.string.negative_movement);
            mCashState = new AddNegativeCash(getActivity());
        } else {
            mFragmentTitle.setText(R.string.positive_movement);
            mCashState = new AddPositiveCash(getActivity());
        }
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
                //Toast.makeText(getActivity(), R.string.accept, Toast.LENGTH_SHORT).show();

                Movement cashMovement = new Movement();
                cashMovement.setAmount(getInputAmount());
                cashMovement.setDate(new Date(getInputDate()));
                cashMovement.setDescription(getInputNotes());

                if (mIsNegative) {
                    cashMovement.setSing("-");
                } else {
                    cashMovement.setSing("+");
                }

                cashMovement.setIdAccount(mAccountsSpinner.getSelectedItemId());

                mCashState.saveCashMovement(cashMovement);
                Toast.makeText(getActivity(), R.string.movement_save_message, Toast.LENGTH_SHORT).
                        show();

                Intent intent = new Intent();
                // Indicate to parent activity that the information was store.
                intent.putExtra(MainActivity.ADD_MOVEMENT_RESULT, true);
                getActivity().setResult(MainActivity.REQUEST_CODE, intent);
                getActivity().finish();
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

    /**
     * Used to get the amount button and assign the initial values.
     * @param rootView View that contains the button.
     * @param mCurrentAmount
     */
    private void customizeAmountButton(View rootView, double mCurrentAmount) {
        // Set default amount.
        mButtonAmount = (Button) rootView.findViewById(R.id.amount_text);
        setDefaultAmount(mCurrentAmount);
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
     * @param mCurrentAmount amount that will be display in the button.
     */
    private void setDefaultAmount(double mCurrentAmount) {
        mButtonAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        final String currency = StringFormatter.formatCurrency(mCurrentAmount);
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
                String dialogText = mAmountTextDago.getText().toString();
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
        mAmountTextDago = new EditText(parentActivity);
        mAmountTextDago.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        mAmountTextDago.setGravity(Gravity.RIGHT);
        mAmountTextDago.setFilters(new InputFilter[]{new DecimalDigitsInputFiler(16, 2)});
        mAmountTextDago.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_CLASS_NUMBER);
        mAmountTextDago.setTextColor(getResources().getColor(R.color.text_white));

        mAmountTextDago.setTypeface(StringFormatter.createLightFont());

        // Assign this view to the dialog.
        builder.setView(mAmountTextDago);
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
        mAmountTextDago.setText(Double.valueOf(amount).toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.v(TAG, "In fragment save instance state");
        // Store amount.
        outState.putDouble(AMOUNT_KEY, getInputAmount());
        // Store selected account.
        outState.putInt(ACCOUNT_KEY, mAccountsSpinner.getSelectedItemPosition());
        // Store date.
        outState.putLong(DATE_KEY, getInputDate());
        // Store notes.
        outState.putString(NOTES_KEY, getInputNotes());

    }

    /**
     * Get the input notes by the user.
     * @return String instance with the value typed by the user.
     */
    private String getInputNotes() {
        return mNotes.getText().toString();
    }

    /**
     * Gets the date input by the user.
     * @return Date instance with the parsed value or current date
     * in case of error.
     */
    private long getInputDate() {
        return StringFormatter.parseFormatDate(
                mDateText.getText().toString()).getTime();
    }

    /**
     * Gets a decimal value from user input.
     * @return double value with the parse value.
     */
    private double getInputAmount() {
        return StringFormatter.getDecimalValue(
                mButtonAmount.getText().toString());
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
        if (mSelectedAccount != 0) {
            mAccountsSpinner.setSelection(mSelectedAccount);
        }

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

    /////////////////////////////////////////////////////////////////////
    // DatePickerDialog.OnDateSetListener Members.
    ///////////////////////////////////////////////////////////////////

    /**
     * @param view        The view associated with this listener.
     * @param year        The year that was set.
     * @param monthOfYear The month that was set (0-11) for compatibility
     *                    with {@link java.util.Calendar}.
     * @param dayOfMonth  The day of the month that was set.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

    }
}
