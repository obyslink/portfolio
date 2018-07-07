package tech.dappworld.webpagetoappconverter.drawer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import tech.dappworld.webpagetoappconverter.Config;
import tech.dappworld.webpagetoappconverter.R;

import java.util.ArrayList;

public class DrawerFragment extends Fragment {

    private static String TAG = DrawerFragment.class.getSimpleName();

    private ListView listView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private DrawerAdapter adapter;
    private View containerView;
    private DrawerFragmentListener drawerListener;

    public DrawerFragment() {

    }

    public void setDrawerListener(DrawerFragmentListener listener) {
        this.drawerListener = listener;
    }

    public ArrayList<String> getData() {
        ArrayList<String> data = new ArrayList<>();

        // preparing navigation drawer items
        for (int i = 0; i < Config.TITLES.length; i++) {
            //If there is a localized title available, use it
            Object title = Config.TITLES[i];
            if (title instanceof Integer && !title.equals(0)){
                data.add(getResources().getString((int) title));
            } else {
                data.add((String) title);
            }
        }
        return data;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating view layout
        View layout = inflater.inflate(R.layout.drawer, container, false);
        listView = (ListView) layout.findViewById(R.id.drawerList);

        RelativeLayout drawerHeader = (RelativeLayout) layout.findViewById(R.id.nav_header_container);
        ImageView drawerIcon = (ImageView) layout.findViewById(R.id.drawer_icon);

        //Hide the drawer if configured, or else, if drawer is visible, set drawer icon
        if (Config.HIDE_DRAWER_HEADER) {
            drawerHeader.setVisibility(View.GONE);
        } else {
            drawerIcon.setImageResource(Config.DRAWER_ICON);
        }

        final DrawerAdapter adapter = new DrawerAdapter(this.getActivity(), getData());
        // Attach the adapter to a ListView
        listView.setAdapter(adapter);
        adapter.setSelectedPosition(0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                boolean selectNew = drawerListener.onDrawerItemSelected(view, i);
                mDrawerLayout.closeDrawer(containerView);

                if (selectNew)
                    adapter.setSelectedPosition(i);
            }

        });

        return layout;
    }


    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

    }

    public interface DrawerFragmentListener {
        /**
         * @return true if the item should be selected in the menu, false if the selection shouldn't be updated
         */
        boolean onDrawerItemSelected(View view, int position);
    }
}
