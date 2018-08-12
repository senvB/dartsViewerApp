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
package app.senvb.dartsviewer;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.util.Optional;

import app.senvb.dartsviewer.task.CheckNewVersionTask;

class UpdateAvailableChecker {

    private static final String TAG = "UpdateAvailableChecker";

    private static final String SERVER_VERSION_SEPARATOR = "##";

    static void checkUpdates(DartsViewerActivity dva) {
        try {
            CheckNewVersionTask task = new CheckNewVersionTask();
            Optional<String> versionInfoString = task.execute().get();
            if (versionInfoString.isPresent()) {
                String[]versionInfo = versionInfoString.get().split(SERVER_VERSION_SEPARATOR);
                int latestAvailableVersion = Integer.parseInt(versionInfo[0]);
                int thisVersionCode = BuildConfig.VERSION_CODE;
                if (latestAvailableVersion > thisVersionCode) {
                    String thisVersionName = "";
                    try {
                        thisVersionName = dva.getPackageManager()
                                .getPackageInfo(dva.getPackageName(), 0).versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    String msg = "Es steht ein neues Update der App zur Verfügung. Alte Version: " + thisVersionName + ", neue Version: " + versionInfo[1] + ". Herunterladen??";
                    final String version = versionInfo[2];
                    new AlertDialog.Builder(dva)
                            .setTitle("Update verfügbar")
                            .setMessage(msg)
                            .setPositiveButton("Herunterladen", (dialog, which) -> download(version, dva))
                            .setNegativeButton("Abbrechen", (dialog, which) -> {})
                            .show();
                }
            } else {
                Toast.makeText(dva, "Überprüfung auf Updates leider fehlgeschlagen.",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(dva, "Überprüfung auf Updates leider fehlgeschlagen.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    private static void download(String version, DartsViewerActivity dva) {
        String url = "https://dartsviewer.senv.de/d/" + version;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Lade neue Version " + version);
        request.setTitle("DartsViewer app");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, version);
        DownloadManager manager = (DownloadManager) dva.getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
        } else {
            Log.e(TAG, "Cannot start download of new versions, service not available!");
        }
    }
}
