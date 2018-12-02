package mcc_2018_g15.chatapp.holders;

import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import mcc_2018_g15.chatapp.Message;
import mcc_2018_g15.chatapp.R;
public class CustomIncomingTextMessageViewHolder extends MessageHolders.IncomingTextMessageViewHolder<Message> {
    private View onlineIndicator;
    private TextView username;

    public CustomIncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);

        //onlineIndicator = (TextView) itemView.findViewById(R.id.onlineIndicator);
        username = itemView.findViewById(R.id.messageusername);

    }
    @Override
    public void onBind(Message message) {
        super.onBind(message);

        boolean isOnline = message.getUser().isBoolean();
//        if (isOnline) {
//            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
//        } else {
//            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
//        }
        username.setText(message.getUser().getName());

        //We can set click listener on view from payload
        final Payload payload = (Payload) this.payload;
        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (payload != null && payload.avatarClickListener != null) {
                    payload.avatarClickListener.onAvatarClick();
                }
            }
        });
    }

    public static class Payload {
        public OnAvatarClickListener avatarClickListener;
    }

    public interface OnAvatarClickListener {
        void onAvatarClick();
    }

}
