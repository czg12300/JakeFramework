package cn.common.ui.widgt.pull;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


public class PullEnableTextView extends TextView implements PullEnable {

    public PullEnableTextView(Context context) {
        super(context);
    }

    public PullEnableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullEnableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean canPullDown() {
        return true;
    }

    @Override
    public boolean canPullUp() {
        return true;
    }

}
