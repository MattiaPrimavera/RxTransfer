package mprimavera.rxdownloader_lib;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

public class DownloadService {
    public static class TransferProgress {
        private int progress;
        private double speed;

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        private long total;
        private long length;

        public TransferProgress(int progress, double speed, long total, long length) {
            this.progress = progress;
            this.speed = speed;
            this.total = total;
            this.length = length;
        }
    }

    public static final int BUFFER_LENGTH = 2048;

    public static Observable<TransferProgress> downloadFile(final File outputFile, final String url) {
        return Observable.<TransferProgress>create(new ObservableOnSubscribe<TransferProgress>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<TransferProgress> emitter) throws Exception {
                int percent = 0;

                if (false) { // Check network connected
                    emitter.onError(new Exception());
                } else {
                    InputStream input = null;
                    OutputStream output = null;
                    try {
                        Request request = new Request.Builder()
                                .url(url)
                                .build();

                        okhttp3.Response response = getOkHttpClient().newCall(request).execute();
                        if (response.isSuccessful()) {
                            input = response.body().byteStream();
                            long tlength = response.body().contentLength();

                            output = new FileOutputStream(outputFile);
                            byte data[] = new byte[BUFFER_LENGTH];

                            emitter.onNext(new TransferProgress(percent, 0, 0, tlength));
                            long total = 0;
                            int count;

                            long totalDownloaded = 0;
                            long firstTime = System.currentTimeMillis();
                            while ((count = input.read(data)) != -1) {
                                total += count;

                                output.write(data, 0, count);

                                int newPercent = (int) (total * 100 / tlength);
                                if (newPercent != percent) {
                                    long secondTime = System.currentTimeMillis();
                                    double diff = (secondTime - firstTime) / 1000.0;
                                    double speed = (total - totalDownloaded / 1024.0 / 1024.0) / diff;
                                    totalDownloaded = total;

                                    DecimalFormat df = new DecimalFormat("0.00##");
                                    String result = df.format(speed);

                                    percent = newPercent;
                                    emitter.onNext(new TransferProgress(percent, Double.parseDouble(result), total, tlength));
                                    firstTime = secondTime;
                                }
                            }

                            output.flush();
                            output.close();
                            input.close();
                        }
                    } catch (IOException e) {
                        emitter.onError(e);
                    } finally {
                        if (input != null) {
                            try {
                                input.close();
                            } catch (IOException ioe) {
                            }
                        }
                        if (output != null) {
                            try {
                                output.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }

                emitter.onComplete();
            }
        });
    }

    public static OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if(BuildConfig.DEBUG) { // Adding Logger interceptors for DEBUG build only
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            // Can be Level.BASIC, Level.HEADERS, or Level.BODY
            // See http://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
        }
        return builder.build();
    }
}
