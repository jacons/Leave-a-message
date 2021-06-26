package project.leaveamessage.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {

    public static final String ERRORTAG = "ERRORE CHIAMATA REST";

    public static boolean isConnected(Context context) {
        boolean connected;
        try {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return !connected;
        } catch (Exception e) {
           return true;
        }
    }

}
