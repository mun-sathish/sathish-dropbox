package in.sathish.dropbox.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.List;

import in.sathish.dropbox.R;
import in.sathish.dropbox.dto.BrowseContentDTO;

/**
 * Created by Sathish Mun on 23/9/15.
 */
public class BrowseContentAdapter extends UltimateViewAdapter {
    Activity activity;
    private List<BrowseContentDTO> allRowItemList;
    BrowseContentDTO singleRowItemList;

    //constructor
    public BrowseContentAdapter(Activity activity, List<BrowseContentDTO> allRowItemList,
                                onBrowseAdapterListener browseAdapterListener) {
        this.activity = activity;
        this.allRowItemList = allRowItemList;
        this.browseAdapterListener = browseAdapterListener;
    }

    @Override
    public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_browse_content, parent, false);
        return new ViewHolder(v);
    }

    //returns the count of the list which is also equal to count of content in the view
    @Override
    public int getAdapterItemCount() {
        return allRowItemList.size();
    }

    @Override
    public long generateHeaderId(int position) {
        if (position == 0) {
            return position;
        } else {
            return -1;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        singleRowItemList = allRowItemList.get(position);    // getting the current view single row content to be displayed
        //sets the elements of the single element
        ((ViewHolder) viewHolder).fileName.setText(singleRowItemList.getFileName());
        ((ViewHolder) viewHolder).fileSize.setText(singleRowItemList.getFileSize());
        ((ViewHolder) viewHolder).fileModified.setText(singleRowItemList.getFileModified());
        ((ViewHolder) viewHolder).fileImage.setImageResource(singleRowItemList.getFileImage());

    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_browse_content, parent, false);
        return new RecyclerView.ViewHolder(v) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

    }

    //setting up the ViewHolder Class
    class ViewHolder extends UltimateRecyclerviewViewHolder implements View.OnClickListener {
        TextView fileName, fileSize, fileModified;
        LinearLayout layout;
        ImageView fileImage;

        public ViewHolder(View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.fileName);
            fileSize = (TextView) itemView.findViewById(R.id.fileSize);
            fileModified = (TextView) itemView.findViewById(R.id.fileModified);
            layout = (LinearLayout) itemView.findViewById(R.id.rowLayout);
            fileImage = (ImageView) itemView.findViewById(R.id.fileImage);
            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //ToastMakerUtil.display(activity, "TEST: "+ getPosition(),Toast.LENGTH_SHORT);
            try {
                switch (v.getId()) {
                    case R.id.rowLayout:   //when a content is clicked in the recyclerView
                        singleRowItemList = allRowItemList.get(getLayoutPosition()); //getPosition()
                        //if is a folde, then only it should further display contents, else do nothing
                        if (singleRowItemList.getIsFolder())
                            browseAdapterListener.onClickFolder(singleRowItemList.getFilePath(), getLayoutPosition());
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //setting up listener for tracking the folder been clicked and for further processes
    onBrowseAdapterListener browseAdapterListener;
    public interface onBrowseAdapterListener {
        public void onClickFolder(String pathName, int position);
    }
}
