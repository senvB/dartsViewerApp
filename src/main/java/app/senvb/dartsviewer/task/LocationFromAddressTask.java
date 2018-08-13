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

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Optional;

import app.senvb.dartsviewer.DartsViewerActivity;

public class LocationFromAddressTask extends DartsViewerAsyncTask<String, Optional<LatLng>> {

	public interface LocationFromAddressHandler {
		void handleLocation(@Nullable LatLng coord);
	}
	
	public static final String IDENTIFIER = "LocationString";

	private LocationFromAddressHandler handler;


	public LocationFromAddressTask(DartsViewerActivity act, LocationFromAddressHandler handler) {
		super(act, IDENTIFIER);
		setHandler(handler);
	}

	public void setHandler(LocationFromAddressHandler handler) {
		this.handler = handler;		
	}

	/**
	 * The system calls this to perform work in a worker thread and delivers it
	 * the parameters given to AsyncTask.execute()
	 */
	protected Optional<LatLng> doInBackground(String... addresses) {
		publishProgress("Lade ... ");
        if (addresses != null && addresses.length != 0) {
            String address = addresses[0];
			return getLocationFromString(address);
        }
		return Optional.empty();
	}

	private Optional<LatLng> getLocationFromString(String address) {
		try {
			String urlString = "http://maps.google.com/maps/api/geocode/json?address="
					+ URLEncoder.encode(address, "UTF-8")
					+ "&ka&sensor=false";
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			String jsonAddress;
			try (InputStream stream = new BufferedInputStream(urlConnection.getInputStream())) {
				jsonAddress = IOUtils.toString(stream, Charset.defaultCharset());
			} finally {
				urlConnection.disconnect();
			}

			if (jsonAddress != null) {
				JSONObject jsonObject = new JSONObject(jsonAddress);

				double lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
						.getJSONObject("geometry").getJSONObject("location")
						.getDouble("lng");

				double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
						.getJSONObject("geometry").getJSONObject("location")
						.getDouble("lat");
				return Optional.of(new LatLng(lat, lng));
			}
			return Optional.empty();
		} catch (IOException | JSONException e) {
			Log.e(IDENTIFIER, "Cannot resolve a location for ", e);
			return Optional.empty();
		}

	}

//	private List<Address> getStringFromLocation(double lat, double lng)
//			throws IOException, JSONException {
//
//		String address = String
//				.format(Locale.ENGLISH,
//						"http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="
//								+ Locale.getDefault().getCountry(), lat, lng);
//
//
//        URL url = new URL(address);
//        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//        StringBuilder stringBuilder = new StringBuilder();
//        try {
//            InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
//            int b;
//            while ((b = stream.read()) != -1) {
//                stringBuilder.append((char) b);
//            }
//        }
//        finally {
//            urlConnection.disconnect();
//        }
//
//		JSONObject jsonObject = new JSONObject(stringBuilder.toString());
//
//		List<Address> retList = new ArrayList<Address>();
//
//		if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
//			JSONArray results = jsonObject.getJSONArray("results");
//			for (int i = 0; i < results.length(); i++) {
//				JSONObject result = results.getJSONObject(i);
//				String indiStr = result.getString("formatted_address");
//				Address addr = new Address(Locale.getDefault());
//				addr.setAddressLine(0, indiStr);
//				retList.add(addr);
//			}
//		}
//
//		return retList;
//	}

	/**
	 * The system calls this to perform work in the UI thread and delivers the
	 * result from doInBackground()
	 */
	protected void onPostExecute(Optional<LatLng> coord) {
		handler.handleLocation(coord.orElse(null));
		taskFinished();
	}

}
