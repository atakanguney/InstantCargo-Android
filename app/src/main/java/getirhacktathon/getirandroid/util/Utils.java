package getirhacktathon.getirandroid.util;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import okhttp3.Request;
import okio.Buffer;


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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String bodyToString(final Request request) {

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}
