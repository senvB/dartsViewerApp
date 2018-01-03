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

    public boolean setCurrentTask(DartsViewerAsyncTask<?, ?> task) {
        if (isTaskRunning()) {
            return false;
        }
        currentTask = task;
        return true;
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
