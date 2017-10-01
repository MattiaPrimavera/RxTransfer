package mprimavera.rxdownloader;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;

import mprimavera.rxdownloader_lib.RxDownload;

public class ProgressBarSample extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar_sample);

        ProgressBar progress = findViewById(R.id.progress);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String testFilePath = path + "/testFile";
        String url = "http://ipv4.download.thinkbroadband.com/200MB.zip";

        Log.d("TEST", "Sample Bar download eample");
        new RxDownload(this, 2)
            .saveTo(testFilePath)
            .progressInto(progress)
            .url(url)
            .start();
    }
}
