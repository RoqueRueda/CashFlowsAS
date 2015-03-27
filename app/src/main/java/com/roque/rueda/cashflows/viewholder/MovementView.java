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
package com.roque.rueda.cashflows.viewholder;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.roque.rueda.cashflows.R;
import com.roque.rueda.cashflows.model.Movement;
import com.roque.rueda.cashflows.util.StringFormatter;

/**
 * View holder class used to display a movement data into a list view.
 *
 * @author Roque Rueda
 * @since 28/05/2014
 * @version 1.0
 *
 */
public class MovementView {

    private Resources mResources;
    private Movement mModel;
    private TextView mAmount;
    private TextView mDescription;
    private TextView mDate;
    private View mParentView;

    /**
     * Creates the view with the resources.
     * @param view RootView that contains the widgets to display the user information.
     * @param resources Global resources of the application.
     */
    public MovementView(View view, Resources resources) {
        mParentView = view;
        mAmount = (TextView) view.findViewById(R.id.movement_amount);
        mDescription = (TextView) view.findViewById(R.id.movement_description);
        mDate = (TextView) view.findViewById(R.id.movement_date);
        mResources = resources;
    }

    /**
     *
     * Set the data for this view.
     * @param model Model that contains all the data to display.
     */
    public void setModel(Movement model) {
        mModel = model;
        bindModel();
    }

    /**
     * Ties the information with their respective widgets.
     */
    private void bindModel() {

        // Format our amount.
        StringBuilder amount = new StringBuilder(mModel.getSing());
        amount.append(StringFormatter.formatCurrency(mModel.getAmount()));
        if (mModel.getSing().equalsIgnoreCase("+")) {
            mParentView.setBackground(mResources.getDrawable(R.drawable.list_bg_blue));
        } else {
            mParentView.setBackground(mResources.getDrawable(R.drawable.list_bg_red));
        }
        mAmount.setText(amount.toString());
        mDescription.setText(mModel.getDescription());
        mDate.setText(StringFormatter.formatDate(mModel.getDate()));
    }


}
