package mprimavera.rxdownloader;


import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import mprimavera.rxdownloader_lib.DialogBuilder;
import mprimavera.rxdownloader_lib.RxDownload;

public class ListenerSample extends AppCompatActivity {
    private LinearLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listener_sample);
        mLayout = findViewById(R.id.mainLayout);

        final ProgressBar progressBar = findViewById(R.id.progress);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String testFilePath = path + "/testFile";
        String url = "http://ipv4.download.thinkbroadband.com/200MB.zip";

        new RxDownload()
            .activity(this)
            .saveTo(testFilePath)
            .listener(progress -> {
                progressBar.setProgress(progress);

                if(progress == 100)
                    DialogBuilder.showMessage("Operation Completed", mLayout);
            })
            .url(url)
            .start();
    }
}
