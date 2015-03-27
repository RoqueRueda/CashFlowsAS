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

import com.roque.rueda.cashflows.R;
import com.roque.rueda.cashflows.database.MovementsTable;
import com.roque.rueda.cashflows.model.Movement;
import com.roque.rueda.cashflows.viewholder.MovementView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Adapter class to show movements on a list view.
 *
 * @author Roque Rueda
 * @since 29/12/2014
 * @version 1.0
 *
 */
public class MovementsAdapter extends CursorAdapter {

    private final LayoutInflater mInflater;
    private static int sCursorFlags = 0;

    /**
     *
     * Creates an movements adapter to display the items on a list view.
     *
     * @param context Interface to application's global information.
     * @param data Cursor that holds the information to show in the list view.
     */
    public MovementsAdapter (Context context, Cursor data) {
        super(context, data, sCursorFlags);

        mInflater = LayoutInflater.from(context);
    }

    /**
     * Bind an exiting view to the data pointed by the cursor.
     * @param view Existing view, returned early by newView.
     * @param context Interface to application's global information.
     * @param cursor The cursor from which get the data.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        Movement movement = new Movement();
        movement.setId(cursor.getLong(cursor.getColumnIndex(MovementsTable._ID)));
        movement.setAmount(cursor.getDouble(cursor.getColumnIndex(MovementsTable.MOVEMENTS_AMOUNT)));

        // Convert the string date into a date object.
        String rawMovementDate = cursor.getString(cursor.getColumnIndex(MovementsTable.MOVEMENTS_DATE));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            movement.setDate(dateFormat.parse(rawMovementDate));
        } catch (ParseException e) {
            movement.setDate(new Date());
        }

        movement.setDescription(cursor.getString(cursor.getColumnIndex(MovementsTable.MOVEMENTS_DESCRIPTION)));
        movement.setSing(cursor.getString(cursor.getColumnIndex(MovementsTable.MOVEMENTS_SING)));
        movement.setIdAccount(cursor.getLong(cursor.getColumnIndex(MovementsTable.ID_ACCOUNT)));

        MovementView viewHolder = (MovementView) view.getTag();
        viewHolder.setModel(movement);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.movement_list_item, parent, false);

        // View holder that is used to avoid calls to find view by id.
        MovementView holder = new MovementView(view, context.getResources());
        if (view != null) {
            view.setTag(holder);
        }
        return view;
    }
}
