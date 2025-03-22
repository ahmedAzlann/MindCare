package com.azlan.mindcare;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class ChatAdapter extends ArrayAdapter<String> {

    public ChatAdapter(Context context, ArrayList<String> messages) {
        super(context, 0, messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_item, parent, false);
        }

        TextView messageText = convertView.findViewById(R.id.message_text);
        String message = getItem(position);

        if (message != null) {
            messageText.setText(message);
            messageText.setAutoLinkMask(Linkify.WEB_URLS);
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
        }

        return convertView;
    }
}
