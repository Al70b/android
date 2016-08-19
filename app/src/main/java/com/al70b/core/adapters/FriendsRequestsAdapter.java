package com.al70b.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.FriendsRequestsListActivity.OnFriendRequestAction;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.misc.Enums;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 5/28/2016.
 */
public class FriendsRequestsAdapter extends ArrayAdapter<OtherUser> {

    private final String TAG = "FriendsRequestsAdapter";
    private Context context;

    private List<OtherUser> friendsRequests;

    private int layout;

    private CurrentUser currentUser;

    private RequestsInterface requestsInterface;

    private OnFriendRequestAction onFriendRequestAction;

    public FriendsRequestsAdapter(Context context, int layout,
                                  List<OtherUser> friendsRequests, CurrentUser currentUser,
                                  OnFriendRequestAction onFriendRequestAction) {
        super(context, layout, friendsRequests);

        this.friendsRequests = friendsRequests;
        this.context = context;
        this.layout = layout;
        this.requestsInterface = new RequestsInterface(context);
        this.currentUser = currentUser;
        this.onFriendRequestAction = onFriendRequestAction;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final FriendRequestItemHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layout, parent, false);

            holder = new FriendRequestItemHolder();
            holder.mainLayout = (RelativeLayout) row.findViewById(R.id.layout_list_item_friend_request_main);
            holder.profilePicture = (CircleImageView) row.findViewById(R.id.list_item_friend_request_profile_image);
            holder.name = (TextView) row.findViewById(R.id.tv_list_item_friend_request_name);
            holder.address = (TextView) row.findViewById(R.id.tv_list_item_friend_request_info);
            holder.imgBtnAccept = (ImageButton) row.findViewById(R.id.list_item_img_btn_accept);
            holder.imgBtnReject = (ImageButton) row.findViewById(R.id.list_item_img_btn_reject);
            holder.progressBarAccept = (ProgressBar) row.findViewById(R.id.progress_btn_accept);
            holder.progressBarReject = (ProgressBar) row.findViewById(R.id.progress_btn_reject);
            row.setTag(holder);
        } else {
            holder = (FriendRequestItemHolder) row.getTag();
        }

        final OtherUser otherUser = friendsRequests.get(position);
        holder.name.setText(otherUser.getName());
        holder.address.setText(otherUser.getAddress().toString());
        Glide.with(context).load(otherUser.getProfilePictureThumbnailPath())
                .asBitmap()
                .placeholder(R.drawable.avatar)
                .fitCenter()
                .into(holder.profilePicture);


        holder.imgBtnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AcceptFriendRequestTask(otherUser, holder).execute();
            }
        });

        holder.imgBtnAccept.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, getString(R.string.accept_friend_request), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        holder.imgBtnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RejectFriendRequestTask(otherUser, holder).execute();
            }
        });

        holder.imgBtnReject.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, getString(R.string.reject_friend_request), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return row;
    }

    private class FriendRequestItemHolder {
        RelativeLayout mainLayout;
        TextView name, address;
        CircleImageView profilePicture;
        ImageButton imgBtnAccept, imgBtnReject;
        ProgressBar progressBarAccept, progressBarReject;
    }

    private class AcceptFriendRequestTask extends AsyncTask<Void, Void, ServerResponse<Integer>> {
        OtherUser otherUser;
        FriendRequestItemHolder holder;

        protected AcceptFriendRequestTask(OtherUser otherUser, FriendRequestItemHolder holder) {
            this.otherUser = otherUser;
            this.holder = holder;
        }

        @Override
        protected void onPreExecute() {
            holder.imgBtnAccept.setVisibility(View.GONE);
            holder.progressBarAccept.setVisibility(View.VISIBLE);
            holder.imgBtnReject.setEnabled(false);
        }

        @Override
        protected ServerResponse<Integer> doInBackground(Void... v) {
            ServerResponse<Integer> serverResponse = null;

            try {
                serverResponse = requestsInterface.sendApproveFriendRequest(
                        currentUser.getUserID(),
                        currentUser.getAccessToken()
                        , otherUser.getUserID());
            } catch (ServerResponseFailedException ex) {
                Log.e(TAG, ex.toString());
            }

            return serverResponse;
        }

        @Override
        protected void onPostExecute(ServerResponse<Integer> serverResponse) {
            if (serverResponse == null) {
                // error occurred
                Toast.makeText(context, getString(R.string.error_server_connection_falied), Toast.LENGTH_SHORT).show();
                holder.imgBtnAccept.setVisibility(View.VISIBLE);
                holder.progressBarAccept.setVisibility(View.GONE);
                holder.imgBtnReject.setEnabled(true);
            } else {

                if (serverResponse.isSuccess()) {
                    // inform that requests was approved
                    Toast.makeText(context, getString(R.string.friend_request_was_approved,
                            otherUser.getName()), Toast.LENGTH_SHORT).show();

                    holder.imgBtnAccept.setVisibility(View.VISIBLE);
                    holder.progressBarAccept.setVisibility(View.GONE);
                    holder.imgBtnAccept.setEnabled(false);
                    holder.imgBtnReject.setEnabled(false);


                    holder.mainLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.trans_bright_green));
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {

                            holder.mainLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    onFriendRequestAction.callback(Enums.FriendRequestAction.ACCEPTED);
                                    // remove user from list of friends requests, and update adapter
                                    friendsRequests.remove(otherUser);
                                    notifyDataSetChanged();
                                }
                            });
                        }
                    }, 3000);
                } else {
                    // inform that requests was approved
                    Toast.makeText(context, getString(R.string.failed_respond_to_friend_request), Toast.LENGTH_SHORT).show();
                    holder.imgBtnAccept.setVisibility(View.VISIBLE);
                    holder.progressBarAccept.setVisibility(View.GONE);
                    holder.imgBtnReject.setEnabled(true);
                }
            }
        }
    }

    private class RejectFriendRequestTask extends AsyncTask<Void, Void, ServerResponse<Integer>> {
        OtherUser otherUser;
        FriendRequestItemHolder holder;

        protected RejectFriendRequestTask(OtherUser otherUser, FriendRequestItemHolder holder) {
            this.otherUser = otherUser;
            this.holder = holder;
        }

        @Override
        protected void onPreExecute() {
            holder.imgBtnReject.setVisibility(View.GONE);
            holder.progressBarReject.setVisibility(View.VISIBLE);
            holder.imgBtnAccept.setEnabled(false);
        }

        @Override
        protected ServerResponse<Integer> doInBackground(Void... v) {
            ServerResponse<Integer> serverResponse = null;

            try {
                serverResponse = requestsInterface.removeFriendRequest(
                        currentUser.getUserID(),
                        currentUser.getAccessToken()
                        , otherUser.getUserID());
            } catch (ServerResponseFailedException ex) {
                Log.e(TAG, ex.toString());
            }

            return serverResponse;
        }

        @Override
        protected void onPostExecute(ServerResponse<Integer> serverResponse) {
            if (serverResponse == null) {
                // error occurred
                Toast.makeText(context, getString(R.string.error_server_connection_falied), Toast.LENGTH_SHORT).show();
                holder.imgBtnReject.setVisibility(View.VISIBLE);
                holder.progressBarReject.setVisibility(View.GONE);
                holder.imgBtnAccept.setEnabled(true);
            } else {

                if (serverResponse.isSuccess()) {
                    // inform that requests was approved
                    Toast.makeText(context, getString(R.string.friend_request_was_rejected,
                            otherUser.getName()), Toast.LENGTH_SHORT).show();

                    holder.imgBtnReject.setVisibility(View.VISIBLE);
                    holder.progressBarReject.setVisibility(View.GONE);
                    holder.imgBtnReject.setEnabled(false);
                    holder.imgBtnAccept.setEnabled(false);

                    holder.mainLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.trans_bright_red));
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {

                            holder.mainLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    onFriendRequestAction.callback(Enums.FriendRequestAction.REJECTED);
                                    // remove user from list of friends requests, and update adapter
                                    friendsRequests.remove(otherUser);
                                    notifyDataSetChanged();
                                }
                            });
                        }
                    }, 3000);
                } else {
                    // inform that requests was approved
                    Toast.makeText(context, getString(R.string.failed_respond_to_friend_request),
                            Toast.LENGTH_SHORT).show();
                    holder.imgBtnReject.setVisibility(View.VISIBLE);
                    holder.progressBarReject.setVisibility(View.GONE);
                    holder.imgBtnAccept.setEnabled(true);
                }
            }
        }
    }

    private String getString(int resId) {
        return context.getString(resId);
    }

    private String getString(int resId, String... strs) {
        return context.getString(resId, strs);
    }


}
