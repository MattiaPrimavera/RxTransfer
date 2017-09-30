package mprimavera.rxdownloader_lib;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ProgressBar;
import com.tbruyelle.rxpermissions2.RxPermissions;
import java.io.File;
import java.io.IOException;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RxDownload {
    private String mUrl;
    private String mSaveTo;
    private Activity mActivity;
    private int progress;
    private Consumer<Integer> mConsumer;
    private boolean mUseListener;
    private ProgressDialog mProgressDialogFragment;
    private ProgressBar mProgress;

    public RxDownload() {
        mProgress = null;
        mProgressDialogFragment = null;
        mConsumer = null;
        mUseListener = false;
    }

    public RxDownload url(String url) {
        mUrl = url;
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
            .subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean granted) throws Exception {
                if (granted) {
                    File saveTo = getOrCreateFile(mSaveTo);
                    Observable<DownloadService.TransferProgress> download = DownloadService
                        .downloadFile(saveTo, mUrl)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                            if(mProgressDialogFragment != null) showProgressDialog();
                            }
                        })
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                            //if (mBaseView != null) mBaseView.showErrorMessage("Network resource not available");
                            }
                        })
                        .doOnTerminate(new Action() {
                            @Override
                            public void run() throws Exception {
                                if(mProgressDialogFragment != null)
                                    dismissProgressDialog();
                            }
                        });

                    download
                        .subscribe(new Consumer<DownloadService.TransferProgress>() {
                            @Override
                            public void accept(DownloadService.TransferProgress transferProgress) throws Exception {
                                int progress = transferProgress.getProgress();
                                if(mProgressDialogFragment != null) {
                                    mProgressDialogFragment.setProgress(progress);
                                    mProgressDialogFragment.setSpeed(transferProgress.getSpeed());
                                    mProgressDialogFragment.setTotal(transferProgress.getTotal(), transferProgress.getLength());
                                } else if(mProgress != null) {
                                    mProgress.setProgress(progress);
                                } else if (mUseListener) {
                                    mConsumer.accept(progress);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                            }
                        });
                    }
                    }
                });

        return this;
    }

    public File getOrCreateFile(String path) {
        File file = new File(path);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public interface IProgressListener {
        void publish(int progress);
        void error();
        void completed();
    }

    public void getProgressDialog(Activity activity, int messageRes) {
        Bundle args = new Bundle();
        args.putInt("message_res", messageRes);
        mProgressDialogFragment = new ProgressDialog(activity);
        mProgressDialogFragment.setArguments(args);
    }

    public void showProgressDialog() {
        mProgressDialogFragment.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "progressdialog");
    }

    public void dismissProgressDialog() {
        if (mProgressDialogFragment.getActivity() != null) {
            mProgressDialogFragment.dismissAllowingStateLoss();
        }
        mProgressDialogFragment = null;
    }
}
