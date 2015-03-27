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
import com.roque.rueda.cashflows.util.StringFormatter;

/**
 * View holder class used to optimize scroll of the account list view.
 * 
 * @author Roque Rueda
 * @since 28/05/2014
 * @version 1.0
 * 
 */
public class AccountView {

	private Resources mResources;
	private Account mModel;
    private View mParentView;
	private ImageView mIcon;
	private TextView mAccountName;
	private TextView mAccountBalance;
	//private ImageButton mActions;
	
	/**
	 * Creates the account view using the parent view to populate
	 * the children views in order to avoid call to findViewById.
	 * @param view				{@link View} that is inflate on the adapter.
	 * @param resources		{@link Resources} resources to access the application
	 * 									files.
	 */
	public AccountView(View view, Resources resources) {
        mParentView = view;
		mIcon = (ImageView) view.findViewById(R.id.icon);
		mAccountName = (TextView) view.findViewById(R.id.account_name);
		mAccountBalance = (TextView) view.findViewById(R.id.account_balance);
		mResources = resources;
	}
	
	/**
	 * Set the model of this view.
	 * @param model			Data object of the account.
	 */
	public void setModel(Account model) {
		mModel = model;
		bindModel();
	}
	
	/**
	 * Bind the model data with the widgets of this view.
	 */
	private void bindModel() {
		getImage(mModel.photoNumber, mResources, mIcon);
		mAccountName.setText(mModel.name.toUpperCase());

        if (mModel.endBalance > 0) {
            mParentView.setBackground(mResources.getDrawable(R.drawable.list_bg_blue));
        } else {
            mParentView.setBackground(mResources.getDrawable(R.drawable.list_bg_red));
        }

		mAccountBalance.setText(StringFormatter.formatCurrency(mModel.endBalance));
	}
	
	/**
	 * Get the image Icon base on the photo number store on the database.
	 * @param photoNumber			Photo number that is store in the database.
	 */
	public static void getImage(long photoNumber, Resources resources, ImageView img) {
		
		// Get the icon for the account base on the account number.
		
		int imageNumber = Long.valueOf(photoNumber).intValue();
		switch (imageNumber) {
		case 1:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_bank_cards_white));
			break;
		case 2:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_banknotes_white));
			break;
		case 3:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_cash_receiving_white));
			break;
		case 4:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_check_book_white));
			break;
		case 5:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_coins_white));
			break;
		case 6:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_money_bag_white));
			break;
		case 7:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_moneybox_white));
			break;
		case 8:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_purse_white));
			break;
		case 9:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_shopping_basket_white));
			break;
		case 10:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_wallet_white));
			break;
		default:
			img.setImageDrawable(resources.getDrawable(R.drawable.ic_bank_cards_white));
			break;
		}
	}
	

}
