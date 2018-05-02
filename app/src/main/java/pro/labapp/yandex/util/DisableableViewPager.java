package pro.labapp.yandex.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.booking.rtlviewpager.RtlViewPager;

/**
 * Created by LabApp on 11.04.2018.
 */

public class DisableableViewPager extends RtlViewPager {

    private boolean enabled;

    public DisableableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled)
            return super.onTouchEvent(event);

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.enabled)
            return super.onInterceptTouchEvent(event);

        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
