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

import app.senvb.dartsviewer.DartsViewerActivity;

public abstract class DartsViewerAsyncTask<Params, Result> extends AsyncTask<Params, String, Result> {

    private DartsViewerActivity activity;

    private String currentProgressString;
    private final String identifier;

    DartsViewerAsyncTask(DartsViewerActivity act, String ident) {
        setActivity(act);
        currentProgressString = "";
        identifier = ident;
    }

    public String identifier() {
        return this.identifier;
    }

    void detach() {
        setActivity(null);
    }

    private void setActivity(DartsViewerActivity activity) {
        this.activity = activity;
    }

    private boolean hasActivity() {
        return this.activity != null;
    }

    void attach(DartsViewerActivity activity) {
        setActivity(activity);
    }

    String getProgressMessage() {
        return this.currentProgressString;
    }

    protected void onProgressUpdate(String... values) {
        if (hasActivity()) {
            currentProgressString = values[0];
            activity.updateProgress(this.currentProgressString);
            super.onProgressUpdate(values);
        }
    }

    void taskFinished() {
        if (hasActivity()) {
            activity.taskFinished();
        }
    }
}
