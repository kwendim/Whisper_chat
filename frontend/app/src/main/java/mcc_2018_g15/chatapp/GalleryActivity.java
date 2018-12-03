package mcc_2018_g15.chatapp;

    import android.support.annotation.NonNull;
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
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;
    import com.stfalcon.frescoimageviewer.ImageViewer;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

//TODO: Orientation change closes the viewer. Back Button for view. Implement Download of images. (Maybe) Ability to navigate
// through all images when opening one.
//TODO: It takes a long time to load the images
public class GalleryActivity extends AppCompatActivity {
    String samp_img_url= "https://firebasestorage.googleapis.com/v0/b/mccchattest.appspot.com/o/chats%2F-LSAJSxZlW6W1Mw5Jd2y%2FIMG_20181127_205836?alt=media&token=1198961b-ac0d-4ec9-9029-3edb5f86dc77";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    RecyclerView galleryRecycler;
    private static final String CHAT_ID = "-LSAJSxZlW6W1Mw5Jd2y";


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
        getAllImages();





    }

    private void setUpRecyclerView() {
        galleryRecycler = (RecyclerView) findViewById(R.id.recycler);
        galleryRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        galleryRecycler.setLayoutManager(linearLayoutManager);
    }

    //populate recycler view
    private void populateRecyclerView(Map<String, ArrayList<String>> sorted_data) {
        ArrayList<SectionModel> sections = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : sorted_data.entrySet()) {
            sections.add(new SectionModel(entry.getKey(),entry.getValue()));
        }
        SectionedGalleryRecyclerAdapter adapter = new SectionedGalleryRecyclerAdapter(GalleryActivity.this, sections);
        galleryRecycler.setAdapter(adapter);
    }


//    private void getAllImages(){
//        DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference("chat_msgs").child(CHAT_ID);
//
//        imagesRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                Log.e("Count " ,""+snapshot.getChildrenCount());
//                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
//                    Log.d("imgges", postSnapshot.toString());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e("The read failed: " ,databaseError.getMessage());
//
//            }
//
//
//        });
//    }

    private void getAllImages(){
        DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference("chat_msgs").child(CHAT_ID);
        final Map<String,ArrayList<String>> date_sorter = new HashMap<>();


        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.e("Count " ,""+snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    if(postSnapshot.hasChild("imageurl")){
                        String image_date = new SimpleDateFormat("yyyyMMdd").format(postSnapshot.child("createdAt").getValue(Date.class));
                        String image_path = postSnapshot.child("imageurl").child("url").getValue(String.class);
                        if(date_sorter.get(image_date)==null){
                            ArrayList<String> new_element = new ArrayList<>();
                            new_element.add(image_path);
                            date_sorter.put(image_date,new_element);
                        }
                        else {
                            date_sorter.get(image_date).add(image_path);
                        }

                        Log.d("haschild", date_sorter.toString());
                    }
                }
                populateRecyclerView(date_sorter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());

            }


        });
    }
}
