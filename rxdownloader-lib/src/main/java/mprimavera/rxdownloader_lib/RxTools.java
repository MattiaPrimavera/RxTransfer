package mprimavera.rxdownloader_lib;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxTools {
    public static ProgressDialogFragment mProgressDialogFragment;

    public static class ProgressDialogFragment extends DialogFragment {
        private RxDownload.IProgressListener mListener;

        public ProgressDialogFragment() {}

        public void setListener(RxDownload.IProgressListener listener) {
            mListener = listener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int messageResId = getArguments().getInt("message_res", 0);
            return createProgressDialog(getActivity(), messageResId);
        }
    }

    public static void dismissProgressDialog() {
        if (mProgressDialogFragment.getActivity() != null) {
            mProgressDialogFragment.dismissAllowingStateLoss();
        }
        mProgressDialogFragment = null;
    }

    public static void showProgressDialog(Activity activity, RxDownload.IProgressListener listener, int messageRes) {
        Bundle args = new Bundle();
        args.putInt("message_res", messageRes);
        mProgressDialogFragment = new ProgressDialogFragment();
        mProgressDialogFragment.setArguments(args);
        mProgressDialogFragment.show(((FragmentActivity)activity).getSupportFragmentManager(), "progressdialog");
        mProgressDialogFragment.setListener(listener);
    }

    public static Dialog createProgressDialog(Context context, @StringRes int messageResId) {
        View content = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
        ProgressBar prgress = content.findViewById(R.id.progress);
        TextView message = content.findViewById(R.id.message);

        message.setText(messageResId);
        return new AlertDialog.Builder(context)
                .setView(content)
                .create();
    }

    public static <T extends Observable<?>> ObservableTransformer<T, T> applySchedulers() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@io.reactivex.annotations.NonNull Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
