package trainedge.lbprofiler;

import com.google.firebase.database.DataSnapshot;


class ProfileModel {
    private final String key;
    private final Boolean state;

    public Boolean getState() {
        return state;
    }

    private String address;
    private Double lat;
    private Double lng;
    private String msgtone;
    private String radius;
    private String ringtone;
    private boolean silent;
    private boolean vibrate;
    private int volume;

    public ProfileModel(DataSnapshot dataSnapshot) {
        address = dataSnapshot.child("address").getValue(String.class);
        lat = dataSnapshot.child("lat").getValue(Double.class);
        lng = dataSnapshot.child("lng").getValue(Double.class);
        ringtone = dataSnapshot.child("ringtone").getValue(String.class);
        msgtone = dataSnapshot.child("msgtone").getValue(String.class);
        radius = dataSnapshot.child("radius").getValue(String.class);
        silent = dataSnapshot.child("silent").getValue(Boolean.class);
        vibrate = dataSnapshot.child("vibrate").getValue(Boolean.class);
        state = dataSnapshot.child("state").getValue(Boolean.class);
        volume = dataSnapshot.child("volume").getValue(Integer.class);
        key = dataSnapshot.getKey();
    }


    public String getKey() {
        return key;
    }

    public String getAddress() {
        return address;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getMsgtone() {
        return msgtone;
    }

    public String getRadius() {
        return radius;
    }

    public String getRingtone() {
        return ringtone;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public int getVolume() {
        return volume;
    }
}
