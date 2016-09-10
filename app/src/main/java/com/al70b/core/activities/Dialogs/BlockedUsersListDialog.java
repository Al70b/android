package com.al70b.core.activities.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.activities.user_home_activity_underlying.ChatHandler;
import com.al70b.core.adapters.BlockedUsersAdapter;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;

import java.util.List;

/**
 * Created by nasee on 9/10/2016.
 */
public class BlockedUsersListDialog extends Dialog {

    private Context context;
    private CurrentUser currentUser;
    private ChatHandler chatHandler;

    public BlockedUsersListDialog(Context context, CurrentUser currentUser,
                                  ChatHandler chatHandler) {
        super(context);

        this.context = context;
        this.currentUser = currentUser;
        this.chatHandler = chatHandler;
    }

    private BlockedUsersAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_blocked_users);
        ListView lv = (ListView) findViewById(R.id.list_view_dialog_blocked_users);

        adapter = new BlockedUsersAdapter(context,
                R.layout.list_item_blocked_users,
                chatHandler.getBlockedUsersList(), currentUser,
                chatHandler);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(null);
        lv.setOnItemLongClickListener(null);
    }

    public void notifyAdapter() {
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void show() {
        super.show();
    }
}
