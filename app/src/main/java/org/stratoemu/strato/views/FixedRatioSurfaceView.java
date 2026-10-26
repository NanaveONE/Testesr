package org.stratoemu.strato.views;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
/** Actual SKYrus XML inflated; standalone preview intentionally never runs Vulkan/native gameplay. */
public final class FixedRatioSurfaceView extends SurfaceView {
    public FixedRatioSurfaceView(Context context) { super(context); }
    public FixedRatioSurfaceView(Context context,AttributeSet attrs) { super(context,attrs); }
    public FixedRatioSurfaceView(Context context,AttributeSet attrs,int style) { super(context,attrs,style); }
}
