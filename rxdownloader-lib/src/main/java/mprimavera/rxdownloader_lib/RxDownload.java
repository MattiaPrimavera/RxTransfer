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
    private boolean mUseListener;
    private ProgressDialog mProgressDialogFragment;
    private ProgressBar mProgress;
    private String mCompletedMessage;
    private View mView;

    public RxDownload() {
        mProgress = null;
        mProgressDialogFragment = null;
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

    public RxDownload showDialog() {
        getProgressDialog(mActivity, R.string.app_name);
        return this;
    }

    public RxDownload saveTo(String path) {
        mSaveTo = path;
        return this;
    }

    public RxDownload activity(Activity activity) {
        mActivity = activity;
        return this;
    }

    public RxDownload start() {
        if(!mUseListener && mProgress == null && mProgressDialogFragment == null)
            return null;

        RxPermissions rxPermissions = new RxPermissions(mActivity); // where this is an Activity instance
        rxPermissions
            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe(granted -> {
                if (granted) {
                    File saveTo = Tools.getOrCreateFile(mSaveTo);
                    Observable<DownloadService.TransferProgress> download = DownloadService
                        .downloadFile(saveTo, mUrl)
                        .compose(RxTools.bind(mActivity, loaderId))
                        .doOnError(throwable -> {
                            if (mView != null)
                                DialogBuilder.showMessage("Network resource not available", mView);
                        })
                        .doOnTerminate(() -> {
                            if(mProgressDialogFragment != null)
                                dismissProgressDialog();

                            if(mCompletedMessage != null) {
                                DialogBuilder.showMessage(mCompletedMessage, mView);
                            }
                        });

                    if(mProgressDialogFragment != null) {
                        Observable.fromCallable(() -> {
                            showProgressDialog();
                            return true;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(t -> download)
                        .subscribe(transferProgress -> {
                            int progress1 = transferProgress.getProgress();
                            if(mProgressDialogFragment != null) {
                                mProgressDialogFragment.setProgress(progress1);
                                mProgressDialogFragment.setSpeed(transferProgress.getSpeed());
                                mProgressDialogFragment.setTotal(
                                        transferProgress.getTotal(),
                                        transferProgress.getLength()
                                );
                            } else if(mProgress != null) {
                                mProgress.setProgress(progress1);
                            } else if (mUseListener) {
                                mConsumer.accept(progress1);
                            }
                        }, throwable -> {});
                    } else {
                        download
                            .subscribe(transferProgress -> {
                                int progress1 = transferProgress.getProgress();
                                if(mProgress != null) {
                                    mProgress.setProgress(progress1);
                                } else if (mUseListener) {
                                    mConsumer.accept(progress1);
                                }
                            }, throwable -> {});
                    }
                }
        });

        return this;
    }

    public void getProgressDialog(Activity activity, int messageRes) {
        Bundle args = new Bundle();
        args.putInt("message_res", messageRes);
        mProgressDialogFragment = new ProgressDialog(activity);
        mProgressDialogFragment.setArguments(args);
    }

    public void showProgressDialog() {
        mProgressDialogFragment.show(
            ((FragmentActivity)mActivity).getSupportFragmentManager(),
            "progressdialog"
        );
    }

    public void dismissProgressDialog() {
        if (mProgressDialogFragment.getActivity() != null) {
            mProgressDialogFragment.dismissAllowingStateLoss();
        }
        mProgressDialogFragment = null;
    }
}
