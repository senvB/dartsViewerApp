package app.senvb.dartsviewer.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Spinner;

import app.senvb.dartsviewer.R;
import app.senvb.dartsviewer.favorite.Favorite;
import app.senvb.dartsviewer.favorite.FavoriteAdapater;
import app.senvb.dartsviewer.favorite.FavoriteHolder;


public class StartFragment extends Fragment {

    private Spinner favoriteSelector;
    private Spinner stateSelector;

    private class FavoriteSelectedListener implements OnClickListener {
        public void onClick(View v) {
            Favorite fav = (Favorite) favoriteSelector.getSelectedItem();
            if (fav == null) {
                return;
            }
            Bundle b;
            if (fav.getType() == Favorite.FavType.LEAGUE) {
                LeagueFragment fragment = new LeagueFragment();
                b = new Bundle();
                b.putSerializable("leagueInfo", fav.getLeagueData().getLeagueMetaData());
                fragment.setArguments(b);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();
            } else if (fav.getType() == Favorite.FavType.TEAM) {
                TeamFragment fragment2 = new TeamFragment();
                b = new Bundle();
                b.putSerializable("leagueData", fav.getLeagueData());
                b.putInt("teamID", fav.getTeamID());
                fragment2.setArguments(b);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment2).addToBackStack(null).commit();
            }
        }
    }

    private class StateSelectedListener implements OnClickListener {
        public void onClick(View v) {
            String stateName = (String) stateSelector.getSelectedItem();
            RegionOverviewFragment fragment = new RegionOverviewFragment();
            Bundle b = new Bundle();
            b.putSerializable("state", stateName);
            fragment.setArguments(b);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_start, container, false);
        stateSelector = rootView.findViewById(R.id.stateSelector);
        favoriteSelector = rootView.findViewById(R.id.favoriteSelector);
        favoriteSelector.setAdapter(new FavoriteAdapater(getActivity(), R.layout.favorite_item, FavoriteHolder.getInstance().getAllFavorites()));
        rootView.findViewById(R.id.btnStateSelection).setOnClickListener(new StateSelectedListener());
        rootView.findViewById(R.id.btnFavoriteSelection).setOnClickListener(new FavoriteSelectedListener());
        return rootView;
    }
}
