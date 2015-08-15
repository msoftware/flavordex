package com.ultramegasoft.flavordex2;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Fragment to display the main details of a journal entry.
 *
 * @author Steve Guidetti
 */
public class EntryInfoFragment extends Fragment {
    /**
     * The database id for this entry
     */
    private long mEntryId;

    public EntryInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEntryId = getArguments().getLong(EntryDetailFragment.ARG_ITEM_ID);
    }
}
