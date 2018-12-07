package mcc_2018_g15.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private DatabaseReference usersRef;
    private EditText editTextUsername;
    private ImageView imageViewAvatar;
    private ProgressDialog progressDialog;
    private Uri updatedAvatarUri;
    private String orgAvatarUri;
    private String orgUsername;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userId = FirebaseAuth.getInstance().getUid();
        editTextUsername = findViewById(R.id.editTextUsername);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        final Button buttonSave = findViewById(R.id.buttonSave);
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        progressDialog = new ProgressDialog(this);

        getProfileData();
        imageViewAvatar.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                chooseImage();
            }
        });
        buttonSave.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                updateUser();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        InputStream inputStream;
        if (requestCode == PICK_IMAGE) {
            updatedAvatarUri = data.getData();
            if (updatedAvatarUri != null) {
                try {
                    inputStream = getContentResolver().openInputStream(updatedAvatarUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    Bitmap compressedBitmap = resizeBitmap(bitmap, 320);
                    imageViewAvatar.setImageBitmap(compressedBitmap);
                } catch (IOException e) {

                }
            }
        }

    }

    private void getProfileData() {
        if (userId == null) {
            return;
        }
        progressDialog.setMessage("Loading");
        progressDialog.show();
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                orgUsername = snapshot.child("name").getValue().toString();
                editTextUsername.setText(orgUsername);
                editTextUsername.setSelection(editTextUsername.getText().length());

                orgAvatarUri = snapshot.child("avatar").getValue().toString();
                StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(orgAvatarUri);

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

    private Bitmap resizeBitmap(Bitmap mBitMap, int maxSize) {
        int width = mBitMap.getWidth();
        int height = mBitMap.getHeight();
        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(mBitMap, width, height, true);
    }

    private void updatedUserNameAndPhoto(final String username, final Uri imageUri) {
        if (updatedAvatarUri != null) {
            final StorageReference storageReference =
                    FirebaseStorage.getInstance()
                            .getReference("users")
                            .child(userId)
                            .child("avatar");

            UploadTask uploadTask = storageReference.putFile(imageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL

                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        User newUser = new User(username, orgAvatarUri, "full", "dark");
                        usersRef.child(userId).setValue(newUser);
                        startActivity(new Intent(ProfileActivity.this, DialogsActivity.class));
                        finish();

                    }
                    progressDialog.dismiss();
                }
            });
        }
        else {
            User newUser = new User(username, orgAvatarUri, "full", "dark");
            usersRef.child(userId).setValue(newUser);
            progressDialog.dismiss();
            startActivity(new Intent(ProfileActivity.this, DialogsActivity.class));
            finish();
        }
    }

    private void updateUser() {
        final String newUsername = editTextUsername.getText().toString().trim();

        if(TextUtils.isEmpty(newUsername)){
            Toast.makeText(this,"Please enter username",Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Updating");
        progressDialog.show();

        if (updatedAvatarUri == null && newUsername.equals(orgUsername)) {
            progressDialog.dismiss();
            startActivity(new Intent(ProfileActivity.this, DialogsActivity.class));
            finish();
            return;
        }

        usersRef.orderByChild("name").equalTo(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !newUsername.equals(orgUsername)) {
                    Toast.makeText(ProfileActivity.this,"Username doesn't exist"
                            ,Toast.LENGTH_SHORT).show();

                } else {
                    updatedUserNameAndPhoto(newUsername, updatedAvatarUri);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

    }
}
