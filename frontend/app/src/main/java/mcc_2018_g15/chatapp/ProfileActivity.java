package mcc_2018_g15.chatapp;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference usersRef;
    private EditText editTextUsername;
    private ImageView imageViewAvatar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        editTextUsername = findViewById(R.id.editTextUsername);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");

        getProfileData();
    }

    private void getProfileData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            return;
        }
        progressDialog.show();
        usersRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String username = snapshot.child("name").getValue().toString();
                editTextUsername.setText(username);
                editTextUsername.setSelection(editTextUsername.getText().length());

                String avatarUri = snapshot.child("avatar").getValue().toString();
                StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(avatarUri);

                httpsReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        Bitmap avatarBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageViewAvatar.setImageBitmap(Bitmap.createScaledBitmap(avatarBmp, imageViewAvatar.getWidth(),
                                imageViewAvatar.getHeight(), false));
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        progressDialog.dismiss();
                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
}
