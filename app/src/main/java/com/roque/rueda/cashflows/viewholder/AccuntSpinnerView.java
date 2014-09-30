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
import android.widget.ImageView;
import android.widget.TextView;

import com.roque.rueda.cashflows.R;
import com.roque.rueda.cashflows.model.Account;

/**
 * View holder class used to optimize scroll of the account in a spinner view.
 *
 * @author Roque Rueda
 * @since 11/08/2014
 * @version 1.0
 *
 */
public class AccuntSpinnerView {

    // Links to the data.
    private Resources mResources;
    private Account mModel;


    // Views
    private ImageView mIcon;
    private TextView mAccountName;

    /**
     * Creates a view used to bind this to the icons.
     *
     * @param parent View that contains the other views.
     * @param resources Resources used to present the information of the activity.
     */
    public AccuntSpinnerView(View parent, Resources resources){
        mIcon = (ImageView) parent.findViewById(R.id.icon);
        mAccountName = (TextView) parent.findViewById(R.id.account_name);
        mResources = resources;
    }

    /**
     * Bind the model to this view.
     * @param model Data structure that contains all the data to be presented in this
     *              view.
     */
    public void setModel(Account model) {
        mModel = model;
        bindModel();
    }

    /**
     * Present the data in the views.
     */
    private void bindModel() {
        AccountView.getImage(mModel.id, mResources, mIcon);
        mAccountName.setText(mModel.name);
    }

}
