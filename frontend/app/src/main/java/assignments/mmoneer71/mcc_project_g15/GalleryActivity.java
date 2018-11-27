package assignments.mmoneer71.mcc_project_g15;

    import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
    import android.support.v7.widget.LinearLayoutManager;
    import android.support.v7.widget.RecyclerView;
    import android.util.Log;
    import android.view.View;
    import android.widget.GridView;

    import com.facebook.drawee.backends.pipeline.Fresco;
    import com.facebook.drawee.view.SimpleDraweeView;
    import com.facebook.imagepipeline.core.ImagePipelineConfig;
    import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;
    import com.stfalcon.frescoimageviewer.ImageViewer;

    import java.util.ArrayList;
    import java.util.List;


public class GalleryActivity extends AppCompatActivity {
    String samp_img_url= "https://firebasestorage.googleapis.com/v0/b/mccchattest.appspot.com/o/chats%2F-LSBmE7sqAbavCu0-D87%2Fimage%3A156257?alt=media&token=cde92e6b-d6e9-49d9-bb46-6ca98253c21d";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    RecyclerView galleryRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_recycler);
//
//
//        RecyclerView galleryRecycler = (RecyclerView) findViewById(R.id.recycler);
//        String[] imageurl = new String[]{samp_img_url,LOADING_IMAGE_URL,samp_img_url,samp_img_url,LOADING_IMAGE_URL,samp_img_url,samp_img_url,samp_img_url,LOADING_IMAGE_URL,samp_img_url};
//
//        GalleryRecyclerViewAdapter adapter = new GalleryRecyclerViewAdapter(new String[]{"hello"});
//        galleryRecycler.setAdapter(adapter);
//
//        galleryRecycler.setLayoutManager(new LinearLayoutManager(this));
//


        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(GalleryActivity.this,config);
        Log.d("fresco", "initialized");
//
//        setContentView(R.layout.activity_gallery);
//        String[] imageurl = new String[]{samp_img_url,LOADING_IMAGE_URL,samp_img_url,samp_img_url,LOADING_IMAGE_URL,samp_img_url,samp_img_url,samp_img_url,LOADING_IMAGE_URL,samp_img_url};
//
//
//        GridView gridView = (GridView) findViewById(R.id.gridView);
//        gridView.setAdapter(new GalleryGridAdapter(GalleryActivity.this,imageurl));
    setUpRecyclerView();
    populateRecyclerView();





    }

    private void setUpRecyclerView() {
        galleryRecycler = (RecyclerView) findViewById(R.id.recycler);
        galleryRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        galleryRecycler.setLayoutManager(linearLayoutManager);
    }

    //populate recycler view
    private void populateRecyclerView() {
        ArrayList<SectionModel> sectionModelArrayList = new ArrayList<>();
        //for loop for sections

        StorageReference storage =  FirebaseStorage.getInstance().getReference("chats");
        Log.d("storage url ", String.valueOf(storage.getDownloadUrl()));
        for (int i = 1; i <= 5; i++) {
            ArrayList<String> itemArrayList = new ArrayList<>();
            //for loop for items
            for (int j = 1; j <= 10; j++) {
                itemArrayList.add(samp_img_url);
            }

            //add the section and items to array list
            sectionModelArrayList.add(new SectionModel("Section " + i, itemArrayList));
        }

        SectionedGalleryRecyclerAdapter adapter = new SectionedGalleryRecyclerAdapter(this, sectionModelArrayList);
        galleryRecycler.setAdapter(adapter);
    }
}
