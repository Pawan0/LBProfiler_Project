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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static trainedge.lbprofiler.ProfileAdapter.PROFILE_NAME;
import static trainedge.lbprofiler.R.id.etProfile;
import static trainedge.lbprofiler.R.id.tvAddress_Get;

public class ViewProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

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

    private Double lat;
    private Double lng;
    private String key;
    private String uid;
    private DatabaseReference profilesRef;
    private ProfileModel profileModel;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        bindViews();
        if (getIntent() != null) {
            handleProfileData();
        }
        initViews();

    }

    private void handleProfileData() {
        Bundle extras = getIntent().getExtras();
        key = extras.getString(PROFILE_NAME);
        loadDataFromFirebase(key);

    }

    private void loadDataFromFirebase(String key) {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        profilesRef = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child(key);
        profilesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    profileModel = new ProfileModel(dataSnapshot);
                    updateUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void bindViews() {
        Locaddress = (TextView) findViewById(R.id.tvAddress);
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

    private void updateUI() {
        lat = profileModel.getLat();
        lng = profileModel.getLng();
        etName.setText(profileModel.getKey());
        etName.setEnabled(false);
        address = profileModel.getAddress();
        Locaddress.setText(address);
        skVolume.setProgress(profileModel.getVolume());
        sVibrate.setChecked(profileModel.isVibrate());
        sSilentMode.setChecked(profileModel.isSilent());
        String msgtoneSelected = profileModel.getMsgtone();
        for (int i = 0; i < msgtone.length; i++) {
            if (msgtone[i].toString().equals(msgtoneSelected)) {
                spMsg.setSelection(i);
            }
        }
        String ringtoneSelected = profileModel.getRingtone();
        for (int i = 0; i < ringtone.length; i++) {
            if (ringtone[i].toString().equals(ringtoneSelected)) {
                spRing.setSelection(i);
            }
        }
        etGeofence.setText(profileModel.getRadius());
    }


    private void setupListeners() {
        spRing.setOnItemSelectedListener(this);
        spMsg.setOnItemSelectedListener(this);
        btnCreate.setOnClickListener(this);
        sVibrate.setOnCheckedChangeListener(this);
        sSilentMode.setOnCheckedChangeListener(this);
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
                editSoundProfile();
                break;
        }
    }

    private void editSoundProfile() {
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
        saveSoundProfile(profileName, geoFenceStr, address, lat, lng, volume, ringtone[spRing.getSelectedItemPosition()], msgtone[spMsg.getSelectedItemPosition()], sVibrate.isChecked(), false);

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
        profileData.put("state", false);
        profilesRef.setValue(profileData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Toast.makeText(ViewProfileActivity.this, "saved", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Toast.makeText(ViewProfileActivity.this, "success", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sVibration:
                if (isChecked) {
                    sSilentMode.setChecked(false);
                }
                break;
            case R.id.sSilentMode:

                break;
        }
    }
}
