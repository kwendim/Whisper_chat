package mcc_2018_g15.chatapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.cache.common.WriterCallback;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;

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
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        Uri uri = Uri.parse(arrayList.get(position));
        DataSource<Boolean> inMemoryCache = imagePipeline.isInDiskCache(uri);
        Log.d("inMemoryCache", String.valueOf(inMemoryCache.getResult()));

//        try {
//            CacheKey cacheKey = new SimpleCacheKey(url);
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//            final byte[] byteArray = stream.toByteArray();
//            ImagePipelineFactory.getInstance().getMainDiskStorageCache().insert(cacheKey, new WriterCallback() {
//                @Override
//                public void write(OutputStream outputStream) throws IOException {
//                    outputStream.write(byteArray);
//                }
//            });
//        } catch (IOException cacheWriteException) {
//
//        }
//

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(arrayList.get(position)))
                .setResizeOptions(new ResizeOptions(90, 90))
                .build();
        holder.drawee.setController(
                Fresco.newDraweeControllerBuilder()
                        .setOldController(holder.drawee.getController())
                        .setImageRequest(request)
                        .build());

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

}
