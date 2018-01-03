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
