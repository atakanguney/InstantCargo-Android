package getirhacktathon.getirandroid.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by atakan1 on 17.02.2018.
 */

public class Utils {
    /**
     * Show a Toast on the given context with the given message. Toast duration
     * is set to Toast.LENGTH_SHORT
     *
     * @param context      Context in which to show the Toast.
     * @param errorMessage Message to be displayed.
     */
    public static void showToast(Context context, String errorMessage) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
