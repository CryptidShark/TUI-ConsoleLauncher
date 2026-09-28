package ohi.andre.consolelauncher.tuils.interfaces;

import android.view.View;

/**
 * Created by andre on 25/07/15.
 */
public interface Outputable {
    void onOutput(CharSequence output, int category);
    void onOutput(int color, CharSequence output);
    void onOutput(CharSequence output);
    void onOutput(View view);
    void dispose();
}
