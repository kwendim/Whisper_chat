package mcc_2018_g15.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchUsersActivity extends AppCompatActivity {

    private Button btnCreateGroup;
    private RecyclerView chatsList;
    private DatabaseReference databaseRef;
    private Query query;
    private boolean isGroup = false;
    private String USER_ID = "user_id";
    private ArrayList<String> usersChats = new ArrayList<>();
    private ArrayList<String> connectedPeople = new ArrayList<>();
    private ArrayList<String> groupMembers = new ArrayList<>();
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        USER_ID = firebaseAuth.getUid();

        Intent intent = getIntent();
        isGroup = intent.getBooleanExtra("isGroup", false);
        chatsList = (RecyclerView) findViewById(R.id.chats_list);
        chatsList.setHasFixedSize(true);
        chatsList.setLayoutManager(new LinearLayoutManager(this));


        databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = databaseRef.child("users");
        DatabaseReference usersChatsRef = usersRef.child(USER_ID).child("user_chats");
        final DatabaseReference chatsRef = databaseRef.child("chats");


        SearchView sv = (SearchView) findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("name").startAt(queryText).endAt(queryText + "\uf8ff").limitToLast(50);
                FirebaseRecyclerOptions<User> options =
                        new FirebaseRecyclerOptions.Builder<User>()
                                .setQuery(query, User.class)
                                .setLifecycleOwner(SearchUsersActivity.this)
                                .build();
                FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<User, chatViewHolder>(options) {
                    @Override
                    public chatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        return new chatViewHolder(LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.chats_list_item, parent, false));
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final chatViewHolder holder, int position, @NonNull User model) {
                        holder.setName(model.getName());
                        holder.setAvatar(model.getAvatar());

                        final String userId = getRef(position).getKey();
                        if(isGroup) {
                            holder.setCheckboxVisibility(View.VISIBLE);

                            if (groupMembers.contains(userId)) {
//                            holder.view.setBackgroundColor(getResources().getColor(R.color.blue));
                                holder.setCheckbox(true);
                            } else {
                                holder.setCheckbox(false);
                            }
                        }
                        else {
                            holder.setCheckboxVisibility(View.GONE);
                        }
                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isGroup) {
                                    if (groupMembers.contains(userId)) {
                                        holder.setCheckbox(false);
                                        groupMembers.remove(userId);
                                    } else {
                                        holder.setCheckbox(true);
                                        groupMembers.add(userId);
                                    }
//                                    chatsRef.child(chatID).child("isGroup").setValue(false);
                                    //databaseRef.child("chats").child(databaseReference.getKey()).child("dialogName").setValue("test");
                                    //databaseRef.child("chats").child(databaseReference.getKey()).child("dialogPhoto").setValue("test");
                                } else {
//                                    chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (connectedPeople.contains(userId)) {
                                        Intent chatIntent = new Intent(getBaseContext(), MessageActivity.class);
                                        chatIntent.putExtra("chatId", usersChats.get(connectedPeople.indexOf(userId)));
                                        startActivity(chatIntent);
                                    } else {
                                        chatsRef.push()
                                                .setValue("", new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError,
                                                                           DatabaseReference databaseReference) {
                                                        String chatID = databaseReference.getKey();
                                                        chatsRef.child(chatID).child("lastMessage").setValue("");
                                                        chatsRef.child(chatID).child("isGroup").setValue(false);
                                                        chatsRef.child(chatID).child("users").child(USER_ID).setValue("value");
                                                        chatsRef.child(chatID).child("users").child(userId).setValue("value");
                                                        chatsRef.child(chatID).child("admin").setValue(USER_ID);
                                                        databaseRef.child("users").child(USER_ID).child("user_chats").child(chatID).setValue("admin");
                                                        databaseRef.child("users").child(userId).child("user_chats").child(chatID).setValue("admin");

                                                        Intent chatIntent = new Intent(getBaseContext(), MessageActivity.class);
                                                        chatIntent.putExtra("chatId", chatID);
                                                        startActivity(chatIntent);
                                                    }
                                                });
                                    }
                                }
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                        }
//                                    });
//                                }
                            }
                        });
                        CheckBox cb = (CheckBox) holder.view.findViewById(R.id.cbGroupMember);
                        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if (groupMembers.contains(!b)) {
//                                    holder.setCheckbox(b);
                                    groupMembers.remove(userId);
                                } else {
//                                    holder.setCheckbox(true);
                                    groupMembers.add(userId);
                                }
                            }
                        });
                    }

                    @Override
                    public void onDataChanged() {
                        // If there are no chat messages, show a view that invites the user to add a message.
                        // mEmptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    }
                };
                chatsList.setAdapter(adapter);
                return true;
            }
        });

        usersChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    chatsRef.child(child.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean isGroup = false;
                            try{
                                isGroup = (boolean) dataSnapshot.child("isGroup").getValue();
                            }catch(Exception e) {
                                if (!isGroup) {
                                    for (DataSnapshot user : dataSnapshot.child("users").getChildren()) {
                                        if (!user.getKey().equals(USER_ID)) {
                                            usersChats.add(dataSnapshot.getKey());
                                            connectedPeople.add(user.getKey());
                                        }
                                        if (dataSnapshot.child("users").getChildrenCount() == 1) {
                                            usersChats.add(dataSnapshot.getKey());
                                            connectedPeople.add(user.getKey());
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnCreateGroup = (Button) findViewById(R.id.btnCreateGroup);
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatsRef.push()
                        .setValue("", new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError,
                                                   DatabaseReference databaseReference) {
                                String chatID = databaseReference.getKey();
                                chatsRef.child(chatID).child("lastMessage").setValue("");
                                chatsRef.child(chatID).child("isGroup").setValue(true);
                                chatsRef.child(chatID).child("users").child(USER_ID).setValue("value");
                                chatsRef.child(chatID).child("admin").setValue(USER_ID);
                                databaseRef.child("users").child(USER_ID).child("user_chats").child(chatID).setValue("admin");
                                for(int i=0; i<groupMembers.size(); i++){
                                    chatsRef.child(chatID).child("users").child(groupMembers.get(i)).setValue("value");
                                    databaseRef.child("users").child(groupMembers.get(i)).child("user_chats").child(chatID).setValue("admin");
                                }
                                Intent chatIntent = new Intent(getBaseContext(), MessageActivity.class);
                                chatIntent.putExtra("chatId", chatID);
                                startActivity(chatIntent);
                            }
                        });
            }
        });

        if(isGroup){
            btnCreateGroup.setVisibility(View.VISIBLE);
        } else {
            btnCreateGroup.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

//        Databaza.child("new").setValue("value");
    }

    public static class chatViewHolder extends RecyclerView.ViewHolder {
        View view;
        String userId;

        public chatViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setAvatar(String url) {
            ImageView avatarIV = (ImageView) view.findViewById(R.id.avatar_iv);
            Picasso.get().load(url).into(avatarIV);
        }

        public void setName(String lastMessage) {
            TextView name_tv = (TextView) view.findViewById(R.id.name_tv);
            name_tv.setText(lastMessage);
        }

        public void setCheckbox(boolean selected) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cbGroupMember);
            checkBox.setChecked(selected);
        }

        public void setCheckboxVisibility(int visibility) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cbGroupMember);
            checkBox.setVisibility(visibility);
        }

    }
}
