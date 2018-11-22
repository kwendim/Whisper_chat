package assignments.mmoneer71.mcc_project_g15;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.net.URL;


public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    private static final String CHAT_ID = "chat_id";
    private static final String USER_ID = "user_id_2";
    private static int REQUEST_IMAGE = 1;
    private static int REQUEST_IMAGE_CAPTURE = 2;
    final Author kidus = new Author(USER_ID,"kidus","meavatar");
      DatabaseReference myRef;
     FirebaseDatabase database;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE || requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());

                    Message tempMessage = new Message(USER_ID, kidus, null);
                    Message.Image new_img = new Message.Image(LOADING_IMAGE_URL);
                    tempMessage.setImage(new_img);

                    myRef.push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        String key = databaseReference.getKey();
                                        StorageReference storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference("user_id_2")
                                                        .child(key)
                                                        .child(uri.getLastPathSegment());
                                        Log.d("putImage", "about to be called: " + key );

                                        putImageInStorage(storageReference, uri, key);
                                    } else {
                                        Log.w(TAG, "Unable to write message to database.",
                                                databaseError.toException());
                                    }
                                }
                            });
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_interface);

         database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("chat_msgs").child(CHAT_ID);

        final Author zee = new Author("user_id_reply","Zee","zee's Avatar");


        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Picasso.get().load(url).into(imageView);

            }

        };


        MessageInput inputView = (MessageInput) findViewById(R.id.input);
        final MessagesListAdapter<Message> adapter = new MessagesListAdapter<>(kidus.getId(), imageLoader);

        MessagesList messagesList = findViewById(R.id.messagesList);
        messagesList.setAdapter(adapter);

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
                Log.d("onchildchanged","hasbeencalled");
                Message new_message = dataSnapshot.getValue(Message.class);
                Author new_author = dataSnapshot.child("user").getValue(Author.class);
                new_message.setUser(new_author);
                Message.Image new_image = dataSnapshot.child("imageurl").getValue(Message.Image.class);
                new_message.setImage(new_image);
                Log.d("boutotloadimage: " , "right here + " + prevChildKey);
                adapter.update(new_message);            }

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
                                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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

//    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
//        storageReference.putFile(uri).addOnCompleteListener(MessageActivity.this,
//                new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            Message.Image up_img= new Message.Image(task.getResult().getMetadata().getReference().getDownloadUrl().getResult().toString());
//                            Log.d("imageurl", task.getResult().getMetadata().getReference().getDownloadUrl().getResult().toString());
//                            Message friendlyMessage =
//                                    new Message(USER_ID, kidus, null);
//                            friendlyMessage.setImage(up_img);
//                            myRef.child(key)
//                                    .setValue(friendlyMessage);

//                        } else {
//                            Log.w(TAG, "Image upload task was not successful.",
//                                    task.getException());
//                        }
//                    }
//                });
//    }
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
                Uri downloadUri = task.getResult();
                Message.Image up_img= new Message.Image(downloadUri.toString());
                Message friendlyMessage =
                        new Message(USER_ID, kidus, null);
                friendlyMessage.setImage(up_img);
                myRef.child(key)
                        .setValue(friendlyMessage);
                Log.d("downloadURI?" , downloadUri.toString());
            } else {
                // Handle failures
                // ...
            }
        }


            });
}

}
