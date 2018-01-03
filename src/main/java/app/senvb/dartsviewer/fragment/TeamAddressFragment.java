package app.senvb.dartsviewer.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

import app.senvb.dartsviewer.DartsViewerActivity;
import app.senvb.dartsviewer.R;
import app.senvb.dartsviewer.task.CurrentTaskHolder;
import app.senvb.dartsviewer.task.LocationFromAddressTask;


public class TeamAddressFragment extends Fragment implements
        LocationFromAddressTask.LocationFromAddressHandler {

    private static final int ZOOM = 15;

    private String address;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_address,
                container, false);
        TextView title = rootView.findViewById(R.id.teamName);
        String teamName = getArguments().getString("teamName");
        String teamVenue = getArguments().getString("teamVenue");
        address = getArguments().getString("teamAddress");
        title.setText(String.format(Locale.getDefault(), "%s [%s]", teamVenue, teamName));
        String[] addressParts = address.split(",");
        TextView addr1 = rootView.findViewById(R.id.address1);
        if (addressParts.length > 0) {
            addr1.setText(addressParts[0]);
        }
        TextView addr2 = rootView.findViewById(R.id.address2);
        if (addressParts.length > 1) {
            addr2.setText(addressParts[1]);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!((DartsViewerActivity) getActivity()).isNetworkAvailable()) {
            ((DartsViewerActivity) getActivity())
                    .showNetworkNotAvailableWarning();
        } else {
            if (address != null && !address.isEmpty()) {
                CurrentTaskHolder taskHolder = CurrentTaskHolder.getInstance();
                if (taskHolder.isTaskRunning()
                        && LocationFromAddressTask.IDENTIFIER.equals(taskHolder.getIdentifierCurrentTask())) {
                    LocationFromAddressTask runningTask = (LocationFromAddressTask) taskHolder
                            .getCurrentTask();
                    runningTask.setHandler(this);
                } else {
                    LocationFromAddressTask task = new LocationFromAddressTask(
                            (DartsViewerActivity) getActivity(), this);
                    CurrentTaskHolder.getInstance().setCurrentTask(task);
                    task.execute(address);
                }
            }
        }

    }

    @Override
    public void handleLocation(LatLng coord) {
        if (coord != null) {
            OnMapReadyCallback callback = new MapReadyCallback(coord);

            GoogleMapOptions gmo = (new GoogleMapOptions()).zoomControlsEnabled(true).rotateGesturesEnabled(false);
            SupportMapFragment mapFragment = SupportMapFragment.newInstance(gmo);
             getFragmentManager().beginTransaction().add(R.id.mapFragmentHoleForAddressMap, mapFragment).commit();

            mapFragment.getMapAsync(callback);
        } else {
            Toast.makeText(getActivity(), "Adresse nicht gefunden", Toast.LENGTH_LONG).show();
        }
    }

    private class MapReadyCallback implements OnMapReadyCallback {

        private final LatLng coord;

        MapReadyCallback(LatLng coord) {
            this.coord = coord;
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            googleMap.addMarker(new MarkerOptions().position(coord));
            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(coord, ZOOM);
            googleMap.moveCamera(cu);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }
}
