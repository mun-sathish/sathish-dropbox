package in.sathish.dropbox.util;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Sathish Mun on 23/9/15.
 */
public class ProgressDialogUtil {

    private static ProgressDialog pDialog;

    // to show the dialog with the required string
    public static void showDialog(Context context, String message) {
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
        pDialog.setMessage(message);
        pDialog.show();
    }

    // to hide the dialog if it is opened
    public static void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }
}
