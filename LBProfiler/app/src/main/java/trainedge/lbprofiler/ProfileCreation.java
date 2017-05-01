package trainedge.lbprofiler;

import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import static trainedge.lbprofiler.R.id.etProfile;
import static trainedge.lbprofiler.R.id.tvAddress_Get;

public class ProfileCreation extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    TextView Locaddress;
    private EditText etName;
    private EditText etGeofence;
    private Spinner spRing;
    private Spinner spMsg;
    private SeekBar skVolume;
    private Switch sVibrate;
    private Uri[] ringtone;
    private Uri[] msgtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);
        bindViews();
        if (getIntent() != null) {
            handleLocationData();
        }
        initViews();

    }

    private void initViews() {
        ringtone = getRingtone();
        ArrayAdapter<Uri> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ringtone);
        spRing.setAdapter(adapter);
        spRing.setOnItemSelectedListener(this);
        msgtone = getMessageTone();
        ArrayAdapter<Uri> adapterMsg = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, msgtone);
        spMsg.setAdapter(adapterMsg);
        spMsg.setOnItemSelectedListener(this);
    }

    private void bindViews() {

        Locaddress = (TextView) findViewById(tvAddress_Get);
        etName = (EditText) findViewById(etProfile);
        etGeofence = (EditText) findViewById(R.id.etGeofence);
        spRing = (Spinner) findViewById(R.id.spRing);
        spMsg = (Spinner) findViewById(R.id.spMsgTone);
        skVolume = (SeekBar) findViewById(R.id.skVolume);
        sVibrate = (Switch) findViewById(R.id.sVibration);

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

    public Uri[] getRingtone() {
        RingtoneManager ringtoneMgr = new RingtoneManager(this);
        ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
        Cursor alarmsCursor = ringtoneMgr.getCursor();
        int alarmsCount = alarmsCursor.getCount();
        if (alarmsCount == 0 && !alarmsCursor.moveToFirst()) {
            return null;
        }
        Uri[] alarms = new Uri[alarmsCount];
        while (!alarmsCursor.isAfterLast() && alarmsCursor.moveToNext()) {
            int currentPosition = alarmsCursor.getPosition();
            alarms[currentPosition] = ringtoneMgr.getRingtoneUri(currentPosition);
        }
        alarmsCursor.close();
        return alarms;
    }

    public Uri[] getMessageTone() {
        RingtoneManager ringtoneMgr = new RingtoneManager(this);
        ringtoneMgr.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor alarmsCursor = ringtoneMgr.getCursor();
        int alarmsCount = alarmsCursor.getCount();
        if (alarmsCount == 0 && !alarmsCursor.moveToFirst()) {
            return null;
        }
        Uri[] alarms = new Uri[alarmsCount];
        while (!alarmsCursor.isAfterLast() && alarmsCursor.moveToNext()) {
            int currentPosition = alarmsCursor.getPosition();
            alarms[currentPosition] = ringtoneMgr.getRingtoneUri(currentPosition);
        }
        alarmsCursor.close();
        return alarms;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Uri uri = ringtone[position];

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
