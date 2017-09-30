package mprimavera.rxdownloader_lib;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import com.philosophicalhacker.lib.RxLoader;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxTools {
    public static ProgressDialog progressDialog;

    public static <T> ObservableTransformer<T, T> bind(Activity activity, int id) {
        RxLoader rxLoader = new RxLoader(activity, ((AppCompatActivity)activity).getSupportLoaderManager());
        return observable -> observable
                .compose(rxLoader.makeObservableTransformer(id));
    }

    public static <T> ObservableTransformer<T, T> applySchedulers() {
        return observable -> observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
