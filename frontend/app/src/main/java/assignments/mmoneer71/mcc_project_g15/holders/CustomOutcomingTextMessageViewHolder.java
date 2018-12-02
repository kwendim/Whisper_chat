package assignments.mmoneer71.mcc_project_g15.holders;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import assignments.mmoneer71.mcc_project_g15.Message;

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
