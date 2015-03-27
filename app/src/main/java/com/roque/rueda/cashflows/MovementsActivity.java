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
package com.roque.rueda.cashflows;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.roque.rueda.cashflows.fragments.MovementsListFragment;

/**
 * An activity that is used to present the movements of each account.
 * This is only used as shell to present the {@link MovementListFragment}.
 *
 * @author Roque Rueda
 * @version 1.0
 * @since 27/05/2014
 */
public class MovementsActivity extends FragmentActivity {

    private static final boolean DEBUG = true;
    private final static String TAG = "MovementsActivity";

    /**
     * Account id for the arguments to the add amount activity.
     */
    public final static String ACCOUNT_ID = "CurrentAccountId";

    /**
     * Account id.
     */
    private long mAccountId;

    /**
     * Method called when the activity is created for the first time,
     * initialize this activity.
     *
     * @param savedInstanceState Saved state of the application.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movements);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            // Get the account id.
            mAccountId = getIntent().getLongExtra(
                    MovementsListFragment.ARG_ACCOUNT_ID, 1); // 1 is the default value.
            boolean showBalance = getIntent().getBooleanExtra(
                    MovementsListFragment.ARG_TWO_PANE, false);
            String accountName = getIntent().getStringExtra(MovementsListFragment.ARG_ACCOUNT_NAME);

            Bundle arguments = new Bundle();
            arguments.putLong(MovementsListFragment.ARG_ACCOUNT_ID,
                    mAccountId);
            arguments.putBoolean(MovementsListFragment.ARG_TWO_PANE,
                    showBalance);

            arguments.putString(MovementsListFragment.ARG_ACCOUNT_NAME, accountName);

            MovementsListFragment fragment = new MovementsListFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movements_container, fragment).commit();
        }
    }

    /**
     * Handles when a item is selected on the action bar.
     *
     * @param item Menu item that was press.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:

                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;
            }
            case R.id.action_add:
            {

                // Calls to the add activity.
                Intent addIntent = new Intent(this, AddAmountActivity.class);
                addIntent.putExtra(MainActivity.SUBSTRACT_MOVEMENT, false);
                addIntent.putExtra(ACCOUNT_ID, mAccountId);
                startActivityForResult(addIntent, MainActivity.REQUEST_CODE);
                break;
            }
            case R.id.action_subtract:
            {
                Intent minusIntent = new Intent(this, AddAmountActivity.class);
                minusIntent.putExtra(MainActivity.SUBSTRACT_MOVEMENT, true);
                minusIntent.putExtra(ACCOUNT_ID, mAccountId);
                startActivityForResult(minusIntent, MainActivity.REQUEST_CODE);
                break;
            }
            default:
            {
                Toast.makeText(this, R.string.invalid_option, Toast.LENGTH_LONG).show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates the menu for this activity.
     *
     * @param menu {@link Menu} that will be used to present the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
