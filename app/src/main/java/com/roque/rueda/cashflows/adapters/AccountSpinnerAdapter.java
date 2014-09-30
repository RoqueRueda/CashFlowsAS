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
package com.roque.rueda.cashflows.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SpinnerAdapter;

import com.roque.rueda.cashflows.R;
import com.roque.rueda.cashflows.database.AccountTable;
import com.roque.rueda.cashflows.viewholder.AccountView;
import com.roque.rueda.cashflows.model.Account;
import com.roque.rueda.cashflows.viewholder.AccuntSpinnerView;

/**
 * Adapter class to show accounts on a Spinner view.
 *
 * @author Roque Rueda
 * @since 08/05/2014
 * @version 1.0
 *
 */
public class AccountSpinnerAdapter extends CursorAdapter {

    private final LayoutInflater mInflater;
    private static int sCoursorFlags = 0;

    /**
     * Creates an accocunt spinner adapter used to show data on a spinner.
     * @param context Interface to application's global information.
     * @param data Cursor that contains the information to present in the view.
     */
    public AccountSpinnerAdapter(Context context, Cursor data) {
        super(context, data, sCoursorFlags);

        mInflater = LayoutInflater.from(context);
    }

    /**
     * Bind an existing view to the data pointed to by cursor.
     * @param view 		Existing view, returned earlier by newView
     * @param ctx 		Interface to application's global information
     * @param cursor	The cursor from which to get the data.
     * 				    The cursor is already moved to the correct position.
     *
     */
    @Override
    public void bindView(View view, Context ctx, Cursor cursor) {
        Account account = new Account();
        account.id = cursor.getLong(cursor.
                getColumnIndex(AccountTable._ID));
        account.name = cursor.getString(cursor.
                getColumnIndex(AccountTable.ACCOUNT_NAME));
        account.photoNumber = cursor.getLong(cursor.
                getColumnIndex(AccountTable.PHOTO_NUMBER));

        AccuntSpinnerView holder = (AccuntSpinnerView) view.getTag();
        holder.setModel(account);
    }


    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context	Interface to application's global information.
     * @param cursor 	The cursor from which to get the data.
     * 					The cursor is already moved to the correct position.
     *  @param parent 	The parent to which the new view is attached to.
     *
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.account_spinner, parent, false);
        // View holder that is used to avoid calls to find view by Id.
        AccuntSpinnerView holder = new AccuntSpinnerView(view, context.getResources());
        if (view != null) {
            view.setTag(holder);
        }
        return view;
    }
}
