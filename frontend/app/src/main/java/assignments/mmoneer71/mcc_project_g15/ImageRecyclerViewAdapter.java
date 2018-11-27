package assignments.mmoneer71.mcc_project_g15;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ImageViewHolder> {


    class ImageViewHolder extends RecyclerView.ViewHolder {

        private SimpleDraweeView drawee;
        public ImageViewHolder(View itemView) {
            super(itemView);
            drawee = (SimpleDraweeView) itemView.findViewById(R.id.grid_image);
        }
    }

    private Context context;
    private ArrayList<String> arrayList;

    public ImageRecyclerViewAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_gallery, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, final int position) {

        holder.drawee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ImageViewer.Builder<>(context,arrayList).setStartPosition(position).show();
            }
        });
        holder.drawee.setImageURI(arrayList.get(position));

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

}
