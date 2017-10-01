package mprimavera.rxdownloader_lib;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;

public class ProgressDialog extends DialogFragment {
    private final String SPEED_SUFFIX = " Mb/s";
    private Context mContext;
    private TextView mMessage, mSpeed, mTotal;
    private ProgressBar mProgress;
    private View mView;
    private static AlertDialog dialog = null;

    public ProgressDialog(Context context) {
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.progress_dialog, null);
        mProgress = mView.findViewById(R.id.progress);
        mMessage = mView.findViewById(R.id.progressText);
        mSpeed = mView.findViewById(R.id.speedText);
        mTotal = mView.findViewById(R.id.totalText);
        this.setCancelable(false);
    }

    public ProgressDialog() {
        mView = getLayoutInflater().inflate(R.layout.progress_dialog, null);
        mProgress = mView.findViewById(R.id.progress);
        mMessage = mView.findViewById(R.id.progressText);
        mSpeed = mView.findViewById(R.id.speedText);
        mTotal = mView.findViewById(R.id.totalText);
        this.setCancelable(false);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setProgress(int progress) {
        mProgress.setProgress(progress);
        mMessage.setText(progress + " %");

        if(progress == 100) {
            Observable
                .empty()
                .delay(1, TimeUnit.SECONDS)
                .subscribe(o -> this.dismiss());
        }
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
        return createProgressDialog(getActivity());
    }

    public Dialog createProgressDialog(Context context) {
        if(dialog == null) {
            dialog = new AlertDialog.Builder(context)
                    .setView(mView)
                    .create();
        }
        return dialog;
    }
}

