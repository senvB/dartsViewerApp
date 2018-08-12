/**
 *  The DartsViewerApp allows to parse and display information for DSAB dart leagues.
 *  Copyright (C) 2017-2018  Sven Baselau
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package app.senvb.dartsviewer.fragment;

import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import app.senvb.dartsviewer.R;
import app.senvb.dartsviewer.dataCache.CacheStateListener;
import app.senvb.dartsviewer.dataCache.DataCache;
import app.senvb.dartsviewer.favorite.FavoriteHolder;

public class CacheOptionsFragment extends Fragment {

    private static final String TAG = "CacheOptionsFragment";

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private TextView cacheSizeView;

    private final List<CacheStateListener> listener = new ArrayList<>();

    public CacheOptionsFragment() {
        listener.add(FavoriteHolder.getInstance());
        listener.add(DataCache.getInstance());
    }

    private class BtnClickListener implements OnClickListener {

        public void onClick(View v) {
            if (v.getId() == R.id.btnClearCache) {
                int totalNrFiles = 0;
                int notDeleted = 0;
                for (CacheStateListener cel : listener) {
                    try {
                        int cacheClearResult = cel.clearCache();
                        if (cacheClearResult > 0) {
                            totalNrFiles += cacheClearResult;
                        } else {
                            notDeleted += -cacheClearResult;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot empty cache", e);
                    }
                }
                StringBuilder msg = new StringBuilder();
                if (notDeleted == 0) {
                    msg.append("Cache ist geleert. ").append(totalNrFiles).append(" Dateien gelöscht.");
                } else {
                    msg.append("Cache nicht komplett geleert. ").append(notDeleted).append(" Dateien konnten nicht gelöscht werden.");
                }
                Builder alert = new Builder(CacheOptionsFragment.this.getActivity());
                alert.setTitle("Cache löschen");
                alert.setMessage(msg.toString());
                alert.setPositiveButton("OK", null);
                alert.show();
                setCacheSize();
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_options, container, false);
        cacheSizeView = rootView.findViewById(R.id.cacheSize);
        setCacheSize();
        BtnClickListener clickListener = new BtnClickListener();
        rootView.findViewById(R.id.btnClearCache).setOnClickListener(clickListener);
        return rootView;
    }

    private void setCacheSize() {
        double cacheSize = 0;
        for (CacheStateListener cel : listener) {
           cacheSize += cel.calculateTotalCacheSizeInKB();
        }
        cacheSizeView.setText(formatTotalCacheSize(cacheSize));
    }

    @NonNull
    private String formatTotalCacheSize(double sizeInKB) {
        if (sizeInKB > 1024.0d) {
            return DF.format(sizeInKB / 1024.0d) + " mb";
        }
        return DF.format(sizeInKB) + " kb";
    }

}
