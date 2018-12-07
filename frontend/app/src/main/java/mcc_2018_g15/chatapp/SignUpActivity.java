package mcc_2018_g15.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

public class SignUpActivity extends AppCompatActivity {

    //defining view objects
    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextUsername;
    private ImageView imageViewAvatar;
    private ProgressDialog progressDialog;
    public static final int PICK_IMAGE = 1;


    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;
    private Uri uploadedAvatarUri, orgAvatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        //initializing views

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextUsername = findViewById(R.id.editTextUsername);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);

        final TextView textViewSignin = findViewById(R.id.textViewSignin);
        final Button buttonSignup = findViewById(R.id.buttonSignup);


        progressDialog = new ProgressDialog(this);

        //attaching click listener
        buttonSignup.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                registerUser();
            }
        });

        textViewSignin.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

        imageViewAvatar.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                chooseImage();
            }
        });

    }

    private void addUser(String userId, String username, String imgUrl) {
        User newUser = new User(username, imgUrl, "full", "dark");
        usersRef.child(userId).setValue(newUser);
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
            orgAvatarUri = data.getData();
            if (orgAvatarUri != null) {
                try {
                    inputStream = getContentResolver().openInputStream(orgAvatarUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    Bitmap compressedBitmap = resizeBitmap(bitmap, 320);
                    imageViewAvatar.setImageBitmap(compressedBitmap);
                } catch (IOException e) {

                }
            }
        }

    }

    private void addUserWithPhoto(final Uri imageUri) {
        final String userId = firebaseAuth.getUid();
        final String username = editTextUsername.getText().toString().trim();
        final StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("avatar");

        UploadTask uploadTask = storageReference.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d("SignUpActivity", "Image uploaded successfully");
                addUser(userId, username, taskSnapshot.getMetadata().getPath());
            }
        });

    }


    private void registerUser(){

        //getting email and password from edit texts
        final String email = editTextEmail.getText().toString().trim();
        final String password  = editTextPassword.getText().toString().trim();
        final String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();


        if (!validateInput(email, password, confirmPassword, username)) {
            return;
        }

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        usersRef.orderByChild("name").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Toast.makeText(SignUpActivity.this,"Username doesn't exist"
                            ,Toast.LENGTH_SHORT).show();
                } else {
                    createUser(email, password);
                }
                progressDialog.dismiss();
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


    private void createUser(String email, String password) {
        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if(task.isSuccessful()){
                            if (orgAvatarUri != null) {
                                addUserWithPhoto(orgAvatarUri);
                            }
                            else {
                                String username = editTextUsername.getText().toString().trim();
                                String userId = firebaseAuth.getUid();
                                addUser(userId, username, "users/default/default.png");
                            }
                            startActivity(new Intent(SignUpActivity.this, DialogsActivity.class));
                            finish();
                        }else{
                            //display some message here
                            Toast.makeText(SignUpActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
    private boolean validateInput(String email, String password, String confirmPassword, String username) {
        //checking if email and passwords are empty
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this,"Please enter username",Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6 ) {
            Toast.makeText(this,"Password has be to at least six characters",Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this,"Please enter a valid email address",Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this,"Passwords do not match",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
