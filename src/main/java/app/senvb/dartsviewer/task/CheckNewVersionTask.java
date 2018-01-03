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



