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
import android.os.AsyncTask.Status;

import app.senvb.dartsviewer.DartsViewerActivity;

public class CurrentTaskHolder {

    private static CurrentTaskHolder holder;

    private DartsViewerAsyncTask<?, ?> currentTask;

    public static CurrentTaskHolder getInstance() {
        if (holder == null) {
            holder = new CurrentTaskHolder();
        }
        return holder;
    }

    public void setCurrentTask(DartsViewerAsyncTask<?, ?> task) {
        currentTask = task;
    }

    public boolean isTaskRunning() {
        return !(currentTask == null || currentTask.getStatus() == Status.FINISHED);
    }

    public AsyncTask<?, ?, ?> getCurrentTask() {
        return currentTask;
    }

    public final String getIdentifierCurrentTask() {
        if (isTaskRunning()) {
            return currentTask.identifier();
        }
        return null;
    }

    public final void detach() {
        if (currentTask != null) {
            currentTask.detach();
        }
    }

    public void attach(DartsViewerActivity dartsViewerActivity) {
        if (currentTask != null) {
            currentTask.attach(dartsViewerActivity);
        }
    }

    public String getProgressMessage() {
        if (currentTask != null) {
            return currentTask.getProgressMessage();
        }
        return null;
    }

    public void cancel() {
        if (isTaskRunning()) {
            currentTask.cancel(true);
        }
    }
}
