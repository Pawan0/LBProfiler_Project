package trainedge.lbprofiler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import static trainedge.lbprofiler.R.id.tvAddress_Get;

public class ProfileCreation extends AppCompatActivity {
    TextView Locaddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);
        bindViews();
        if (getIntent()!= null) {
            handleLocationData();
        }

    }

    private void bindViews() {

        Locaddress= (TextView) findViewById(tvAddress_Get);


    }

    private void handleLocationData() {
        Bundle extras = getIntent().getExtras();
        String address = extras.getString("trainedge.lbprofiler.address");
        Double lat = extras.getDouble("trainedge.lbprofiler.latitude");
        Double lng = extras.getDouble("trainedge.lbprofiler.longitude");
        updateUI(address, lat, lng);
    }

    private void updateUI(String address, Double lat, Double lng) {

        Locaddress.setText(address);
           }
}
