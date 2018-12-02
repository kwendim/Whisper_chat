package mcc_2018_g15.chatapp.holders;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import mcc_2018_g15.chatapp.Message;
import mcc_2018_g15.chatapp.R;

public class CustomOutcomingTextMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<Message> {

    public CustomOutcomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);

        //time.setText(message.getStatus() + " " + time.getText());
    }
}
