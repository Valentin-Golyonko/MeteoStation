package valentin8dev.by.MeteoStation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutMenu extends Fragment {

    private static final String TAG = "AboutMenu";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootAbout = inflater.inflate(R.layout.about_menu, container, false);

        TextView tv_hello = rootAbout.findViewById(R.id.tv_menu_hello);
        Linkify.addLinks(tv_hello, Linkify.WEB_URLS);

        return rootAbout;
    }
}
