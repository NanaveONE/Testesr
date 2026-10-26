package org.stratoemu.strato.input.onscreen;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
/** Inflatable preview stand-in; original native emulator callbacks deliberately excluded. */
public final class OnScreenControllerView extends View {
    public OnScreenControllerView(Context c) { super(c); }
    public OnScreenControllerView(Context c, AttributeSet a) { super(c,a); }
    public OnScreenControllerView(Context c, AttributeSet a, int style) { super(c,a,style); }
}
