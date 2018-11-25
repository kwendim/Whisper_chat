package assignments.mmoneer71.mcc_project_g15;

    import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.GridView;

    import com.facebook.drawee.backends.pipeline.Fresco;
    import com.facebook.drawee.view.SimpleDraweeView;
    import com.facebook.imagepipeline.core.ImagePipelineConfig;
    import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
    import com.stfalcon.frescoimageviewer.ImageViewer;



public class GalleryActivity extends AppCompatActivity {
    String samp_img_url= "https://firebasestorage.googleapis.com/v0/b/mccchattest.appspot.com/o/chats%2F-LSBmE7sqAbavCu0-D87%2Fimage%3A156257?alt=media&token=cde92e6b-d6e9-49d9-bb46-6ca98253c21d";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(GalleryActivity.this,config);
        Log.d("fresco", "initialized");
        Fresco.initialize(this,config);

        setContentView(R.layout.activity_gallery);
        String[] imageurl = new String[]{samp_img_url,LOADING_IMAGE_URL,samp_img_url,samp_img_url,LOADING_IMAGE_URL,samp_img_url,samp_img_url,samp_img_url,LOADING_IMAGE_URL,samp_img_url};


        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(new GalleryGridAdapter(GalleryActivity.this,imageurl));






    }
}
