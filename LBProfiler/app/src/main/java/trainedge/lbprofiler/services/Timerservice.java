package trainedge.lbprofiler.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import trainedge.lbprofiler.AlertActivity;

/**
 * Created by xaidi on 05-05-2017.
 */

public class Timerservice extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    Context mContext;
    public static final String ACTION = "trainedge.lbprofiler.services.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {


        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if ( !manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            Intent i1 = new Intent(context, AlertActivity.class);
            i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i1);

        }else{
            Intent i = new Intent(context, GeofenceService.class);
            context.startService(i);
        }
    }

}
