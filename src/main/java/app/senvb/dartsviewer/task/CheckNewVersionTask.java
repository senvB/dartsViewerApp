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
package app.senvb.dartsviewer.task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

public class CheckNewVersionTask extends AsyncTask<Void, Void, Optional<String>> {

    private static final String TAG = "CheckNewVersion";

    @Override
    protected Optional<String> doInBackground(Void... v) {
       try {
           URL url = new URL("https://dartsviewer.senv.de/d/version.txt");
           url.openConnection().connect();
           try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))){
               return Optional.of(reader.readLine());
           }
        } catch (IOException e) {
           Log.e(TAG, "Cannot read latest version information from server!", e);
           return Optional.empty();
        }

    }

}



