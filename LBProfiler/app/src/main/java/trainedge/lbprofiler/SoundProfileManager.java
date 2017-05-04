package trainedge.lbprofiler;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class SoundProfileManager {

    Context context;
    private ProfileModel profileModel;

    public SoundProfileManager(Context context) {
        this.context = context;

    }

    public void changeSoundProfile(String key, GeoLocation location) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference profilesRef = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child(key);
        Toast.makeText(context, "key= " + key, Toast.LENGTH_SHORT).show();
        profilesRef.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    profileModel = new ProfileModel(dataSnapshot);
                    updateSoundProfile(profilesRef);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSoundProfile(DatabaseReference profilesRef) {
        boolean state = profileModel.getState();
        boolean isSilent = profileModel.isSilent();
        boolean isVibrate = profileModel.isVibrate();
        String name = profileModel.getKey();
        final AudioManager profileMode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (isSilent) {
            profileMode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } else if (isVibrate) {
            profileMode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        } else {
            int volume = profileModel.getVolume();
            String msgtone = profileModel.getMsgtone();
            String ringtone = profileModel.getRingtone();
            profileMode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            RingtoneManager.setActualDefaultRingtoneUri(
                    context,
                    RingtoneManager.TYPE_RINGTONE,
                    Uri.parse(ringtone)
            );
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, Uri.parse(msgtone));
            profileMode.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
        }
        ProfileGeofenceNotification.notify(context, "Sound Profile loaded ->" + name, 0);
        /*HashMap<String, Object> profileData = new HashMap<>();
        profileData.put("silent", profileModel.isSilent());
        profileData.put("vibrate", profileModel.isVibrate());
        profileData.put("ringtone", profileModel.getRingtone());
        profileData.put("msgtone", profileModel.getMsgtone());
        profileData.put("volume", profileModel.getVolume());
        profileData.put("address", profileModel.getAddress());
        profileData.put("lat", profileModel.getLat());
        profileData.put("lng", profileModel.getLng());
        profileData.put("radius", profileModel.getRadius());
        profileData.put("state", true);
        profilesRef.setValue(profileData);*/


    }

    public void setToDefualt() {

    }
}
