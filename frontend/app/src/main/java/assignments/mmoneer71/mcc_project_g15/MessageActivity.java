package assignments.mmoneer71.mcc_project_g15;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    private static final String CHAT_ID = "-LSAJSxZlW6W1Mw5Jd2y";
    private static final String USER_ID = "user_id_2";
    private static int REQUEST_IMAGE = 1;
    private static int REQUEST_TAKE_PHOTO = 2;
    final Author kidus = new Author(USER_ID,"kidus","meavatar");
    DatabaseReference myRef;
    FirebaseDatabase database;
    //private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    private static final String LOADING_IMAGE_URL = "https://giphy.com/gifs/mashable-3oEjI6SIIHBdRxXI40";
    String mCurrentPhotoPath;
    Uri photoURI;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK ) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.getLastPathSegment().toString());
                    storeTemporaryImage(myRef,uri);

                }
        } else  if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
                final Uri uri = photoURI;
                Log.d(TAG, "Uri: " + uri.getLastPathSegment());
                storeTemporaryImage(myRef,uri);

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Main Page");
        }
        toolbar.setSubtitle("Test Subtitle");
        toolbar.inflateMenu(R.menu.menu_message);



        database = FirebaseDatabase.getInstance();
        //TODO: Set to custom chat_id or make .push for new chat
        myRef = database.getReference().child("chat_msgs").child(CHAT_ID);

        final Author zee = new Author("user_id_reply","Zee","zee's Avatar");


        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                if (url == LOADING_IMAGE_URL){
                    Glide.with(MessageActivity.this)
                            .load(url)
                            .into(imageView);

                }
                Glide.with(MessageActivity.this).load(url).into(imageView);


            }

        };


        MessageInput inputView = (MessageInput) findViewById(R.id.input);
        final MessagesListAdapter<Message> adapter = new MessagesListAdapter<>(kidus.getId(), imageLoader);

        MessagesList messagesList = findViewById(R.id.messagesList);
        messagesList.setAdapter(adapter);


//TODO: HANDLE NEW CHATS WITH THIS EVENT LISTENER
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Message new_message = dataSnapshot.getValue(Message.class);
                Author new_author = dataSnapshot.child("user").getValue(Author.class);
                new_message.setUser(new_author);
                Log.d("everything: " , new_message.print());
                adapter.addToStart(new_message, true);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                if (dataSnapshot.hasChildren()) {
                    Log.d("onchildchanged", "hasbeencalled");
                    Message new_message = dataSnapshot.getValue(Message.class);
                    Author new_author = dataSnapshot.child("user").getValue(Author.class);
                    new_message.setUser(new_author);
                    Message.Image new_image = dataSnapshot.child("imageurl").getValue(Message.Image.class);
                    new_message.setImage(new_image);
                    Log.d("boutotloadimage: ", "right here + " + prevChildKey);
                    adapter.update(new_message);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });




        inputView.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message

                Message message = new Message(USER_ID, kidus, input.toString() );
                Message rep_message = new Message("user_replier", zee, input.toString() + "'s reply");
                myRef.push().setValue(message);
                myRef.push().setValue(rep_message);

                return true;
            }
        });

        inputView.setAttachmentsListener(new MessageInput.AttachmentsListener() {
            @Override
            public void onAddAttachments() {

                new AlertDialog.Builder(MessageActivity.this)
                        .setItems(R.array.view_types_dialog, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0){
                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, REQUEST_IMAGE);
                                }
                                else {
                                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                    // Ensure that there's a camera activity to handle the intent
                                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                        // Create the File where the photo should go
                                        File photoFile = null;
                                        try {
                                            photoFile = createImageFile();
                                        } catch (IOException ex) {
                                            // Error occurred while creating the File

                                        }
                                        // Continue only if the File was successfully created
                                        if (photoFile != null) {
                                            photoURI = FileProvider.getUriForFile(MessageActivity.this,
                                                    "assignments.mmoneer71.mcc_project_g15.fileprovider",
                                                    photoFile);
                                            Log.d("photouri", photoURI.toString());
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                                        }
                                    }

                                }
                                Log.i("interface", String.valueOf(i));
                            }
                        })
                        .show();
                //select attachments

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_gallery) {
          //  logoutUser();
            Log.d("Menu_Item", "Gallery");
            Intent myIntent = new Intent(this, GalleryActivity.class);
            //myIntent.putExtra("key", value); //Optional parameters
            startActivity(myIntent);
            return true;
        } else if (id == R.id.menu_addMember) {
            //  logoutUser();
            Log.d("menu Item", "Add Member");
            return true;
        } else if (id == R.id.menu_leaveChat){
            Log.d("Menu_item", "leave chat");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
private void storeTemporaryImage(final DatabaseReference dbReference,final Uri uri){
    Message tempMessage = new Message(USER_ID, kidus, null);
    Message.Image new_img = new Message.Image(LOADING_IMAGE_URL);
    tempMessage.setImage(new_img);

    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    final String imageFileName = "IMG_" + timeStamp ;


    dbReference.push()
            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError,
                                       DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        String key = databaseReference.getKey();
                        StorageReference storageReference =
                                FirebaseStorage.getInstance()
                                        .getReference("chats")
                                        .child(CHAT_ID)
                                        .child(imageFileName);
                        Log.d("putImage", "about to be called: " + key );
                        putImageInStorage(storageReference, uri, key);
                    } else {
                        Log.w(TAG, "Unable to write message to database.",
                                databaseError.toException());
                    }
                }
            });
}

private void putImageInStorage(final StorageReference storageReference, Uri uri, final String key) {

    UploadTask uploadTask = storageReference.putFile(uri);
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
                final Uri downloadUri = task.getResult();
                Message.Image up_img= new Message.Image(downloadUri.toString());
                Message message_update =
                        new Message(USER_ID, kidus, null);
                message_update.setImage(up_img);
                myRef.child(key)
                        .setValue(message_update);

//                SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
//                Date now = new Date();
//                String fileName = formatter.format(now) + ".tar.gz";
                //TODO: Remove this part right here
                final DatabaseReference imageurls = FirebaseDatabase.getInstance().getReference("imageursl");
                imageurls.child(CHAT_ID).child(key).setValue(downloadUri.toString()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("imageurlFailure", e.toString());
                    }
                });

                Log.d("downloadURI?" , downloadUri.toString());
            } else {
                // Handle failures
                // ...
            }
        }


            });
}

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp ;
        Log.d("timestamp", timeStamp);
        Log.d("dateee", new Date().toString());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("storagedir", storageDir.toString() );
        try {
            File image = new File(storageDir.toString() + "/" + imageFileName + ".jpg");
            image.createNewFile();
            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        }catch (Exception e ){
            Log.e("fileException", e.toString());
            return null;
        }

//
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",storageDir        /* suffix */
//        );

        // Save a file: path for use with ACTION_VIEW intents

    }


}
