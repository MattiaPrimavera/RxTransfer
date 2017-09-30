package mprimavera.rxdownloader;


import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import mprimavera.rxdownloader_lib.RxDownload;

public class ProgressBarListSample extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar_list_sample);

        LinearLayout layout = findViewById(R.id.mainLayout);


        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String testFilePath = path + "/testFile";
        String url = "http://ipv4.download.thinkbroadband.com/200MB.zip";

        for(int i = 0; i < 20; i++) {
            ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progress.setProgress(0);
            progress.setMax(100);
            progress.setIndeterminate(false);

            new RxDownload()
                .activity(this)
                .saveTo(testFilePath)
                .progressInto(progress)
                .url(url)
                .start();

            layout.addView(progress);
        }
    }
}
