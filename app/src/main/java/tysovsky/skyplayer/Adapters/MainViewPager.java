package tysovsky.skyplayer.Adapters;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.rey.material.widget.Slider;
import com.rey.material.widget.Switch;

/**
 * Created by tysovsky on 1/18/16.
 */
public class MainViewPager extends ViewPager {

    public MainViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainViewPager(Context context) {
        super(context);
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return super.canScroll(v, checkV, dx, x, y) || (checkV && customCanScroll(v));
    }

    protected boolean customCanScroll(View v) {
        if (v instanceof Slider || v instanceof Switch) {
            return true;
        }
        return false;
    }
}
