package trainedge.lbprofiler;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public class ProfileAdapter extends RecyclerView.Adapter<ProfileHolder> {

    public static final String PROFILE_NAME = "trainedge.lbprofiler.name";
    private final Context context;
    //we also need a constructor for this example
    List<ProfileModel> profileList;
    //alt insert to add constructor


    public ProfileAdapter(Context context, List<ProfileModel> profileList) {
        this.context = context;
        this.profileList = profileList;
    }

    @Override
    public ProfileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = ((LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.simple_profile_item, parent, false);
        return new ProfileHolder(row);
    }

    @Override
    public void onBindViewHolder(ProfileHolder holder, int position) {
        //databinding
        final ProfileModel model = profileList.get(position);
        holder.tvProfileState.setText(model.getState() ? "active" : "inactive");
        holder.tvProfileName.setText(model.getKey());
        holder.tvProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tvView:
                        Intent intent = new Intent(context, ViewProfileActivity.class);
                        intent.putExtra(PROFILE_NAME,model.getKey());
                        context.startActivity(intent);
                        break;
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }
}
