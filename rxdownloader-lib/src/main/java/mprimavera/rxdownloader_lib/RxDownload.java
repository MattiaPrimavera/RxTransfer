package mprimavera.rxdownloader_lib;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
    private IProgressListener mListener;
    private ProgressDialog mProgressDialogFragment;

    public RxDownload() {
        mListener = null;
    }

    public RxDownload url(String url) {
        mUrl = url;
        return this;
    }

    public RxDownload saveTo(String path) {
        mSaveTo = path;
        return this;
    }

    public RxDownload listener(IProgressListener listener) {
        mListener = listener;
        return this;
    }

    public RxDownload activity(Activity activity) {
        mActivity = activity;
        return this;
    }

    public RxDownload start() {
        if(mListener != null)
            return null;

        getProgressDialog(mActivity, R.string.app_name);

        RxPermissions rxPermissions = new RxPermissions(mActivity); // where this is an Activity instance
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (granted) {
                            File saveTo = new File(mSaveTo);
                            if(!saveTo.exists()) {
                                try {
                                    saveTo.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            Observable<DownloadService.TransferProgress> download = DownloadService
                                    .downloadFile(saveTo, mUrl)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnSubscribe(new Consumer<Disposable>() {
                                        @Override
                                        public void accept(Disposable disposable) throws Exception {
                                            showProgressDialog();
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
                                            dismissProgressDialog();
                                        }
                                    });

                            download
                                    .subscribe(new Consumer<DownloadService.TransferProgress>() {
                                        @Override
                                        public void accept(DownloadService.TransferProgress transferProgress) throws Exception {
                                            mProgressDialogFragment.setProgress(transferProgress.getProgress());
                                            mProgressDialogFragment.setSpeed(transferProgress.getSpeed());
                                            mProgressDialogFragment.setTotal(transferProgress.getTotal(), transferProgress.getLength());
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

    public interface IProgressListener {
        void update(int progress);
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
