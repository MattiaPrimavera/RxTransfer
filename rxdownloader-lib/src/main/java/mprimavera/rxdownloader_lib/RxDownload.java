package mprimavera.rxdownloader_lib;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ProgressBar;
import com.tbruyelle.rxpermissions2.RxPermissions;
import java.io.File;
import java.util.HashMap;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RxDownload {
    private String mUrl;
    private String mSaveTo;
    private Activity mActivity;
    private Consumer<Integer> mConsumer;
    private boolean mUseListener, mShowProgressDialog;
    private ProgressBar mProgress;
    private String mCompletedMessage;
    private View mView;
    private int mId;
    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks;
    private static Disposable mDownload;
    private static HashMap<Integer, Observable<DownloadService.TransferProgress>> mCache = new HashMap<>();

    public RxDownload(Activity activity, int id) {
        mId = id;
        mActivity = activity;
        mProgress = null;
        mConsumer = null;
        mUseListener = false;
        mCompletedMessage = null;
        mActivityLifecycleCallbacks = getLyfecicleCallbacks();
        activity.getApplication().registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
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
                    mDownload = download(mUrl, saveTo)
                        .subscribe(transferProgress -> {
                            int progress1 = transferProgress.getProgress();
                            if(mProgress != null) {
                                mProgress.setProgress(progress1);
                            } else if (mUseListener) {
                                mConsumer.accept(progress1);
                            } else if(RxTools.progressDialog != null) {
                                RxTools.progressDialog.setSpeed(transferProgress.getSpeed());
                                RxTools.progressDialog.setTotal(
                                        transferProgress.getTotal(),
                                        transferProgress.getLength()
                                );
                                RxTools.progressDialog.setProgress(progress1);
                            }
                        }, throwable -> {});
                }
            });

        return this;
    }

    public Observable<DownloadService.TransferProgress> download(String url, File saveTo) {
        if(mCache.containsKey(mId)) {
            Observable<DownloadService.TransferProgress> observable = mCache.get(mId);
            return observable;
        }

        // Download Observable
        Observable<DownloadService.TransferProgress> download = DownloadService
                .downloadFile(saveTo, url)
                .doOnError(throwable -> {
                    if (mView != null)
                        DialogBuilder.showMessage("Network resource not available", mView);
                })
                .compose(RxTools.applySchedulers())
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

        // Subscr@ibe to Observable
        download = download.cache();
        mCache.put(mId, download);
        return download;
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

        RxTools.progressDialog = new ProgressDialog(activity);
        RxTools.progressDialog.setArguments(args);
        RxTools.progressDialog.show(
            ((FragmentActivity)activity).getSupportFragmentManager(),
            "progressdialog"
        );
    }

    public static void dismissProgressDialog() {
        if (RxTools.progressDialog.getActivity() != null) {
            RxTools.progressDialog.dismissAllowingStateLoss();
        }
        RxTools.progressDialog = null;
    }

    public Application.ActivityLifecycleCallbacks getLyfecicleCallbacks() {
        return new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle bundle) {
            }
            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityResumed(Activity activity) {}
            @Override public void onActivityPaused(Activity activity) {}
            @Override public void onActivityStopped(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}
            @Override
            public void onActivityDestroyed(Activity activity) {
                if(activity.isFinishing()) { // Back pressed case
                    if(!mDownload.isDisposed()) {
                        mDownload.dispose();
                        mCache.remove(mUrl + mSaveTo + mId);
                    }
                }
            }
        };
    }
}
