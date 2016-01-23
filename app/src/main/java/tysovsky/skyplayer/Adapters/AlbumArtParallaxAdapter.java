package tysovsky.skyplayer.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import tysovsky.skyplayer.Fragments.AlbumArtFragment;

/**
 * Created by tysovsky on 1/18/16.
 */
public class AlbumArtParallaxAdapter extends FragmentStatePagerAdapter {

    private ArrayList<AlbumArtFragment> mFragments;
    private ViewPager mPager;

    public AlbumArtParallaxAdapter(FragmentManager fm){
        super(fm);

        mFragments = new ArrayList<>();
    }



    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public void add(AlbumArtFragment fragment){
        fragment.setAdapter(this);
        mFragments.add(fragment);
        notifyDataSetChanged();
        mPager.setCurrentItem(getCount()-1, true);
    }

    public void remove(int i) {
        mFragments.remove(i);
        notifyDataSetChanged();
    }

    public void removeAll(){
        mFragments.clear();
        //notifyDataSetChanged();
    }

    public void moveLeft(AlbumArtFragment fragment){
        mFragments.set(0,  mFragments.get(1));
        mFragments.set(1, mFragments.get(2));
        mFragments.set(2, fragment);
    }

    public void moveRight(AlbumArtFragment fragment){
        mFragments.set(2,  mFragments.get(1));
        mFragments.set(1,  mFragments.get(0));
        mFragments.set(0,  fragment);
    }

    public void remove(AlbumArtFragment parallaxFragment) {
        mFragments.remove(parallaxFragment);

        int pos = mPager.getCurrentItem();
        notifyDataSetChanged();

        mPager.setAdapter(this);
        if (pos >= this.getCount()) {
            pos = this.getCount() - 1;
        }
        mPager.setCurrentItem(pos, true);

    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setPager(ViewPager pager) {
        mPager = pager;
    }
}
