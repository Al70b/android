package com.al70b.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.server_methods.RequestsInterface;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nasee on 5/28/2016.
 */
public class FriendsRequestsAdapter extends ArrayAdapter<OtherUser> {

    private Context context;

    private List<OtherUser> friendsRequests;

    private int layout;

    private CurrentUser currentUser;

    private RequestsInterface requestsInterface;

    public FriendsRequestsAdapter(Context context, int layout, List<OtherUser> friendsRequests
                , CurrentUser currentUser) {
        super(context, layout, friendsRequests);

        this.friendsRequests = friendsRequests;
        this.context = context;
        this.layout = layout;
        this.requestsInterface = new RequestsInterface(context);
        this.currentUser = currentUser;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        FriendRequestItemHolder holder;

        if (row == null) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layout, parent, false);

            holder = new FriendRequestItemHolder();

            holder.profilePicture = (CircleImageView) row.findViewById(R.id.list_item_friend_request_profile_image);
            holder.name = (TextView) row.findViewById(R.id.tv_list_item_friend_request_name);
            holder.address = (TextView) row.findViewById(R.id.tv_list_item_friend_request_info);
            holder.imgBtnAccept = (ImageButton) row.findViewById(R.id.list_item_img_btn_accept);
            holder.imgBtnReject = (ImageButton) row.findViewById(R.id.list_item_img_btn_reject);
            holder.progressBar = (ProgressBar) row.findViewById(R.id.progress_bar_list_item_friend_request);
            holder.responseStatus = (TextView) row.findViewById(R.id.tv_list_item_friend_request_in_progress);
            row.setTag(holder);
        } else {
            holder = (FriendRequestItemHolder) row.getTag();
        }

        final OtherUser otherUser = friendsRequests.get(position);

        holder.name.setText(otherUser.getName());

        //holder.age.setText(context.getApplicationContext().getString(R.string.age_of, calculateAge(otherUser.getDateOfBirth())));
        holder.address.setText(otherUser.getAddress().toString());

        Glide.with(context).load(otherUser.getProfilePictureThumbnailPath())
                .asBitmap()
                .placeholder(R.drawable.avatar)
                .into(holder.profilePicture);

        final ImageButton finalBtnAccept = holder.imgBtnAccept;
        final ImageButton finalBtnReject = holder.imgBtnReject;
        final ProgressBar finalProgressBar = holder.progressBar;
        final TextView finalResponseStatus = holder.responseStatus;
        holder.imgBtnAccept.setOnClickListener(new View.OnClickListener() {

            private void showProgress(boolean show) {
                int showInt = show ? View.VISIBLE : View.GONE;
                int btnsShow = !show ? View.VISIBLE : View.GONE;

                // show\hide relevant widgets
                finalBtnAccept.setVisibility(btnsShow);
                finalBtnReject.setVisibility(btnsShow);

                finalProgressBar.setVisibility(showInt);
                finalResponseStatus.setVisibility(showInt);
            }

            @Override
            public void onClick(View view) {

                /*
                showProgress(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            ServerResponse<Integer> sr = requestsInterface.sendApproveFriendRequest(currentUser.getUserID(), currentUser.getAccessToken()
                                    , otherUser.getUserID());

                            if (sr.isSuccess()) {
                                numOfFriendsRequests--;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        // inform that requests was approved
                                        Toast.makeText(context, getString(R.string.friend_request_was_approved, otherUser.getName()), Toast.LENGTH_SHORT).show();

                                        // update the friends requests header
                                        updateFriendsRequestsCounter();

                                        // remove user from list of friends requests, and update adapter
                                        listOfFriendRequests.remove(otherUser);
                                        friendsRequestsAdapter.notifyDataSetChanged();

                                        // add user to list of friends & notify adapter
                                        listOfFriends.add(otherUser);
                                        friendsListAdapter.notifyDataSetChanged();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // inform that requests was approved
                                        Toast.makeText(context, getString(R.string.failed_respond_to_friend_request), Toast.LENGTH_SHORT).show();

                                        showProgress(false);
                                    }
                                });
                            }
                        } catch (ServerResponseFailedException ex) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // inform that requests was approved
                                    Toast.makeText(context, getString(R.string.error_server_connection_falied), Toast.LENGTH_SHORT).show();

                                    showProgress(false);
                                }
                            });
                        }

                    }
                }).start();

*/
                // TODO update FRIENDS counter if it exists
            }
        });

        holder.imgBtnAccept.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, context.getString(R.string.accept_friend_request), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        holder.imgBtnReject.setOnClickListener(new View.OnClickListener() {

            private void showProgress(boolean show) {
                int showInt = show ? View.VISIBLE : View.GONE;
                int btnsShow = !show ? View.VISIBLE : View.GONE;

                // show\hide relevant widgets
                finalBtnAccept.setVisibility(btnsShow);
                finalBtnReject.setVisibility(btnsShow);

                finalProgressBar.setVisibility(showInt);
                finalResponseStatus.setVisibility(showInt);
            }

            @Override
            public void onClick(View view) {
                /*
                showProgress(true);

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            ServerResponse<Integer> sr = requestsInterface.removeFriendRequest(currentUser.getUserID(), currentUser.getAccessToken()
                                    , otherUser.getUserID());

                            if (sr.isSuccess()) {
                                numOfFriendsRequests--;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // inform that requests was approved
                                        Toast.makeText(context, getString(R.string.friend_request_was_rejected, otherUser.getName()), Toast.LENGTH_SHORT).show();

                                        // update the friends requests header
                                        updateFriendsRequestsCounter();

                                        // update list
                                        listOfFriendRequests.remove(otherUser);
                                        friendsRequestsAdapter.notifyDataSetChanged();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // inform that requests was approved
                                        Toast.makeText(context, getString(R.string.failed_respond_to_friend_request), Toast.LENGTH_SHORT).show();

                                        showProgress(false);
                                    }
                                });
                            }
                        } catch (ServerResponseFailedException ex) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // inform that requests was approved
                                    Toast.makeText(context, getString(R.string.error_server_connection_falied), Toast.LENGTH_SHORT).show();

                                    showProgress(false);
                                }
                            });
                        }

                    }
                }).start();

                */
            }
        });

        holder.imgBtnReject.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, context.getString(R.string.reject_friend_request), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return row;
    }

    private class FriendRequestItemHolder {
        TextView name, address;
        CircleImageView profilePicture;
        ImageButton imgBtnAccept, imgBtnReject;
        TextView responseStatus;
        ProgressBar progressBar;
    }
}
