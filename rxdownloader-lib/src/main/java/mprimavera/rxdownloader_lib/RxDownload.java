package mprimavera.rxdownloader_lib;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ProgressBar;
import com.tbruyelle.rxpermissions2.RxPermissions;
import java.io.File;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RxDownload {
    private static int loaderId = 0;
    private String mUrl;
    private String mSaveTo;
    private Activity mActivity;
    private int progress;
    private Consumer<Integer> mConsumer;
    private boolean mUseListener, mShowProgressDialog;
    private ProgressBar mProgress;
    private String mCompletedMessage;
    private View mView;
    private static ProgressDialog mProgressDialogFragment;

    public RxDownload() {
        mProgress = null;
        mConsumer = null;
        mUseListener = false;
        mCompletedMessage = null;

        loaderId++;
    }

    public RxDownload url(String url) {
        mUrl = url;
        return this;
    }

    public RxDownload completedMessage(String message, View view) {
        mCompletedMessage = message;
        mView = view;
        return this;
    }

    public RxDownload messageView(View view) {
        mView = view;
        return this;
    }

    public RxDownload completedMessage(int res, View view) {
        mCompletedMessage = mActivity.getResources().getString(res);
        mView = view;
        return this;
    }

    public RxDownload progressInto(ProgressBar progress) {
        mProgress = progress;
        return this;
    }

    public RxDownload listener(Consumer<Integer> consumer) {
        mConsumer = consumer;
        mUseListener = true;
        return this;
    }

    public RxDownload saveTo(String path) {
        mSaveTo = path;
        return this;
    }

    public RxDownload showDialog() {
        mShowProgressDialog = true;
        return this;
    }

    public RxDownload activity(Activity activity) {
        mActivity = activity;
        return this;
    }

    public RxDownload start() {
        if(!mUseListener && mProgress == null && !mShowProgressDialog)
            return null;

        RxPermissions rxPermissions = new RxPermissions(mActivity); // where this is an Activity instance
        rxPermissions
            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe(granted -> {
                if (granted) {
                    // Create Destination File
                    File saveTo = Tools.getOrCreateFile(mSaveTo);

                    // Download Observable
                    Observable<DownloadService.TransferProgress> download = DownloadService
                        .downloadFile(saveTo, mUrl)
                        .compose(RxTools.bind(mActivity, loaderId))
                        .doOnError(throwable -> {
                            if (mView != null)
                                DialogBuilder.showMessage("Network resource not available", mView);
                        })
                        .doOnTerminate(() -> {
                            if(mCompletedMessage != null) {
                                DialogBuilder.showMessage(mCompletedMessage, mView);
                            }
                        });

                    // Show Progress Dialog Fragment
                    if(mShowProgressDialog) {
                        download = download
                            .compose(showDialog(mActivity, R.string.app_name));
                    }

                    // Subscribe to Observable
                    download
                        .subscribe(transferProgress -> {
                            int progress1 = transferProgress.getProgress();
                            if(mProgress != null) {
                                mProgress.setProgress(progress1);
                            } else if (mUseListener) {
                                mConsumer.accept(progress1);
                            } else if(mProgressDialogFragment != null) {
                                mProgressDialogFragment.setProgress(progress1);
                                mProgressDialogFragment.setSpeed(transferProgress.getSpeed());
                                mProgressDialogFragment.setTotal(
                                        transferProgress.getTotal(),
                                        transferProgress.getLength()
                                );
                            }
                        }, throwable -> {});
                }
            });

        return this;
    }

    public static <T> ObservableTransformer<T, T> showDialog(final Activity activity, int messageRes) {
        return observable -> Observable.fromCallable(() -> {
            showProgressDialog(activity, messageRes);
            return true;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .flatMap(t -> observable)
        .doOnTerminate(() -> dismissProgressDialog());
    }

    public static void showProgressDialog(Activity activity, int messageRes) {
        Bundle args = new Bundle();
        args.putInt("message_res", messageRes);
        mProgressDialogFragment = new ProgressDialog(activity);
        mProgressDialogFragment.setArguments(args);
        mProgressDialogFragment.show(
            ((FragmentActivity)activity).getSupportFragmentManager(),
            "progressdialog"
        );
    }

    public static void dismissProgressDialog() {
        if (mProgressDialogFragment.getActivity() != null) {
            mProgressDialogFragment.dismissAllowingStateLoss();
        }
        mProgressDialogFragment = null;
    }
}
