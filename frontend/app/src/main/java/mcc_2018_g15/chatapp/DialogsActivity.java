package mcc_2018_g15.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;
import java.util.Calendar;

public class DialogsActivity extends AppCompatActivity{

    private static final String TAG = "MessageActivity";
    private static final String USER_ID = "user_id";
    DatabaseReference myRef;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);

        DialogsList dialogsListView = (DialogsList)findViewById(R.id.dialogsList);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("users").child(USER_ID).child("user_chats");

        final DialogsListAdapter dialogsListAdapter = new DialogsListAdapter<>(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                try {
                    Picasso.get().load(url).into(imageView);
                }catch(Exception e){

                }
            }
        });

        dialogsListView.setAdapter(dialogsListAdapter);

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String prevChildKey) {

                // users=Author() from users/user_id, lastMessage=chats/chat_id/last_message, unreadCount=1||messages where date later then opened
                // TODO: 11/22/2018 possible add last opened time

//                Toast.makeText(DialogsActivity.this, "New child added: "+ dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Child added: " );

                final Calendar calendar = Calendar.getInstance();

//                final Message msg = new Message("id", getAuthor(), "Sample message", calendar.getTime());

//                Message last_message = dataSnapshot.child("user").getValue(Message.class);
//                new_dialog.setLastMessage(last_message);
//                new_dialog.setDialogName("DialogName");
//                Log.d("everything: " , new_dialog.toString());

                // TODO: 11/24/2018 Order by date
                DatabaseReference chatsRef = database.getReference().child("chats").child(dataSnapshot.getKey());
                Dialog new_dialog = new Dialog(dataSnapshot.getKey(), "","",
                                new ArrayList<Author>(), new Message("id", new Author("","","http://i.imgur.com/mRqh5w1.png"), "", calendar.getTime()), 0);
                dialogsListAdapter.addItem(0,new_dialog);
                chatsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot chatsDataSnapshot) {
                        final Dialog new_dialog = new Dialog();

                        new_dialog.setId(chatsDataSnapshot.getKey());
                        new_dialog.setDialogPhoto(chatsDataSnapshot.child("dialogPhoto").getValue(String.class));
                        new_dialog.setDialogName(chatsDataSnapshot.child("dialogName").getValue(String.class));

                        new_dialog.setUsers(new ArrayList<Author>());

                        ArrayList<String> keyList = new ArrayList<>();
                        final ArrayList<String> usernames = new ArrayList<String>();
                        final ArrayList<Author> authorsList = new ArrayList<Author>();
                        final int[] index = new int[] {0};
                        for (DataSnapshot child : chatsDataSnapshot.child("users").getChildren()) {
                            Log.e("!_@@_Key::>", child.getKey());
                            keyList.add( child.getValue().toString());
                            DatabaseReference usersRef = database.getReference().child("users").child(child.getKey());

                            usersRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot usersDataSnapshot) {
//                                    if(usernames.size()>index[0]) {
//                                        usernames.remove(index[0]);
//                                        usernames.add(index[0], usersDataSnapshot.child("username").getValue().toString());
//                                    }else{
                                        usernames.add(index[0], usersDataSnapshot.child("username").getValue().toString());
                                        authorsList.add(new Author(usersDataSnapshot.getKey(), usersDataSnapshot.child("name").getValue().toString(), usersDataSnapshot.child("avatar").getValue().toString()));
//                                        index[0]++;
//                                    }

                                    IDialog dialog = dialogsListAdapter.getItemById(chatsDataSnapshot.getKey());
                                    Message lastMessage = (Message)dialog.getLastMessage();
//                                    Log.e("!_@@_LastMessageKEY::>", lastMessage.getUser().getId());
//                                    Log.e("!_@@_UsersDataKEY::>", usersDataSnapshot.getKey());
                                    if(lastMessage.getUser().getId()==usersDataSnapshot.getKey()){
                                        lastMessage.setImage(new Message.Image(usersDataSnapshot.child("avatar").getValue().toString()));
                                    }
//                                    Log.e("!_@@_LastMessageURL::>", lastMessage.getUser().getAvatar());
//                                    Message msg = new Message("id", new Author("user_id","","http://i.imgur.com/mRqh5w1.png"), dialog.getLastMessage().getText(), calendar.getTime());
                                    Dialog dlg = new Dialog(dialog.getId(), dialog.getDialogPhoto(), TextUtils.join(", ", usernames), authorsList, lastMessage, dialog.getUnreadCount());
                                    dialogsListAdapter.updateItemById(dlg);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }


//                        new_dialog.setDialogName(TextUtils.join(", ", keyList));

                        final String last_message=(chatsDataSnapshot.child("last_message").getValue(String.class));
                        // TODO: 11/27/2018 update data below with data from last message object
                        Message msg = new Message("id", new Author("user_id","","http://i.imgur.com/mRqh5w1.png"), last_message, calendar.getTime());
                        new_dialog.setLastMessage(msg);
                        dialogsListAdapter.updateItemById(new_dialog);
                        dialogsListAdapter.updateDialogWithMessage(dataSnapshot.getKey(), msg);

//                        IDialog dialog = dialogsListAdapter.getItemById(chatsDataSnapshot.getKey());
//                        new_dialog.setUnreadCount(dialog.getUnreadCount()+1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
//                Message msg = new Message("id", getAuthor(), "Sample message", calendar.getTime());
//                Dialog new_dialog = new Dialog(dataSnapshot.getKey(), "http://i.imgur.com/pv1tBmT.png","Chat "+dataSnapshot.getKey()+" name",
//                        new ArrayList<Author>(), msg, 2);
//                dialogsListAdapter.addItem(0, new_dialog);
//                chatsRef.addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(DataSnapshot chatsDataSnapshot, String prevChildKey) {
//                        Dialog new_dialog = chatsDataSnapshot.getValue(Dialog.class);
//                        new_dialog.setId(chatsDataSnapshot.getKey());
//                        String last_message=(chatsDataSnapshot.child("last_message").getValue(String.class));
//                        Message msg = new Message("id", getAuthor(), last_message, calendar.getTime());
//                        new_dialog.setLastMessage(msg);
//                        new_dialog.setUsers(new ArrayList<Author>());
////                        new_dialog = new Dialog(dataSnapshot.getKey(), "http://i.imgur.com/pv1tBmT.png","Chat "+dataSnapshot.getKey()+" name",
////                                new ArrayList<Author>(), msg, 2);
////                        if(new_dialog.getId()==dataSnapshot.getKey())
//                            dialogsListAdapter.addItem(0,new_dialog);
//                    }
//
//                    @Override
//                    public void onChildChanged(DataSnapshot chatsDataSnapshot, String prevChildKey) {
//                        Dialog new_dialog = chatsDataSnapshot.getValue(Dialog.class);
//                        String last_message=(chatsDataSnapshot.child("last_message").getValue(String.class));
//                        Message msg = new Message("id", getAuthor(), last_message, calendar.getTime());
//                        new_dialog.setLastMessage(msg);
//                        new_dialog.setUsers(new ArrayList<Author>());
//
//                        dialogsListAdapter.updateItemById(new_dialog);
//                    }
//
//                    @Override
//                    public void onChildRemoved(DataSnapshot dataSnapshot) {}
//
//                    @Override
//                    public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {}
//
//                });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
//                Toast.makeText(DialogsActivity.this, "Child edite", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "Child edited");
////                Dialog new_dialog = dataSnapshot.getValue(Dialog.class);
//                Calendar calendar = Calendar.getInstance();
//                calendar.add(Calendar.DAY_OF_MONTH, -(0));
//                calendar.add(Calendar.MINUTE, -(0));
//                Dialog new_dialog = getDialog(0, calendar.getTime());
////                Message last_message = dataSnapshot.child("user").getValue(Message.class);
////                new_dialog.setLastMessage(last_message);
////                new_dialog.setDialogName("DialogName");
////                Log.d("everything: " , new_dialog.toString());
//                dialogsListAdapter.addItem(0, new_dialog);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });

        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<Dialog>() {
            @Override
            public void onDialogClick(Dialog dialog) {
//                Toast.makeText(DialogsActivity.this, dialog.getId(), Toast.LENGTH_SHORT).show();
                Intent messageIntent = new Intent(DialogsActivity.this, MessageActivity.class);
                messageIntent.putExtra("chatId", dialog.getId()); //Optional parameters
                startActivity(messageIntent);
            }
        });
        //dialogsListAdapter.setItems(getDialogs());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent searchIntent = new Intent(DialogsActivity.this, SearchUsersActivity.class);
                startActivity(searchIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }





