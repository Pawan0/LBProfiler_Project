package trainedge.lbprofiler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class AllGeofencesAdapter extends RecyclerView.Adapter<AllGeofencesAdapter.ViewHolder> {

  // region Properties

  private List<NamedGeofence> namedGeofences;

  private AllGeofencesAdapterListener listener;

  public void setListener(AllGeofencesAdapterListener listener) {
    this.listener = listener;
  }

  // endregion

  // Constructors

  public AllGeofencesAdapter(List<NamedGeofence> namedGeofences) {
    this.namedGeofences = namedGeofences;
  }

  // endregion

  // region Overrides

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_geofence, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    final NamedGeofence geofence = namedGeofences.get(position);

    holder.name.setText(geofence.name);
    holder.latitide.setText(String.valueOf(geofence.latitude) + holder.latitide.getResources().getString(R.string.Units_Degrees));
    holder.longitude.setText(String.valueOf(geofence.longitude) + holder.longitude.getResources().getString(R.string.Units_Degrees));
    holder.radius.setText(String.valueOf(geofence.radius / 1000.0) + " " + holder.radius.getResources().getString(R.string.Units_Kilometers));



  }

  @Override
  public int getItemCount() {
    return namedGeofences.size();
  }

  // endregion

  // region Interfaces

  public interface AllGeofencesAdapterListener {
    void onDeleteTapped(NamedGeofence namedGeofence);
  }

  // endregion

  // region Inner classes

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView name;
    TextView latitide;
    TextView longitude;
    TextView radius;


    public ViewHolder(ViewGroup v) {
      super(v);

      name = (TextView) v.findViewById(R.id.Listitem_geofenceName);
      latitide = (TextView) v.findViewById(R.id.listitem_geofenceLatitude);
      longitude = (TextView) v.findViewById(R.id.listitem_geofenceLongitude);
      radius = (TextView) v.findViewById(R.id.listitem_geofenceRadius);

    }
  }

  // endregion
}
