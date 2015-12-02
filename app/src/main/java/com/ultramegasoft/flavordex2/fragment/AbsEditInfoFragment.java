package com.ultramegasoft.flavordex2.fragment;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.ultramegasoft.flavordex2.R;
import com.ultramegasoft.flavordex2.provider.Tables;
import com.ultramegasoft.flavordex2.widget.DateInputWidget;
import com.ultramegasoft.flavordex2.widget.EntryHolder;
import com.ultramegasoft.flavordex2.widget.ExtraFieldHolder;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Base class for the Fragment for editing details for a new or existing journal entry.
 *
 * @author Steve Guidetti
 */
public abstract class AbsEditInfoFragment extends LoadingProgressFragment
        implements LoaderManager.LoaderCallbacks {
    /**
     * Keys for the Fragment arguments
     */
    public static final String ARG_ENTRY_ID = "entry_id";

    /**
     * Loader IDs
     */
    private static final int LOADER_MAIN = 0;
    private static final int LOADER_MAKERS = 1;

    /**
     * Keys for the saved state
     */
    private static final String STATE_EXTRAS = "extras";

    /**
     * The Views for the form fields
     */
    private EditText mTxtTitle;
    private AutoCompleteTextView mTxtMaker;
    private EditText mTxtOrigin;
    private EditText mTxtPrice;
    protected EditText mTxtLocation;
    private DateInputWidget mDateInputWidget;
    private RatingBar mRatingBar;
    private EditText mTxtNotes;

    /**
     * The category ID for the entry being added
     */
    private long mCatId;

    /**
     * The entry ID to edit
     */
    private long mEntryId;

    /**
     * True while data is loading
     */
    private boolean mIsLoading;

    /**
     * Map of extra field names to their data
     */
    private LinkedHashMap<String, ExtraFieldHolder> mExtras;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final Bundle args = getArguments();
        mCatId = args.getLong(AddEntryFragment.ARG_CAT_ID);
        mEntryId = args.getLong(ARG_ENTRY_ID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState == null) {
            getLoaderManager().initLoader(LOADER_MAIN, null, this).forceLoad();
        } else {
            //noinspection unchecked
            mExtras = (LinkedHashMap<String, ExtraFieldHolder>)savedInstanceState
                    .getSerializable(STATE_EXTRAS);
            populateExtras(mExtras);
            hideLoadingIndicator(false);
        }
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = super.onCreateView(inflater, container, savedInstanceState);

        mTxtTitle = (EditText)root.findViewById(R.id.entry_title);
        mTxtMaker = (AutoCompleteTextView)root.findViewById(R.id.entry_maker);
        mTxtOrigin = (EditText)root.findViewById(R.id.entry_origin);
        mTxtPrice = (EditText)root.findViewById(R.id.entry_price);
        mTxtLocation = (EditText)root.findViewById(R.id.entry_location);
        mDateInputWidget = (DateInputWidget)root.findViewById(R.id.entry_date);
        mRatingBar = (RatingBar)root.findViewById(R.id.entry_rating);
        mTxtNotes = (EditText)root.findViewById(R.id.entry_notes);

        final Date date = new Date();
        mDateInputWidget.setDate(date);
        mDateInputWidget.setMaxDate(date);

        setupMakersAutoComplete();

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_EXTRAS, mExtras);
    }

    /**
     * Set up the autocomplete for the maker field.
     */
    private void setupMakersAutoComplete() {
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(),
                R.layout.simple_dropdown_item_2line, null,
                new String[] {Tables.Makers.NAME, Tables.Makers.LOCATION},
                new int[] {android.R.id.text1, android.R.id.text2}, 0);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                final Uri uri;
                if(TextUtils.isEmpty(constraint)) {
                    uri = Tables.Makers.CONTENT_URI;
                } else {
                    uri = Uri.withAppendedPath(Tables.Makers.CONTENT_FILTER_URI_BASE,
                            constraint.toString());
                }

                final Bundle args = new Bundle();
                args.putParcelable("uri", uri);

                getLoaderManager().restartLoader(LOADER_MAKERS, args, AbsEditInfoFragment.this);

                return adapter.getCursor();
            }
        });

        mTxtMaker.setAdapter(adapter);

        // fill in maker and origin fields with a suggestion
        mTxtMaker.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Cursor cursor = (Cursor)parent.getItemAtPosition(position);
                cursor.moveToPosition(position);

                final String name = cursor.getString(cursor.getColumnIndex(Tables.Makers.NAME));
                final String origin =
                        cursor.getString(cursor.getColumnIndex(Tables.Makers.LOCATION));
                mTxtMaker.setText(name);
                mTxtOrigin.setText(origin);

                // skip origin field
                mTxtOrigin.focusSearch(View.FOCUS_DOWN).requestFocus();
            }
        });
    }

    /**
     * Load the values for the main entry fields.
     *
     * @param entry The entry
     */
    private void populateFields(EntryHolder entry) {
        if(entry != null) {
            mTxtTitle.setText(entry.title);
            mTxtMaker.setText(entry.maker);
            mTxtOrigin.setText(entry.origin);
            mTxtPrice.setText(entry.price);
            mTxtLocation.setText(entry.location);
            mDateInputWidget.setDate(new Date(entry.date));
            mRatingBar.setRating(entry.rating);
            mTxtNotes.setText(entry.notes);
        }
    }

    /**
     * Create and set up the extra field views.
     *
     * @param extras A map of extra names to the extra field
     */
    protected void populateExtras(LinkedHashMap<String, ExtraFieldHolder> extras) {
    }

    /**
     * Set up an EditText with an extra field.
     *
     * @param editText The EditText
     * @param extra    The extra field to associate with the View
     */
    protected static void initEditText(EditText editText, final ExtraFieldHolder extra) {
        if(extra == null) {
            return;
        }
        if(!extra.preset) {
            editText.setHint(extra.name);
        }
        editText.setText(extra.value);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                extra.value = s.toString();
            }
        });
    }

    /**
     * Set up a Spinner with an extra field.
     *
     * @param spinner The Spinner
     * @param extra   The extra field to associate with the View
     */
    protected static void initSpinner(Spinner spinner, final ExtraFieldHolder extra) {
        if(extra == null) {
            return;
        }
        if(extra.value != null) {
            spinner.setSelection(Integer.valueOf(extra.value));
        }

        final AdapterView.OnItemSelectedListener listener = spinner.getOnItemSelectedListener();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                extra.value = position + "";
                if(listener != null) {
                    listener.onItemSelected(parent, view, position, id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if(listener != null) {
                    listener.onNothingSelected(parent);
                }
            }
        });
    }

    /**
     * Test if the required fields are properly filled out.
     *
     * @return Whether the form is valid
     */
    public boolean isValid() {
        if(TextUtils.isEmpty(mTxtTitle.getText().toString())) {
            mTxtTitle.setError(getString(R.string.error_required));
            mTxtTitle.requestFocus();
            return false;
        }
        return !mIsLoading;
    }

    /**
     * Is this fragment currently loading data?
     *
     * @return True while the fragment is loading data.
     */
    public boolean isLoading() {
        return mIsLoading;
    }

    /**
     * Get the data for this entry, including the main info fields and extra fields.
     *
     * @param entry An EntryHolder
     */
    public final void getData(EntryHolder entry) {
        entry.id = mEntryId;

        if(entry.id == 0) {
            entry.catId = mCatId;
        }

        entry.title = mTxtTitle.getText().toString();
        entry.maker = mTxtMaker.getText().toString();
        entry.origin = mTxtOrigin.getText().toString();
        entry.price = mTxtPrice.getText().toString();
        entry.location = mTxtLocation.getText().toString();
        entry.date = mDateInputWidget.getDate().getTime();
        entry.rating = mRatingBar.getRating();
        entry.notes = mTxtNotes.getText().toString();

        entry.getExtras().addAll(mExtras.values());
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch(id) {
            case LOADER_MAIN:
                mIsLoading = true;
                ActivityCompat.invalidateOptionsMenu(getActivity());
                return new DataLoader(getContext(), mCatId, mEntryId);
            case LOADER_MAKERS:
                final String order = Tables.Makers.NAME + " ASC";
                final Uri uri = args.getParcelable("uri");
                return new CursorLoader(getContext(), uri, null, null, null, order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch(loader.getId()) {
            case LOADER_MAIN:
                final DataLoader.Holder holder = (DataLoader.Holder)data;

                populateFields(holder.entry);
                mExtras = holder.extras;
                populateExtras(holder.extras);

                hideLoadingIndicator(true);
                mTxtTitle.setSelection(mTxtTitle.getText().length());

                mIsLoading = false;
                ActivityCompat.invalidateOptionsMenu(getActivity());
                break;
            case LOADER_MAKERS:
                ((CursorAdapter)mTxtMaker.getAdapter()).swapCursor((Cursor)data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        switch(loader.getId()) {
            case LOADER_MAKERS:
                ((CursorAdapter)mTxtMaker.getAdapter()).swapCursor(null);
                break;
        }
    }

    /**
     * Custom Loader to load everything in one task
     */
    public static class DataLoader extends AsyncTaskLoader<DataLoader.Holder> {
        /**
         * The ContentResolver to use
         */
        private final ContentResolver mResolver;

        /**
         * The category ID
         */
        private long mCatId;

        /**
         * The entry ID, if editing
         */
        private final long mEntryId;

        /**
         * @param context The Context
         * @param catId   The category ID
         * @param entryId The entry ID, if editing
         */
        public DataLoader(Context context, long catId, long entryId) {
            super(context);
            mResolver = context.getContentResolver();
            mCatId = catId;
            mEntryId = entryId;
        }

        @Override
        public Holder loadInBackground() {
            final Holder holder = new Holder();
            if(mEntryId > 0) {
                final Uri uri =
                        ContentUris.withAppendedId(Tables.Entries.CONTENT_ID_URI_BASE, mEntryId);
                holder.entry = loadEntry(uri);
                loadExtras(holder);
                loadExtrasValues(holder, uri);
            } else {
                loadExtras(holder);
            }
            return holder;
        }

        /**
         * Load the entry from the database.
         *
         * @param entryUri The Uri for the entry
         * @return The entry
         */
        private EntryHolder loadEntry(Uri entryUri) {
            final EntryHolder entry = new EntryHolder();
            final Cursor cursor = getContext().getContentResolver().query(entryUri, null, null, null, null);
            if(cursor != null) {
                try {
                    if(cursor.moveToFirst()) {
                        entry.title = cursor.getString(cursor.getColumnIndex(Tables.Entries.TITLE));
                        entry.maker = cursor.getString(cursor.getColumnIndex(Tables.Entries.MAKER));
                        entry.origin =
                                cursor.getString(cursor.getColumnIndex(Tables.Entries.ORIGIN));
                        entry.price = cursor.getString(cursor.getColumnIndex(Tables.Entries.PRICE));
                        entry.location =
                                cursor.getString(cursor.getColumnIndex(Tables.Entries.LOCATION));
                        entry.date = cursor.getLong(cursor.getColumnIndex(Tables.Entries.DATE));
                        entry.rating =
                                cursor.getFloat(cursor.getColumnIndex(Tables.Entries.RATING));
                        entry.notes = cursor.getString(cursor.getColumnIndex(Tables.Entries.NOTES));

                        mCatId = cursor.getLong(cursor.getColumnIndex(Tables.Entries.CAT_ID));
                    }
                } finally {
                    cursor.close();
                }
            }
            return entry;
        }

        /**
         * Load the extra fields from the database.
         *
         * @param holder The Holder
         */
        private void loadExtras(Holder holder) {
            final Uri uri = ContentUris.withAppendedId(Tables.Cats.CONTENT_ID_URI_BASE, mCatId);
            final Cursor cursor = mResolver.query(Uri.withAppendedPath(uri, "extras"), null, null,
                    null, Tables.Extras.POS + " ASC");
            if(cursor != null) {
                long id;
                String name;
                boolean preset;
                try {
                    while(cursor.moveToNext()) {
                        id = cursor.getLong(cursor.getColumnIndex(Tables.Extras._ID));
                        name = cursor.getString(cursor.getColumnIndex(Tables.Extras.NAME));
                        preset = cursor.getInt(cursor.getColumnIndex(Tables.Extras.PRESET)) == 1;
                        holder.extras.put(name, new ExtraFieldHolder(id, name, preset));
                    }
                } finally {
                    cursor.close();
                }
            }
        }

        /**
         * Load the extra field values from the database.
         *
         * @param holder   The Holder
         * @param entryUri The Uri for the entry
         */
        private void loadExtrasValues(Holder holder, Uri entryUri) {
            final Cursor cursor = mResolver.query(Uri.withAppendedPath(entryUri, "extras"), null,
                    null, null, null);
            if(cursor != null) {
                String name;
                String value;
                try {
                    while(cursor.moveToNext()) {
                        name = cursor.getString(cursor.getColumnIndex(Tables.Extras.NAME));
                        value = cursor.getString(cursor.getColumnIndex(Tables.EntriesExtras.VALUE));
                        holder.extras.get(name).value = value;
                    }
                } finally {
                    cursor.close();
                }
            }
        }

        /**
         * The holder for return data
         */
        public static class Holder {
            /**
             * The entry
             */
            public EntryHolder entry;

            /**
             * Map of extra field names to their data
             */
            public final LinkedHashMap<String, ExtraFieldHolder> extras = new LinkedHashMap<>();
        }
    }
}