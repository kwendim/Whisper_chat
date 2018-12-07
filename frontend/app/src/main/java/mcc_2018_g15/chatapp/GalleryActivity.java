package mcc_2018_g15.chatapp;

    import android.content.Intent;
    import android.content.res.Configuration;
    import android.os.Environment;
    import android.support.annotation.NonNull;
    import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
    import android.support.v7.widget.LinearLayoutManager;
    import android.support.v7.widget.RecyclerView;
    import android.support.v7.widget.Toolbar;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.GridView;
    import android.widget.Toast;

    import com.facebook.cache.disk.DiskCacheConfig;
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
    import java.util.Collections;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.LinkedHashMap;
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
    private static int SORTING_OPTION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_recycler);



        Toolbar toolbar = (Toolbar) findViewById(R.id.gallery_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Main Page");
        }
        toolbar.inflateMenu(R.menu.menu_gallery);
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this).setMaxCacheSize(10000).setBaseDirectoryName("cache")
                .setBaseDirectoryPath(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
                .build();

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this).setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .setDiskCacheEnabled(true)
                .setMainDiskCacheConfig(diskCacheConfig)
                .build();

        Fresco.initialize(this,config);


        Log.d("fresco", "initialized");

        setUpRecyclerView();
        getAllImages_date();
        SORTING_OPTION =1;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_date) {
            if (SORTING_OPTION == 1){
                Toast.makeText(GalleryActivity.this, "Already sorted by date", Toast.LENGTH_SHORT).show();
            }
            else{
                getAllImages_date();
                SORTING_OPTION = 1;
            }
            return true;
        } else if (id == R.id.menu_user) {
            if (SORTING_OPTION == 2){
                Toast.makeText(GalleryActivity.this, "Already sorted by user", Toast.LENGTH_SHORT).show();
            }
            else{
                getAllImages_user();
                SORTING_OPTION = 2;
            }
            return true;
        } else if (id == R.id.menu_label){
            if (SORTING_OPTION == 3){
                Toast.makeText(GalleryActivity.this, "Already sorted by label", Toast.LENGTH_SHORT).show();
            }
            else{
                getAllImages_label();
                SORTING_OPTION=3;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void setUpRecyclerView() {
        galleryRecycler = (RecyclerView) findViewById(R.id.recycler);
        galleryRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        galleryRecycler.setLayoutManager(linearLayoutManager);
    }

    //populate recycler view
    private void populateRecyclerView(Map<String, ArrayList<String>> sorted_data) {
        Log.d("populaterecycler", "Inside populate");
        ArrayList<SectionModel> sections = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : sorted_data.entrySet()) {
            Log.d("populaterecycler", entry.getKey());
            sections.add(new SectionModel(entry.getKey(),entry.getValue()));
        }
        Log.d("populaterecycler", "Sections:- " + sections.toString());
        SectionedGalleryRecyclerAdapter adapter = new SectionedGalleryRecyclerAdapter(GalleryActivity.this, sections);
        galleryRecycler.setAdapter(adapter);
    }


    private void getAllImages_date(){
        DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference("image_urls").child(CHAT_ID);
        final Map<String,ArrayList<String>> image_sorter = new LinkedHashMap<>();


        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.e("Count " ,""+snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        if(postSnapshot.hasChild("createdAt")) {
                            String image_date = new SimpleDateFormat("MMMM dd,yyyy").format(postSnapshot.child("createdAt").getValue(Date.class));
                            String image_path = postSnapshot.child("url").getValue(String.class);
                            if (image_sorter.get(image_date) == null) {
                                ArrayList<String> new_element = new ArrayList<>();
                                new_element.add(image_path);
                                image_sorter.put(image_date, new_element);
                            } else {
                                image_sorter.get(image_date).add(image_path);
                            }

                            Log.d("haschild", image_sorter.toString());
                        }
                }

                Map<String,ArrayList<String>> ordered_data = new LinkedHashMap<>();
                List<String> keyList = new ArrayList<String>(image_sorter.keySet());
                List<ArrayList<String>> images_list = new ArrayList<>(image_sorter.values());
                for(int i=image_sorter.size()-1; i>=0; i--){
                    Collections.reverse(images_list.get(i));
                    ordered_data.put(keyList.get(i),images_list.get(i));
                }
                Log.d("ordered data", ordered_data.toString());
                populateRecyclerView(ordered_data);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());

            }


        });
    }

    private void getAllImages_user() {//TODO get user name from users table
        DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference("image_urls").child(CHAT_ID);
        final Map<String,ArrayList<String>> image_sorter = new HashMap<>();


        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.e("Count " ,""+snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    if(postSnapshot.hasChild("id")) {
                        String user_id = postSnapshot.child("id").getValue(String.class);
                        String image_path = postSnapshot.child("url").getValue(String.class);
                        if (image_sorter.get(user_id) == null) {
                            ArrayList<String> new_element = new ArrayList<>();
                            new_element.add(image_path);
                            image_sorter.put(user_id, new_element);
                        } else {
                            image_sorter.get(user_id).add(image_path);
                        }

                        Log.d("haschild", image_sorter.toString());
                    }
                }
                populateRecyclerView(image_sorter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());

            }
        });
    }

    private void getAllImages_label() {
        DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference("image_urls").child(CHAT_ID);
        final Map<String,ArrayList<String>> image_sorter = new HashMap<>();


        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.e("Count " ,"" + snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        String label = postSnapshot.child("label").getValue(String.class);
                        String image_path = postSnapshot.child("url").getValue(String.class);
                        if(image_sorter.get(label)==null){
                            ArrayList<String> new_element = new ArrayList<>();
                            new_element.add(image_path);
                            image_sorter.put(label,new_element);
                        }
                        else {
                            image_sorter.get(label).add(image_path);
                        }

                        Log.d("haschild", image_sorter.toString());
                }

                if(image_sorter.get("others")!=null) {
                    ArrayList<String> others = image_sorter.get("others");
                    image_sorter.remove("others");
                    image_sorter.put("others",others);
                }

                populateRecyclerView(image_sorter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());

            }
        });
    }
}
