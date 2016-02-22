package cn.common.bitmap.core.imageaware;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 描述：用于需要异步显示图片的view
 * 作者：jake on 2016/1/3 18:22
 */
public class ViewImageAware extends ViewAware {
    public ViewImageAware(View view) {
        super(view);
    }

    @Override
    protected void setImageDrawableInto(Drawable drawable, View view) {
        view.setBackgroundDrawable(drawable);
    }

    @Override
    protected void setImageBitmapInto(Bitmap bitmap, View view) {
        view.setBackgroundDrawable(new BitmapDrawable(bitmap));
    }
}
