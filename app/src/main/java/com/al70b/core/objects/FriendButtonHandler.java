package com.al70b.core.objects;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.MembersListActivity;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.server_methods.RequestsInterface;

/**
 * Created by Naseem on 6/20/2015.
 */
public class FriendButtonHandler {

    // all this is for getting dialog's response
    private static final CommandWrapper DISMISS = new CommandWrapper(Command.NO_OP);
    private ServerResponse<Integer> sr;
    private RequestsInterface requestsInterface;
    private OtherUser.FriendStatus friendStatus;

    public boolean handle(final Activity activity, final CurrentUser currentUser, final OtherUser otherUser) {

        final StringBuilder str = new StringBuilder();

        friendStatus = otherUser.getFriendStatus();

        // to compare with the result
        OtherUser.FriendStatus temp = friendStatus;
        requestsInterface = new RequestsInterface(activity.getApplicationContext());

        if (friendStatus.isNoFriendRequest()) {
            // send friend request
            try {
                sr = requestsInterface.sendApproveFriendRequest(currentUser.getUserID(), currentUser.getAccessToken()
                        , otherUser.getUserID());

                if (sr.isSuccess()) {
                    str.append(activity.getString(R.string.friend_request_was_sent, otherUser.getName()));

                    friendStatus.setValue(OtherUser.FriendStatus.SENT_REQUEST_PENDING);
                } else {
                    str.append(sr.getErrorMsg());
                }
            } catch (ServerResponseFailedException ex) {
                str.append(ex.toString());
            }

            // show appropriate toast message
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, str.toString(), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (friendStatus.isReceivedRequestPending()
                || friendStatus.isSentRequestPending()) {

            final Command decline = new Command() {
                public void execute() {
                    // decline friend request (incoming\outgoing)
                    try {
                        sr = requestsInterface.removeFriendRequest(currentUser.getUserID(), currentUser.getAccessToken()
                                , otherUser.getUserID());

                        if (sr.isSuccess()) {
                            if (sr.getResult() == 2 || sr.getResult() == 3) {
                                str.append(activity.getString(R.string.friend_request_was_canceled, otherUser.getName()));
                                //MembersListActivity.updateAdapter(sr.getResult(), otherUser.getUserID());
                            }
                            friendStatus.setValue(OtherUser.FriendStatus.NONE);
                        } else {
                            str.append(sr.getErrorMsg());
                        }
                    } catch (ServerResponseFailedException ex) {
                        str.append(ex.toString());
                    }


                    // show appropriate toast message
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, str.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };

            if (friendStatus.isReceivedRequestPending()) {
                final Command approve = new Command() {
                    public void execute() {
                        // approve friend request
                        try {
                            sr = requestsInterface.sendApproveFriendRequest(currentUser.getUserID(), currentUser.getAccessToken()
                                    , otherUser.getUserID());

                            if (sr.isSuccess()) {
                                if (sr.getResult() == 2) {
                                    str.append(activity.getString(R.string.friend_request_was_approved, otherUser.getName()));
                                    //xnMembersListActivity.updateAdapter(sr.getResult(), otherUser.getUserID());
                                }

                                friendStatus.setValue(OtherUser.FriendStatus.FRIENDS);
                            } else {
                                str.append(sr.getErrorMsg());
                            }
                        } catch (ServerResponseFailedException ex) {
                            str.append(ex.toString());
                        }

                        // show appropriate toast message
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, str.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                };

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        showDialog(activity, R.string.please_choose, activity.getString(R.string.what_to_do)
                                , R.string.approve, approve,
                                true, R.string.decline, decline);

                    }
                });
            } else {
                decline.execute();
            }

        } else if (friendStatus.isFriend()) {
            final Command unfriend = new Command() {
                public void execute() {
                    try {
                        sr = requestsInterface.removeFriendRequest(currentUser.getUserID(), currentUser.getAccessToken()
                                , otherUser.getUserID());

                        if (sr.isSuccess()) {
                            str.append(activity.getString(R.string.friend_was_removed, otherUser.getName()));

                            friendStatus.setValue(OtherUser.FriendStatus.NONE);
                        } else {
                            str.append(sr.getErrorMsg());
                        }
                    } catch (ServerResponseFailedException ex) {
                        str.append(ex.toString());
                    }

                    // show appropriate toast message
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, str.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDialog(activity, R.string.caution, activity.getString(R.string.unfriend_user_caution_message, otherUser.getName())
                            , R.string.button_ok, unfriend,
                            false, -1, null);

                }
            });
        }


        // friend status was changed, return true
        return otherUser.getFriendStatus().getValue() != temp.getValue();
    }

    private void showDialog(final Activity activity, int title, String message,
                            int positiveStr, Command onPositiveCommand,
                            boolean neutral, int neutralStr, Command onNeutralCommand) {
        // show dialog to insure current user's decision of unfriending this user
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // customize the alert dialog
        LinearLayout linearLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.dialog, null, false);
        (linearLayout.findViewById(R.id.dialog_progress_bar)).setVisibility(View.GONE);
        (linearLayout.findViewById(R.id.dialog_icon)).setVisibility(View.GONE);
        ((TextView) linearLayout.findViewById(R.id.dialog_title)).setText(title);

        // show the alert dialog
        final AlertDialog.Builder alertBuilder = builder.setCustomTitle(linearLayout)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton(positiveStr, new CommandWrapper(onPositiveCommand))
                .setNegativeButton(R.string.cancel, DISMISS);

        if (neutral)
            alertBuilder.setNeutralButton(neutralStr, new CommandWrapper(onNeutralCommand));

        // create and show the dialog
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    public interface Command {
        Command NO_OP = new Command() {
            public void execute() {

            }
        };

        void execute();
    }

    public static class CommandWrapper implements DialogInterface.OnClickListener {
        private Command command;

        public CommandWrapper(Command command) {
            this.command = command;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            command.execute();
        }
    }


}
