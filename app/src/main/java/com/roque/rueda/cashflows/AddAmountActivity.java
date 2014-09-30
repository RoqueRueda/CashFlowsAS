package com.roque.rueda.cashflows;

import com.roque.rueda.cashflows.R;
import com.roque.rueda.cashflows.database.observer.DataBaseObserver;
import com.roque.rueda.cashflows.database.observer.DatabaseMessenger;
import com.roque.rueda.cashflows.fragments.AddMovementFragment;
import com.roque.rueda.cashflows.loader.SpinnerAccountLoader;

import android.app.Activity;
import android.app.ActionBar;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import java.util.LinkedHashSet;


/**
 * Wrapper activity used to present the form to add a cash movement.
 */
public class AddAmountActivity extends FragmentActivity {

    /**
     * Called when the activity is first created.
     * @param savedInstanceState saved instance used to create the
     *                           activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_amount);

        if (savedInstanceState == null) {

            Fragment addFragment = new AddMovementFragment();
            addFragment.setHasOptionsMenu(true);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, addFragment)
                    .commit();
        }
    }

    /**
     * Method used to create the options on the action bar.
     * @param menu Menu instance that will be used to inflate the UI.
     * @return true if the creation is a success.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.add_amount, menu);
        return true;
    }

    /**
     * Called when an item is selected.
     * @param item Item that was selected.
     * @return true if the handle is complete for this item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
