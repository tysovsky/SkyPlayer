package tysovsky.skyplayer.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rey.material.widget.TabIndicatorView;

import tysovsky.skyplayer.Activities.MainActivity;
import tysovsky.skyplayer.Adapters.MainViewPager;
import tysovsky.skyplayer.Adapters.MainViewTabAdapter;
import tysovsky.skyplayer.R;
import tysovsky.skyplayer.Helpers.Enums.Tab;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    public static String TAG = "MainFragment";
    private TabIndicatorView tiv;
    private MainViewPager vp;

    private MainActivity mainActivity;

    public MainViewTabAdapter mPagerAdapter;

    private Tab[] mItems = new Tab[]{Tab.WELCOME, Tab.ARTISTS, Tab.ALBUMS, Tab.SONGS};


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        mainActivity = (MainActivity)activity;
        super.onAttach(activity);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView called");
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        vp = (MainViewPager)v.findViewById(R.id.main_vp);
        tiv = (TabIndicatorView)v.findViewById(R.id.main_tiv);

        mPagerAdapter = new MainViewTabAdapter(mainActivity.getSupportFragmentManager(), mItems, mainActivity);
        vp.setAdapter(mPagerAdapter);
        tiv.setTabIndicatorFactory(new TabIndicatorView.ViewPagerIndicatorFactory(vp));
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "OnPause called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "OnStop called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "OnDestroyView called");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "OnDetach called");
    }


}
