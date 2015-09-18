package com.ultramegasoft.flavordex2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.ultramegasoft.flavordex2.util.PermissionUtils;


/**
 * The main application Activity. This shows a list of all the journal entries. On narrow screens,
 * selecting an entry launches a new Activity to show details. On wide screens, selecting an entry
 * shows details in a Fragment in this Activity.
 *
 * @author Steve Guidetti
 */
public class EntryListActivity extends AppCompatActivity {
    /**
     * Whether the Activity is in two-pane mode
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);

        if(findViewById(R.id.entry_detail_container) != null) {
            mTwoPane = true;

            ((EntryListFragment)getSupportFragmentManager().findFragmentById(R.id.entry_list))
                    .setTwoPane(true);
        }

        if(savedInstanceState == null) {
            PermissionUtils.checkExternalStoragePerm(this, R.string.message_request_storage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    /**
     * Called by the ListFragment when an item is selected.
     *
     * @param id      The entry ID
     * @param catName The name of the entry category
     * @param catId   The ID of the entry category
     */
    public void onItemSelected(long id, String catName, long catId) {
        if(mTwoPane) {
            final Bundle args = new Bundle();
            args.putLong(ViewEntryFragment.ARG_ENTRY_ID, id);
            args.putString(ViewEntryFragment.ARG_ENTRY_CAT, catName);
            args.putLong(ViewEntryFragment.ARG_ENTRY_CAT_ID, catId);

            final ViewEntryFragment fragment = new ViewEntryFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.entry_detail_container, fragment).commit();

        } else {
            final Intent intent = new Intent(this, ViewEntryActivity.class);
            intent.putExtra(ViewEntryFragment.ARG_ENTRY_ID, id);
            intent.putExtra(ViewEntryFragment.ARG_ENTRY_CAT, catName);
            intent.putExtra(ViewEntryFragment.ARG_ENTRY_CAT_ID, catId);
            startActivity(intent);
        }
    }
}
