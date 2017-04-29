package trainedge.lbprofiler;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddGeofenceFragment extends DialogFragment {

    private static final int REQUEST_RINGTONE_CODE = 5;

    // region Properties

    private ViewHolder viewHolder;
    private String chosenRingtone;
    private String address;
    private double lat;
    private double lng;

    private ViewHolder getViewHolder() {
        return viewHolder;
    }

    AddGeofenceFragmentListener listener;

    public void setListener(AddGeofenceFragmentListener listener) {
        this.listener = listener;
    }

    // endregion

    // region Overrides

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dailog_add_geofence, null);

        viewHolder = new ViewHolder();
        viewHolder.populate(view);
        viewHolder.addressEditText.setText(address);
        viewHolder.latitudeEditText.setText(String.valueOf(lat));
        viewHolder.longitudeEditText.setText(String.valueOf(lng));
        viewHolder.notificationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                startActivityForResult(intent, REQUEST_RINGTONE_CODE);
            }
        });
        final Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        final int day = c.get(Calendar.DAY_OF_MONTH);
        viewHolder.dateEditText.setText(day + "-" + (month + 1) + "-" + year);
        viewHolder.dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create a new instance of DatePickerDialog and return it
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        viewHolder.dateEditText.setText(dayOfMonth + "-" + month + "-" + year);

                    }
                }, year, month, day).show();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.Add, null)
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddGeofenceFragment.this.getDialog().cancel();

                        if (listener != null) {
                            listener.onDialogNegativeClick(AddGeofenceFragment.this);
                        }
                    }
                });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (dataIsValid()) {
                            NamedGeofence geofence = new NamedGeofence();
                            geofence.name = getViewHolder().taskNameEditText.getText().toString();
                            geofence.address = address;
                            geofence.latitude = Double.parseDouble(getViewHolder().latitudeEditText.getText().toString());
                            geofence.longitude = Double.parseDouble(getViewHolder().longitudeEditText.getText().toString());
                            geofence.radius = Float.parseFloat(getViewHolder().radiusEditText.getText().toString()) * 1000.0f;
                            geofence.ringtone = chosenRingtone;

                            // upload to cloud
                            uploadtoFirebase(
                                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    getViewHolder().taskNameEditText.getText().toString(),
                                    address,
                                    lat,
                                    lng,
                                    Float.parseFloat(getViewHolder().radiusEditText.getText().toString()) * 1000.0f,
                                    chosenRingtone,
                                    viewHolder.dateEditText.getText().toString()
                            );

                            if (listener != null) {
                                listener.onDialogPositiveClick(AddGeofenceFragment.this, geofence);
                                dialog.dismiss();
                            }
                        } else {
                            // error message
                            showValidationErrorToast();
                        }
                    }

                });

            }
        });

        return dialog;
    }

    private void uploadtoFirebase(String uid, String taskName, String address, double lat, double lng, float radius, String chosenRingtone, String dateStr) {

        Map<String, Object> map = new HashMap<>();
        map.put("name", taskName);
        map.put("address", address);
        map.put("lat", lat);
        map.put("lng", lng);
        map.put("raduis", radius);
        map.put("ringtone", chosenRingtone);
        map.put("date", dateStr);
        map.put("status", false);
        FirebaseDatabase.getInstance().getReference("tasks").child(uid).push().setValue(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Toast.makeText(getContext(), "success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // endregion

    // region Private

    private boolean dataIsValid() {
        boolean validData = true;

        String name = getViewHolder().taskNameEditText.getText().toString();
        String latitudeString = getViewHolder().latitudeEditText.getText().toString();
        String longitudeString = getViewHolder().longitudeEditText.getText().toString();
        String radiusString = getViewHolder().radiusEditText.getText().toString();
        String dateStr = viewHolder.dateEditText.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(latitudeString) || TextUtils.isEmpty(dateStr)
                || TextUtils.isEmpty(longitudeString) || TextUtils.isEmpty(radiusString)) {
            validData = false;
        } else {
            double latitude = Double.parseDouble(latitudeString);
            double longitude = Double.parseDouble(longitudeString);
            float radius = Float.parseFloat(radiusString);
            if ((latitude < FetchAddressIntentService.Constants.Geometry.MinLatitude || latitude > FetchAddressIntentService.Constants.Geometry.MaxLatitude)
                    || (longitude < FetchAddressIntentService.Constants.Geometry.MinLongitude || longitude > FetchAddressIntentService.Constants.Geometry.MaxLongitude)
                    || (radius < FetchAddressIntentService.Constants.Geometry.MinRadius || radius > FetchAddressIntentService.Constants.Geometry.MaxRadius)) {
                validData = false;
            }
        }

        return validData;
    }

    private void showValidationErrorToast() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.Toast_Validation), Toast.LENGTH_SHORT).show();
    }

    public static AddGeofenceFragment newInstance(String address, double lat, double lng) {
        AddGeofenceFragment fragment = new AddGeofenceFragment();
        Bundle args = new Bundle();
        args.putString("address", address);
        args.putDouble("lat", lat);
        args.putDouble("lng", lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            address = getArguments().getString("address", "no address");
            lat = getArguments().getDouble("lat", 0.0);
            lng = getArguments().getDouble("lng", 0.0);
        }
    }

    // endregion

    // region Interfaces

    public interface AddGeofenceFragmentListener {
        void onDialogPositiveClick(DialogFragment dialog, NamedGeofence geofence);

        void onDialogNegativeClick(DialogFragment dialog);
    }

    // endregion

    // region Inner classes

    static class ViewHolder {
        EditText taskNameEditText;
        EditText latitudeEditText;
        EditText longitudeEditText;
        EditText radiusEditText;
        TextView notificationTextView;
        EditText addressEditText;
        TextView dateEditText;

        public void populate(View v) {
            taskNameEditText = (EditText) v.findViewById(R.id.Listitem_geofenceName);
            latitudeEditText = (EditText) v.findViewById(R.id.fragment_add_geofence_latitude);
            longitudeEditText = (EditText) v.findViewById(R.id.fragment_add_geofence_longitude);
            radiusEditText = (EditText) v.findViewById(R.id.fragment_add_geofence_radius);
            dateEditText = (TextView) v.findViewById(R.id.fragment_add_geofence_date);
            notificationTextView = (TextView) v.findViewById(R.id.add_geofence_notification);
            addressEditText = (EditText) v.findViewById(R.id.add_geofence_address);

            latitudeEditText.setHint(String.format(v.getResources().getString(R.string.Hint_Latitude), FetchAddressIntentService.Constants.Geometry.MinLatitude, FetchAddressIntentService.Constants.Geometry.MaxLatitude));
            longitudeEditText.setHint(String.format(v.getResources().getString(R.string.Hint_Longitude), FetchAddressIntentService.Constants.Geometry.MinLongitude, FetchAddressIntentService.Constants.Geometry.MaxLongitude));
            radiusEditText.setHint(String.format(v.getResources().getString(R.string.Hint_Radius), FetchAddressIntentService.Constants.Geometry.MinRadius, FetchAddressIntentService.Constants.Geometry.MaxRadius));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 5) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            String name = data.getStringExtra(RingtoneManager.EXTRA_RINGTONE_TITLE);
            if (uri != null) {
                chosenRingtone = uri.getPath().toString();
                viewHolder.notificationTextView.setText(name);
            } else {
                chosenRingtone = null;
            }
        }
    }
}