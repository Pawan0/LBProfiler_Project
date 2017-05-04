package trainedge.lbprofiler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import static trainedge.lbprofiler.R.id.etProfile;
import static trainedge.lbprofiler.R.id.tvAddress_Get;

public class ProfileCreation extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    TextView Locaddress;
    Button btnCreate;
    private EditText etName;
    private EditText etGeofence;
    private Spinner spRing;
    private Spinner spMsg;
    private SeekBar skVolume;
    private Switch sVibrate;
    private Switch sSilentMode;
    private Uri[] ringtone;
    private Uri[] msgtone;
    private Uri ring;
    private Uri msg;
    private String address;
    private Double lat;
    private Double lng;

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

    private void bindViews() {

        Locaddress = (TextView) findViewById(tvAddress_Get);
        etName = (EditText) findViewById(etProfile);
        etGeofence = (EditText) findViewById(R.id.etGeofence);
        spRing = (Spinner) findViewById(R.id.spRing);
        spMsg = (Spinner) findViewById(R.id.spMsgTone);
        skVolume = (SeekBar) findViewById(R.id.skVolume);
        sVibrate = (Switch) findViewById(R.id.sVibration);
        sSilentMode = (Switch) findViewById(R.id.sSilentMode);
        btnCreate = (Button) findViewById(R.id.btnCreate);


    }

    private void initViews() {
        ringtone = getRingtone();
        ArrayAdapter<Uri> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ringtone);
        spRing.setAdapter(adapter);
        msgtone = getMessageTone();
        ArrayAdapter<Uri> adapterMsg = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, msgtone);
        spMsg.setAdapter(adapterMsg);
        setupListeners();
    }

    private void handleLocationData() {
        Bundle extras = getIntent().getExtras();
        address = extras.getString("trainedge.lbprofiler.address");
        lat = extras.getDouble("trainedge.lbprofiler.latitude");
        lng = extras.getDouble("trainedge.lbprofiler.longitude");
        updateUI(address, lat, lng);
    }

    private void setupListeners() {
        spRing.setOnItemSelectedListener(this);
        spMsg.setOnItemSelectedListener(this);
        btnCreate.setOnClickListener(this);
    }

    public Uri[] getRingtone() {
        RingtoneManager ringtoneMgr = new RingtoneManager(this);
        ringtoneMgr.setType(RingtoneManager.TYPE_RINGTONE);
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

    private void updateUI(String address, Double lat, Double lng) {
        Locaddress.setText(address);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (view.getId()) {
            case R.id.spRing:
                ring = ringtone[position];
                break;
            case R.id.spMsgTone:
                msg = msgtone[position];
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCreate:
                createNewSoundProfile();
                break;
        }
    }

    private void createNewSoundProfile() {

        String profileName = etName.getText().toString().trim();
        String geoFenceStr = etGeofence.getText().toString().trim();
        if (profileName.isEmpty()) {
            etName.setError("Enter a profile name");
            return;
        }
        if (geoFenceStr.isEmpty()) {
            etGeofence.setError("Enter a geofence size in meters");
            return;
        }
        if (sSilentMode.isChecked()) {
            Toast.makeText(this, "no ringtone will be audible", Toast.LENGTH_SHORT).show();
            sVibrate.setChecked(false);
            saveSoundProfile(profileName, geoFenceStr, address, lat, lng, 0, null, null, false, true);
        }
        int volume = skVolume.getProgress();
        saveSoundProfile(profileName, geoFenceStr, address, lat, lng, volume,ringtone[spRing.getSelectedItemPosition()], msgtone[spMsg.getSelectedItemPosition()], sVibrate.isChecked(), false);

    }

    private void saveSoundProfile(final String profileName, String geoFenceStr, String address, Double lat, Double lng, int volume, Uri ring, Uri msg, boolean vibrate, boolean silent) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.show();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference profilesRef = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child(profileName);
        HashMap<String, Object> profileData = new HashMap<>();
        profileData.put("silent", silent);
        profileData.put("vibrate", vibrate);
        if (ring == null) {
            profileData.put("ringtone", "n/a");
        } else {
            profileData.put("ringtone", ring.toString());
        }
        if (msg == null) {
            profileData.put("msgtone", "n/a");
        } else {
            profileData.put("msgtone", msg.toString());
        }
        profileData.put("volume", volume);
        profileData.put("address", address);
        profileData.put("lat", lat);
        profileData.put("lng", lng);
        profileData.put("radius", geoFenceStr);
        profilesRef.setValue(profileData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Toast.makeText(ProfileCreation.this, "saved", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                createGeofenceforProfile(profileName);
            }
        });
    }

    private void createGeofenceforProfile(String profileName) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child("geofire");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(profileName, new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    Toast.makeText(ProfileCreation.this, "There was an error saving the location to GeoFire: " + error, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(ProfileCreation.this, "success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileCreation.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }
}
