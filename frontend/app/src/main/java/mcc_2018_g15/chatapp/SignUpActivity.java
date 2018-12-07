package mcc_2018_g15.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {



    //defining view objects
    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextUsername;

    private ProgressDialog progressDialog;


    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

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

    }

    private void addUserToDatabase(String username, String imgUrl) {
        User newUser = new User(username, imgUrl, "full", "dark");
        usersRef.child(firebaseAuth.getUid()).setValue(newUser);
    }

    private void registerUser(){

        //getting email and password from edit texts
        final String email = editTextEmail.getText().toString().trim();
        final String password  = editTextPassword.getText().toString().trim();
        final String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();

        Log.d("firebasecheck", "reg user");
        if (!validateInput(email, password, confirmPassword, username)) {
            return;
        }

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        usersRef.orderByChild("name").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // use "username" already exists

                    Log.d("firebasecheck", "if");
                    Toast.makeText(SignUpActivity.this,"Username doesn't exist"
                            ,Toast.LENGTH_SHORT).show();

                    // Let the user know he needs to pick another username.
                } else {
                    // User does not exist. NOW call createUserWithEmailAndPassword
                    Log.d("firebasecheck", "else");
                    Log.d("firebasecheck", dataSnapshot.getValue().toString());
                    createUser(email, password);
                    // Your previous code here.

                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void createUser(String email, String password) {
        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if(task.isSuccessful()){
                            String username = editTextUsername.getText().toString().trim();
                            addUserToDatabase(username, "blablabla");
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
