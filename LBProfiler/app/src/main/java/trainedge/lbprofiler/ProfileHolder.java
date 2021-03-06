package trainedge.lbprofiler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;



public class ProfileHolder extends RecyclerView.ViewHolder {

    View rlContainer;
    TextView tvProfileView;
    TextView tvProfileState;
    TextView tvProfileName;

    public ProfileHolder(View itemView) {
        super(itemView);
        tvProfileName = (TextView) itemView.findViewById(R.id.tvProfileName);
        tvProfileState = (TextView) itemView.findViewById(R.id.tvState);
        tvProfileView = (TextView) itemView.findViewById(R.id.tvView);
        rlContainer =  itemView.findViewById(R.id.rlContainer);
    }
}
