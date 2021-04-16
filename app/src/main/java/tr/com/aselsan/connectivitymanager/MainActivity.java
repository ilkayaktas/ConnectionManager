package tr.com.aselsan.connectivitymanager;

import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "XXXXX";
    NetworkCapabilities caps;
    LinkProperties linkProperties;
    TextView download;
    TextView upload;
    ImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        download = findViewById(R.id.download);
        upload = findViewById(R.id.upload);
        icon = findViewById(R.id.icon);

        ConnectionManager connectionManager = ConnectionManager.getInstance(this);
        connectionManager.register();

        connectionManager.getConnectionStatePublisher()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectionState -> {
                    switch (connectionState.connection){
                        case INTERNET_ONLINE:
                            if (connectionState.transport != null){
                                switch (connectionState.transport){
                                    case UNSTABLE:
                                        icon.setImageDrawable(getDrawable(R.drawable.unstable_network));
                                        break;
                                    case TWO_G:
                                        icon.setImageDrawable(getDrawable(R.drawable.two_g));
                                        break;
                                    case THREE_G:
                                        icon.setImageDrawable(getDrawable(R.drawable.three_g));
                                        break;
                                    case FOUR_G:
                                        icon.setImageDrawable(getDrawable(R.drawable.four_g));
                                        break;
                                    case WIFI:
                                        icon.setImageDrawable(getDrawable(R.drawable.wifi));
                                        break;
                                }
                                icon.setScaleType(ImageView.ScaleType.FIT_XY);
                                download.setText(String.valueOf(connectionState.downloadBandwithKbps)+" kbps");
                                upload.setText(String.valueOf(connectionState.uploadBandwithKbps)+" kbps");
                            }

                            break;
                        case INTERNET_OFFLINE:
                            icon.setImageDrawable(getDrawable(R.drawable.connection_offline));
                            icon.setScaleType(ImageView.ScaleType.FIT_XY);
                            download.setText("-");
                            upload.setText("-");
                            break;
                    }
                }, throwable -> {
                    Log.d("XXXXXX", throwable.toString());
                });


    }
}