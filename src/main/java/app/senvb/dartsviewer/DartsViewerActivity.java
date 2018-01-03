package app.senvb.dartsviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import app.senvb.dartsviewer.dataCache.DataCache;
import app.senvb.dartsviewer.fragment.CacheOptionsFragment;
import app.senvb.dartsviewer.fragment.InfoFragment;
import app.senvb.dartsviewer.fragment.LeagueFragment;
import app.senvb.dartsviewer.fragment.LeagueOverviewFragment;
import app.senvb.dartsviewer.fragment.LeagueOverviewFragment.OnLeagueSelectedListener;
import app.senvb.dartsviewer.fragment.RegionOverviewFragment.OnRegionSelectedListener;
import app.senvb.dartsviewer.fragment.SeasonOverviewFragment;
import app.senvb.dartsviewer.fragment.SeasonOverviewFragment.OnSeasonSelectedListener;
import app.senvb.dartsviewer.fragment.StartFragment;
import app.senvb.dartsviewer.task.CurrentTaskHolder;
import senvb.lib.dsabLoader.LeagueMetaData;
import senvb.lib.dsabLoader.Region;
import senvb.lib.dsabLoader.Season;


public class DartsViewerActivity extends AppCompatActivity implements OnSeasonSelectedListener, OnLeagueSelectedListener, OnRegionSelectedListener {

    private static final String TAG = "DartsViewer";

    private ProgressDialog progressDialog;

    private CurrentTaskHolder taskHolder;

    // CREATION OF ACTIVITY AND SETUP

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        UpdateAvailableChecker.checkUpdates(this);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_darts_viewer);
        DataCache.initialize(getFilesDir());

        if (savedInstanceState == null) {
            Fragment fragment = new StartFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        }
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                if (DartsViewerActivity.this.getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    DartsViewerActivity.this.closingOperations();
                }
            }
        );

        prepareProgressDialog();
        prepareTaskHolder();
    }

    private void prepareTaskHolder() {
        taskHolder = (CurrentTaskHolder) getLastCustomNonConfigurationInstance();
        if (taskHolder == null) {
            taskHolder = CurrentTaskHolder.getInstance();
        } else {
            taskHolder.attach(this);
            if (taskHolder.isTaskRunning()) {
                updateProgress(taskHolder.getProgressMessage());
            }
        }
    }

    private void prepareProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setOwnerActivity(this);
        progressDialog.setTitle("Bitte warten...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }

    // CURRENT TASK HANDLING

    private void closingOperations() {
        taskHolder.cancel();
        finish();
    }

    public Object onRetainCustomNonConfigurationInstance() {
        taskHolder.detach();
        return taskHolder;
    }

    // OPTIONS HANDLING

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.darts_viewer, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new CacheOptionsFragment()).addToBackStack(null).commit();
            return true;
        } else if (id == R.id.action_info) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new InfoFragment()).addToBackStack(null).commit();
            return true;
        } else if (id != R.id.action_feedback) {
            return super.onOptionsItemSelected(item);
        } else {
            try {
                startFeedbackEmail();
                return true;
            } catch (UnsupportedEncodingException e) {
                Log.w(TAG, "Cannot open mail dialog to send feedback.");
                return true;
            }
        }
    }

    private void startFeedbackEmail() throws UnsupportedEncodingException {
        Uri uri = Uri.parse("mailto:dartsViewerFeedback@senv.de?subject=" + URLEncoder.encode("Feedback DartsViewer", Charset.defaultCharset().name()) + "&body=" + URLEncoder.encode("[Feedback bitte hier eintragen]", Charset.defaultCharset().name()));
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(uri);
        startActivity(Intent.createChooser(sendIntent, "Sende Email"));
    }

    // CHECK FOR NETWORK

    public final boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } else {
            Log.e(TAG, "Cannot access connectivity manager, no check for network possible");
            return false;
        }
    }

    public final void showNetworkNotAvailableWarning() {
        new Builder(this).setTitle("Warnung").setMessage("Es besteht aktuell keine Internetverbindung. Es können nur gespeicherte Daten geladen werden.").setPositiveButton(AlertDialog.BUTTON_POSITIVE, null).show();
    }

    // FRAGMENT HANDLING

    public void onRegionSelected(Region region) {
        SeasonOverviewFragment seasonFragment = new SeasonOverviewFragment();
        Bundle b = new Bundle();
        b.putSerializable("region", region);
        seasonFragment.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, seasonFragment).addToBackStack(null).commit();
    }

    public void onSeasonSelected(Region r, Season season) {
        Fragment fragment = new LeagueOverviewFragment();
        Bundle b = new Bundle();
        b.putSerializable("season", season);
        b.putSerializable("region", r);
        fragment.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();
    }

    public void onLeagueSelected(LeagueMetaData leagueInfo, Season s, Region r) {
        Fragment fragment = new LeagueFragment();
        Bundle b = new Bundle();
        b.putSerializable("leagueInfo", leagueInfo);
        b.putSerializable("region", r);
        b.putSerializable("season", s);
        fragment.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();
    }

    // PROGRESS DIALOG HANDLING

    public void taskFinished() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void updateProgress(String progress) {
        if (progressDialog.getOwnerActivity() == this) {
            progressDialog.setMessage(progress);
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        }
    }

}
