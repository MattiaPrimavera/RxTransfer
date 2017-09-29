package mprimavera.rxdownloader_lib;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressDialog extends DialogFragment {
    private final String SPEED_SUFFIX = " Mb/s";
    private Context mContext;
    private TextView mMessage, mSpeed, mTotal;
    private ProgressBar mProgress;
    private View mView;

    public ProgressDialog(Context context) {
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.progress_dialog, null);
        mProgress = mView.findViewById(R.id.progress);
        mMessage = mView.findViewById(R.id.progressText);
        mSpeed = mView.findViewById(R.id.speedText);
        mTotal = mView.findViewById(R.id.totalText);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setProgress(int progress) {
        mProgress.setProgress(progress);
        mMessage.setText(progress + " %");
    }

    public void setSpeed(double speed) {
        String result = Double.toString(speed);
        if(result.length() > 4)
            mSpeed.setText(result.substring(0, 4) + SPEED_SUFFIX);
        else mSpeed.setText(result + SPEED_SUFFIX);
    }

    public void setTotal(long total, long length) {
        long totalMb = total / 1024 / 1024;
        long lengthMb = length / 1024 / 1024;

        mTotal.setText(totalMb + " Mb / " + lengthMb + " Mb");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int messageResId = getArguments().getInt("message_res", 0);
        return createProgressDialog(getActivity(), messageResId);
    }

    public Dialog createProgressDialog(Context context, @StringRes int messageResId) {
        mMessage.setText(messageResId);
        return new AlertDialog.Builder(context)
                .setView(mView)
                .create();
    }
}


