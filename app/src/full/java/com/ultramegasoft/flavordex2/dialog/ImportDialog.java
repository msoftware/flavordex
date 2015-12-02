package com.ultramegasoft.flavordex2.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ultramegasoft.flavordex2.R;

/**
 * Base for Dialogs for importing journal entries from external sources.
 *
 * @author Steve Guidetti
 */
public abstract class ImportDialog extends DialogFragment {
    /**
     * Views from the layout
     */
    private FrameLayout mListContainer;
    private ListView mListView;
    private ProgressBar mProgressBar;

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View root = LayoutInflater.from(getContext()).inflate(R.layout.list_dialog, null);

        mListContainer = (FrameLayout)root.findViewById(R.id.list_container);

        mListView = (ListView)root.findViewById(R.id.list);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                invalidateButtons();
            }
        });

        final TextView emptyView = (TextView)root.findViewById(R.id.empty);
        emptyView.setText(R.string.message_import_no_data);
        mListView.setEmptyView(emptyView);

        mProgressBar = (ProgressBar)root.findViewById(R.id.progress);

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_import)
                .setIcon(R.drawable.ic_import)
                .setView(root)
                .setPositiveButton(R.string.button_import, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        insertSelected();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        invalidateButtons();
    }

    /**
     * Insert the selected entries into the database.
     */
    protected abstract void insertSelected();

    /**
     * Update the status of the dialog buttons.
     */
    protected final void invalidateButtons() {
        final AlertDialog dialog = (AlertDialog)getDialog();
        if(dialog != null) {
            final boolean itemSelected = mListView.getCheckedItemCount() > 0;
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(itemSelected);
        }
    }

    /**
     * Set whether to show the list or the loading indicator.
     *
     * @param shown Whether to show the list
     */
    protected void setListShown(boolean shown) {
        if(shown) {
            mProgressBar.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            mListContainer.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Get the ListView from the dialog.
     *
     * @return The ListView
     */
    protected ListView getListView() {
        return mListView;
    }

    /**
     * Set the ListAdapter backing the list.
     *
     * @param adapter A ListAdapter
     */
    protected void setListAdapter(ListAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    /**
     * Get the ListAdapter backing the list.
     *
     * @return The ListAdapter
     */
    protected ListAdapter getListAdapter() {
        return mListView.getAdapter();
    }
}