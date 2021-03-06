package mprimavera.rxdownloader_lib;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxTools {
    public static ProgressDialog progressDialog;

    public static <T> ObservableTransformer<T, T> applySchedulers() {
        return observable -> observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
