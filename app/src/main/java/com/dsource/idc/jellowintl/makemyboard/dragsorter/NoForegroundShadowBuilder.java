package com.dsource.idc.jellowintl.makemyboard.dragsorter;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.makeramen.dragsortadapter.DragSortShadowBuilder;

import java.lang.ref.WeakReference;

public class NoForegroundShadowBuilder extends DragSortShadowBuilder {

  private final WeakReference<View> viewRef;

  public NoForegroundShadowBuilder(View view, Point touchPoint) {
    super(view, touchPoint);
    this.viewRef = new WeakReference<>(view);
  }

  @Override public void onDrawShadow(@NonNull Canvas canvas) {
    final View view = viewRef.get();
    if (view != null) {
      Drawable foreground = null;

      // remove foreground before canvas draw
      if (view instanceof FrameLayout && view.getForeground() != null) {
        foreground = view.getForeground();
        view.setForeground(null);
      }

      view.draw(canvas);

      // reset foreground if it was removed
      if (foreground != null) {
        view.setForeground(foreground);
      }

    } else {
      Log.e(TAG, "Asked to draw drag shadow but no view");
    }
  }
}