package com.al70b.core.activities.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.objects.OtherUser;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 6/25/2016.
 */
public class DialogUserCommand extends Dialog {

    private Context context;
    private OtherUser otherUser;
    private String[] commands;

    public DialogUserCommand(Context context, OtherUser otherUser) {
        this(context, otherUser, null);
    }

    public DialogUserCommand(Context context, OtherUser otherUser, String[] commands) {
        super(context);
        this.context = context;
        this.otherUser = otherUser;
        this.commands = commands;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_list_of_commands);
        TextView title = (TextView) findViewById(R.id.text_view_dialog_list_of_commands_title);
        CircleImageView imgViewProfilePicture = (CircleImageView) findViewById(R.id.image_view_dialog_list_of_commands_icon);
        CircleImageView imgViewStatus = (CircleImageView) findViewById(R.id.image_view_dialog_list_of_commands_status);
        ListView lv = (ListView) findViewById(R.id.list_view_dialog_list_of_commands);

        title.setText(otherUser.getName());
        imgViewStatus.setImageResource(otherUser.getOnlineStatus().getResourceID());

        if(commands == null) {
            commands = context.getResources().getStringArray(R.array.friend_item_commands);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, commands);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            }
        });

        Glide.with(context)
                .load(otherUser.getProfilePicture().getThumbnailFullPath())
                .asBitmap()
                .fitCenter()
                .centerCrop()
                .placeholder(R.drawable.avatar)
                .into(imgViewProfilePicture);
    }

    @Override
    public void show() {
        super.show();

    }
}
