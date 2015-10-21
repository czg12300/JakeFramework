package cn.common.ui.widgt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

/**
 * 描述:自定义圆形的ImageView,可设置边缘宽度和颜色。边缘宽度和颜色属性在attrs.xml有定义
 *
 * @author jaek
 * @since 2015-8-28 下午05:26:48
 */

public class RoundImageView extends ImageView {
  private float borderWidth = 1;

  public float getBorderWidth() {
    return borderWidth;
  }

  public void setBorderWidth(float borderWidth) {
    this.borderWidth = borderWidth;
  }

  public void setBorderWidth(float borderWidth, int unit) {
    this.borderWidth = TypedValue.applyDimension(unit, borderWidth, getResources().getDisplayMetrics());
    ;
  }

  public int getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(int borderColor) {
    this.borderColor = borderColor;
  }

  private int borderColor = 0xFFFFFFFF;

  public RoundImageView(Context context) {
    super(context);
  }

  public RoundImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }


  @Override
  protected void onDraw(Canvas canvas) {
    Drawable drawable = getDrawable();
    if (drawable == null) {
      return;
    }
    if (getWidth() == 0 || getHeight() == 0) {
      return;
    }
    this.measure(0, 0);
    if (drawable instanceof NinePatchDrawable) {
      return;
    }
    if (drawable instanceof BitmapDrawable) {
      Bitmap bCopy = ((BitmapDrawable) drawable).getBitmap();
      if (bCopy == null || bCopy.isRecycled()) {
        return;
      }
      int radius = (getWidth() < getHeight() ? getWidth() : getHeight()) / 2 - (int) borderWidth;
      try {
        bCopy = getCroppedBitmap(bCopy, radius);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setColor(borderColor);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius + borderWidth, paint);
        canvas.drawBitmap(bCopy, getWidth() / 2 - radius, getHeight() / 2 - radius, null);
      } catch (OutOfMemoryError error) {
        error.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 设置裁剪的Bitmap
   *
   * @param bmp    需要裁剪的Bitmap
   * @param radius 裁剪成圆形的半径
   * @return
   */
  private Bitmap getCroppedBitmap(Bitmap bmp, int radius) throws OutOfMemoryError {
    int diameter = radius * 2;
    if (bmp.getWidth() != diameter || bmp.getHeight() != diameter) {
      bmp = Bitmap.createScaledBitmap(bmp, diameter, diameter, false);
    }
    Bitmap output = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    final Paint paint = new Paint();
    final Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
    paint.setAntiAlias(true);
    paint.setFilterBitmap(true);
    paint.setDither(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(Color.parseColor("#ffffff"));
    canvas.drawCircle(bmp.getWidth() / 2, bmp.getHeight() / 2, bmp.getWidth() / 2, paint);
    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    canvas.drawBitmap(bmp, rect, rect, paint);
    if (bmp != null) {
      bmp.recycle();
      bmp = null;
    }
    return output;
  }
}
