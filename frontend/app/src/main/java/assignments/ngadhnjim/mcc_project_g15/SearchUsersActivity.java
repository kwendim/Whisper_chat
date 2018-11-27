package assignments.ngadhnjim.mcc_project_g15;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class SearchUsersActivity extends AppCompatActivity {

    private RecyclerView chatsList;
    private DatabaseReference Databaza;
    private Query query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatsList = (RecyclerView)findViewById(R.id.chats_list);
        chatsList.setHasFixedSize(true);
        chatsList.setLayoutManager(new LinearLayoutManager(this));
        Databaza = FirebaseDatabase.getInstance().getReference().child("users");

        SearchView sv = (SearchView)findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("name").startAt(queryText).endAt(queryText+"\uf8ff").limitToLast(50);
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
                    protected void onBindViewHolder(@NonNull chatViewHolder holder, int position, @NonNull User model) {
                        holder.setName(model.getName());
                        holder.setAvatar(model.getAvatar());
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
    }
    @Override
    protected void onStart() {
        super.onStart();

//        Databaza.child("new").setValue("value");
    }

    public static class chatViewHolder extends  RecyclerView.ViewHolder{
        View view;
        public chatViewHolder(View itemView) {
            super(itemView);
            view=itemView;
        }
        public void setAvatar(String url){
            ImageView avatarIV = (ImageView) view.findViewById(R.id.avatar_iv);
            Picasso.get().load(url).into(avatarIV);
        }

        public void setName(String lastMessage){
            TextView name_tv = (TextView)view.findViewById(R.id.name_tv);
            name_tv.setText(lastMessage);
        }
    }
}
