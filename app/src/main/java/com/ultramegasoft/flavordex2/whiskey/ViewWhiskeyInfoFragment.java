/*
 * The MIT License (MIT)
 * Copyright © 2016 Steve Guidetti
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ultramegasoft.flavordex2.whiskey;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ultramegasoft.flavordex2.R;
import com.ultramegasoft.flavordex2.fragment.ViewInfoFragment;
import com.ultramegasoft.flavordex2.provider.Tables;
import com.ultramegasoft.flavordex2.widget.ExtraFieldHolder;

import java.util.LinkedHashMap;

/**
 * Whiskey specific entry view Fragment.
 *
 * @author Steve Guidetti
 */
public class ViewWhiskeyInfoFragment extends ViewInfoFragment {
    /**
     * Views to hold details specific to whiskey
     */
    private TextView mTxtType;

    private TextView mTxtAge;
    private TextView mTxtABV;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View root = super.onCreateView(inflater, container, savedInstanceState);

        mTxtType = root.findViewById(R.id.entry_type);

        mTxtAge = root.findViewById(R.id.entry_stats_age);
        mTxtABV = root.findViewById(R.id.entry_stats_abv);

        return root;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    protected int getLayoutId() {
        return R.layout.fragment_view_info_whiskey;
    }

    @Override
    protected void populateExtras(@NonNull LinkedHashMap<String, ExtraFieldHolder> data) {
        super.populateExtras(data);
        setViewText(mTxtType, getExtraValue(data.get(Tables.Extras.Whiskey.STYLE)));

        mTxtAge.setText(getExtraValue(data.get(Tables.Extras.Whiskey.STATS_AGE)));
        mTxtABV.setText(getExtraValue(data.get(Tables.Extras.Whiskey.STATS_ABV)));
    }
}