//    public static ArrayList<Dialog> getDialogs() {
//        ArrayList<Dialog> chats = new ArrayList<>();
//
//        for (int i = 0; i < 20; i++) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.DAY_OF_MONTH, -(i * i));
//            calendar.add(Calendar.MINUTE, -(i * i));
//
//            chats.add(getDialog(i, calendar.getTime()));
//        }
//
//        return chats;
//    }
//
//    private static Dialog getDialog(int i, Date lastMessageCreatedAt) {
//        ArrayList<Author> users = getAuthors();
//        return new Dialog(
//                getRandomId(),
//                users.size() > 1 ? groupChatImages.get(users.size() - 2) : getRandomAvatar(),
//                users.size() > 1 ? groupChatTitles.get(users.size() - 2) : users.get(0).getName(),
//                users,
//                getMessage(lastMessageCreatedAt),
//                i < 3 ? 3 - i : 0);
//    }
//
//    private static ArrayList<Author> getAuthors() {
//        ArrayList<Author> users = new ArrayList<>();
//        int usersCount = 1 + rnd.nextInt(4);
//
//        for (int i = 0; i < usersCount; i++) {
//            users.add(getAuthor());
//        }
//
//        return users;
//    }
//
//    private static Author getAuthor() {
//        return new Author(
//                getRandomId(),
//                getRandomName(),
//                getRandomAvatar(),
//                getRandomBoolean());
//    }
//
//    private static Message getMessage(final Date date) {
//        return new Message(
//                getRandomId(),
//                getAuthor(),
//                getRandomMessage(),
//                date);
//    }
//
//    static SecureRandom rnd = new SecureRandom();
//
//    static ArrayList<String> avatars = new ArrayList<String>() {
//        {
//            add("http://i.imgur.com/pv1tBmT.png");
//            add("http://i.imgur.com/R3Jm1CL.png");
//            add("http://i.imgur.com/ROz4Jgh.png");
//            add("http://i.imgur.com/Qn9UesZ.png");
//        }
//    };
//
//    static final ArrayList<String> groupChatImages = new ArrayList<String>() {
//        {
//            add("http://i.imgur.com/hRShCT3.png");
//            add("http://i.imgur.com/zgTUcL3.png");
//            add("http://i.imgur.com/mRqh5w1.png");
//        }
//    };
//
//    static final ArrayList<String> groupChatTitles = new ArrayList<String>() {
//        {
//            add("Samuel, Michelle");
//            add("Jordan, Jordan, Zoe");
//            add("Julia, Angel, Kyle, Jordan");
//        }
//    };
//
//    static final ArrayList<String> names = new ArrayList<String>() {
//        {
//            add("Samuel Reynolds");
//            add("Kyle Hardman");
//            add("Zoe Milton");
//            add("Angel Ogden");
//            add("Zoe Milton");
//            add("Angelina Mackenzie");
//            add("Kyle Oswald");
//            add("Abigail Stevenson");
//            add("Julia Goldman");
//            add("Jordan Gill");
//            add("Michelle Macey");
//        }
//    };
//
//    static final ArrayList<String> messages = new ArrayList<String>() {
//        {
//            add("Hello!");
//            add("This is my phone number - +1 (234) 567-89-01");
//            add("Here is my e-mail - myemail@example.com");
//            add("Hey! Check out this awesome link! www.github.com");
//            add("Hello! No problem. I can today at 2 pm. And after we can go to the office.");
//            add("At first, for some time, I was not able to answer him one word");
//            add("At length one of them called out in a clear, polite, smooth dialect, not unlike in sound to the Italian");
//            add("By the bye, Bob, said Hopkins");
//            add("He made his passenger captain of one, with four of the men; and himself, his mate, and five more, went in the other; and they contrived their business very well, for they came up to the ship about midnight.");
//            add("So saying he unbuckled his baldric with the bugle");
//            add("Just then her head struck against the roof of the hall: in fact she was now more than nine feet high, and she at once took up the little golden key and hurried off to the garden door.");
//        }
//    };
//
//    static final ArrayList<String> images = new ArrayList<String>() {
//        {
//            add("https://habrastorage.org/getpro/habr/post_images/e4b/067/b17/e4b067b17a3e414083f7420351db272b.jpg");
//            add("https://cdn.pixabay.com/photo/2017/12/25/17/48/waters-3038803_1280.jpg");
//        }
//    };
//
//    static String getRandomId() {
//        return Long.toString(UUID.randomUUID().getLeastSignificantBits());
//    }
//
//    static String getRandomAvatar() {
//        return avatars.get(rnd.nextInt(avatars.size()));
//    }
//
//    static String getRandomGroupChatImage() {
//        return groupChatImages.get(rnd.nextInt(groupChatImages.size()));
//    }
//
//    static String getRandomGroupChatTitle() {
//        return groupChatTitles.get(rnd.nextInt(groupChatTitles.size()));
//    }
//
//    static String getRandomName() {
//        return names.get(rnd.nextInt(names.size()));
//    }
//
//    static String getRandomMessage() {
//        return messages.get(rnd.nextInt(messages.size()));
//    }
//
//    static String getRandomImage() {
//        return images.get(rnd.nextInt(images.size()));
//    }
//
//    static boolean getRandomBoolean() {
//        return rnd.nextBoolean();
//    }
}
