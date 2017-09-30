package mprimavera.rxdownloader_lib;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import com.philosophicalhacker.lib.RxLoader;
import io.reactivex.ObservableTransformer;

public class RxTools {
    public static <T> ObservableTransformer<T, T> bind(Activity activity, int id) {
        RxLoader rxLoader = new RxLoader(activity, ((AppCompatActivity)activity).getSupportLoaderManager());
        return observable -> observable
                .compose(rxLoader.makeObservableTransformer(id));
    }
}
