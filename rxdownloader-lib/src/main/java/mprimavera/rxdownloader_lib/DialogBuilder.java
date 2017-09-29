package mprimavera.rxdownloader_lib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.view.View;

public class DialogBuilder {
    private final int NO_BUTTON_RES_SPECIFIED = 0;
    private String TAKE_PHOTO, CHOOSE_FROM_LIBRARY, CANCEL;
    private Context mContext;
    private View mMainAlertView;
    private String mTitle;
    private AlertDialog.Builder mBuilder;
    private int mWidth, mHeight;

    public DialogBuilder(Context context){
        mContext = context;
        mBuilder = new AlertDialog.Builder(mContext);
    }

    public DialogBuilder(Context context, int style) {
        mContext = context;
        mBuilder = new AlertDialog.Builder(mContext, style);
        mHeight = 0; mWidth = 0;
    }

    public static DialogBuilder with(Context context) {
        return new DialogBuilder(context);
    }

    public static DialogBuilder with(Context context, int style) {
        return new DialogBuilder(context, style);
    }

    public static void showMessage(String message, String action, int color, View.OnClickListener listener, View view){
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(action, listener)
                .setActionTextColor(color)
                .show();
    }

    public static void showMessageTextOnly(String message, String action, int color, View view){
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };

        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(action, clickListener)
                .setActionTextColor(color)
                .show();
    }

    public static void showPositiveDialog(Context context, String title, String message, String buttonLabel, DialogInterface.OnClickListener listener){
        AlertDialog.Builder editAlert = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonLabel, listener);
        AlertDialog alert = editAlert.create();
        alert.show();
    }

    public DialogBuilder mainView(View view) {
        mBuilder.setView(view);
        return this;
    }

    public DialogBuilder mainView(int res) {
        mBuilder.setView(res);
        return this;
    }

    public DialogBuilder size(int width, int height) {
        mWidth = width;
        mHeight = height;
        return this;
    }

    public DialogBuilder negativeButton(int labelRes, final DialogInterface.OnClickListener listener) {
        mBuilder.setNegativeButton(labelRes, listener);
        return this;
    }

    public DialogBuilder negativeButton(String label, final DialogInterface.OnClickListener listener) {
        mBuilder.setNegativeButton(label, listener);
        return this;
    }

    public DialogBuilder positiveButton(String label, final DialogInterface.OnClickListener listener) {
        mBuilder.setPositiveButton(label, listener);
        return this;
    }

    public DialogBuilder positiveButton(int labelRes, final DialogInterface.OnClickListener listener) {
        mBuilder.setPositiveButton(labelRes, listener);
        return this;
    }

    public DialogBuilder onDismiss(AlertDialog.OnDismissListener listener) {
        mBuilder.setOnDismissListener(listener);
        return this;
    }

    public DialogBuilder message(String message) {
        mBuilder.setMessage(message);
        return this;
    }

    public DialogBuilder message(int messageRes) {
        mBuilder.setMessage(messageRes);
        return this;
    }

    public DialogBuilder cancelable(boolean cancelable) {
        mBuilder.setCancelable(cancelable);
        return this;
    }

    public DialogBuilder title(String title) {
        mBuilder.setTitle(title);
        return this;
    }

    public DialogBuilder title(int title) {
        mBuilder.setTitle(title);
        return this;
    }

    public DialogBuilder show(int width, int height) {
        AlertDialog dialog = mBuilder.create();
        dialog.show();
        dialog.getWindow().setLayout(width, height);
        return this;
    }

    public DialogBuilder show() {
        mBuilder.show();
        return this;
    }
}
