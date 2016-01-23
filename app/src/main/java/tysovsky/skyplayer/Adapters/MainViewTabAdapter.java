package tysovsky.skyplayer.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;

import tysovsky.skyplayer.Activities.MainActivity;
import tysovsky.skyplayer.Helpers.Enums.Tab;
import tysovsky.skyplayer.Fragments.TabAlbumsFragment;
import tysovsky.skyplayer.Fragments.TabArtistsFragment;
import tysovsky.skyplayer.Fragments.TabSongsFragment;
import tysovsky.skyplayer.Fragments.TabWelcomeFragment;

/**
 * Created by tysovsky on 1/19/16.
 */
public class MainViewTabAdapter extends FragmentStatePagerAdapter {
    Fragment[] mFragments;
    Tab[] mTabs;

    MainActivity mainActivity;

    private static final Field sActiveField;

    static {
        Field f = null;
        try {
            Class<?> c = Class.forName("android.support.v4.app.FragmentManagerImpl");
            f = c.getDeclaredField("mActive");
            f.setAccessible(true);
        } catch (Exception e) {

        }

        sActiveField = f;
    }

    public MainViewTabAdapter(android.support.v4.app.FragmentManager fm, Tab[] tabs, MainActivity activity) {
        super(fm);
        mTabs = tabs;
        mFragments = new Fragment[mTabs.length];
        mainActivity = activity;


        //dirty way to get reference of cached fragment
        try{
            ArrayList<Fragment> mActive = (ArrayList<Fragment>)sActiveField.get(fm);

            if(mActive != null){
                for(Fragment fragment : mActive){
                    if(fragment instanceof TabSongsFragment)
                        setFragment(Tab.SONGS, fragment);
                    else if(fragment instanceof TabArtistsFragment)
                        setFragment(Tab.ARTISTS, fragment);
                    else if(fragment instanceof TabWelcomeFragment)
                        setFragment(Tab.WELCOME, fragment);
                    else if(fragment instanceof TabAlbumsFragment)
                        setFragment(Tab.ALBUMS, fragment);
                }
            }
        }
        catch(Exception e){}
    }

    private void setFragment(Tab tab, Fragment f){
        for(int i = 0; i < mTabs.length; i++)
            if(mTabs[i] == tab){
                mFragments[i] = f;
                break;
            }
    }

    @Override
    public Fragment getItem(int position) {
        if(mFragments[position] == null){
            switch (mTabs[position]) {
                case SONGS:
                    mFragments[position] = TabSongsFragment.newInstance(mainActivity);
                    break;
                case ARTISTS:
                    mFragments[position] = TabArtistsFragment.newInstance();
                    break;
                case ALBUMS:
                    mFragments[position] = TabAlbumsFragment.newInstance(mainActivity);
                    break;
                case WELCOME:
                    mFragments[position] = TabWelcomeFragment.newInstance();
                    break;
            }
        }

        return mFragments[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs[position].toString().toUpperCase();
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }
}
