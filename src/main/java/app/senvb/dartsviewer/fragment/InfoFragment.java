package app.senvb.dartsviewer.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import app.senvb.dartsviewer.R;

public class InfoFragment extends Fragment {

    private class URLClickListener implements OnClickListener {
        public void onClick(View v) {
            Intent i = new Intent("android.intent.action.VIEW");
            i.setData(Uri.parse("http://dartsviewer.senv.de"));
            startActivity(i);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);
        rootView.findViewById(R.id.textWebsite).setOnClickListener(new URLClickListener());
        return rootView;
    }
}
