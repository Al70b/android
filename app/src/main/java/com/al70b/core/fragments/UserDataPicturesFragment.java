package com.al70b.core.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.DisplayPictureActivity;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.activities.Dialogs.PleaseWaitDialog;
import com.al70b.core.activities.Dialogs.QuestionAlert;
import com.al70b.core.adapters.UserImageAdapter;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.ImageHandler;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.Picture;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Naseem on 5/20/2015.
 */
public class UserDataPicturesFragment extends Fragment {

    // constants for requests codes
    public static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    public static final int REQUEST_CODE_GALLERY_PICKER = 2;
    private static final String TAG = "UserDataPicturesFrag";

    private static boolean fullCapacity;

    private UserHomeActivity activity;
    private CurrentUser currentUser;

    // grid view and image adapter
    private GridView gridView;
    private UserImageAdapter adapter;

    // to hold profile image
    private ImageView imgViewProfile;

    private List<Picture> listOfPictures;

    private Picture profilePicture;

    // File for captured photo
    private File photoFile;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (UserHomeActivity) getActivity();

        // in order to edit action bar
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_data_pictures, container, false);
        imgViewProfile = (ImageView) viewGroup.findViewById(R.id.image_view_user_data_pictures_profile);
        gridView = (GridView) viewGroup.findViewById(R.id.grid_view_user_pictures);

        gridView.setEmptyView(viewGroup.findViewById(R.id.text_view_empty_grid_view));

        if (savedInstanceState == null) {
            currentUser = ((MyApplication) getActivity().getApplication()).getCurrentUser();

            // get currentUser photos
            listOfPictures = currentUser.getPicturesList();

            updateCapacityFlag();

            // get currentUser profile picture
            profilePicture = currentUser.getProfilePicture();

            // create adapter and set it to grid view
            adapter = new UserImageAdapter(activity, listOfPictures);

            gridView.setAdapter(adapter);
        }

        if (currentUser != null && currentUser.isProfilePictureSet()) {
            // get currentUser profile picture
            Glide.with(activity.getApplicationContext())
                    .load(profilePicture.getThumbnailFullPath())
                    .asBitmap()
                    .fitCenter()
                    .placeholder(R.drawable.avatar)
                    .into(imgViewProfile);
        } else {
            imgViewProfile.setImageResource(R.drawable.avatar);
        }

        imgViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!currentUser.isProfilePictureSet())
                    return;

                activity.toUserData = true;
                UserDataFragment.raiseGoToMyPicturesFlag();

                Intent intent = new Intent(activity, DisplayPictureActivity.class);
                intent.putExtra(DisplayPictureActivity.THUMBNAIL_KEY, currentUser.getProfilePictureThumbnailPath());
                intent.putExtra(DisplayPictureActivity.FULL_PICTURE_KEY, currentUser.getProfilePicturePath());
                startActivity(intent);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Picture pic = (Picture) adapter.getItem(i);

                activity.toUserData = true;
                UserDataFragment.raiseGoToMyPicturesFlag();

                Intent intent = new Intent(activity, DisplayPictureActivity.class);
                intent.putExtra(DisplayPictureActivity.THUMBNAIL_KEY, pic.getThumbnailFullPath());
                intent.putExtra(DisplayPictureActivity.FULL_PICTURE_KEY, pic.getPictureFullPath());
                startActivity(intent);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Picture pic = (Picture) adapter.getItem(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                AlertDialog alert = builder.setTitle(R.string.picture_properties)
                        .setItems(R.array.picture_commands, new DialogListClickListener(i))
                        .setIcon(pic.getDrawable(activity.getApplicationContext()))
                        .create();
                alert.setCanceledOnTouchOutside(true);
                alert.show();

                return true;
            }
        });

        return viewGroup;
    }

    @Override
    public void onStop() {
        super.onStop();

        fullCapacity = false;
    }

    private void updateCapacityFlag() {
        fullCapacity = listOfPictures.size() >= AppConstants.MAX_NUMBER_OF_PICTURES_PER_USER;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = activity.isChangingConfigurations();

        if (!drawerOpen && menu.findItem(R.id.menu_item_user_picture_capture) == null) { // if drawer is closed add the content menu item

            if (menu.findItem(R.id.action_user_data_edit) != null)
                menu.removeItem(R.id.action_user_data_edit);

            menu.add(Menu.NONE, R.id.menu_item_user_picture_capture, Menu.NONE, R.string.capture_photo)
                    .setIcon(R.drawable.ic_action_camera)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                menu.findItem(R.id.menu_item_user_picture_capture).setEnabled(false);
            } else {
                menu.findItem(R.id.menu_item_user_picture_capture).setEnabled(true);
            }

            menu.add(Menu.NONE, R.id.menu_item_user_picture_pick, Menu.NONE, R.string.upload_photo_from_device)
                    .setIcon(R.drawable.ic_action_upload)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                // implementation is in the activity
                return false;
            case R.id.action_friends:
                // implementation is in the activity
                return false;
            case R.id.menu_item_user_picture_capture:
                if (fullCapacity) {
                    Toast.makeText(activity, activity.getString(R.string.pictures_limit_reach
                            , AppConstants.MAX_NUMBER_OF_PICTURES_PER_USER), Toast.LENGTH_SHORT).show();
                    return true;
                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                    // Create the File where the photo should go
                    try {
                        photoFile = ImageHandler.createImageFile(activity.getApplicationContext());
                    } catch (IOException ex) {
                        Log.e(TAG, ex.toString());
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));

                        // to return to my pictures
                        activity.toUserData = true;
                        UserDataFragment.raiseGoToMyPicturesFlag();
                        getParentFragment().startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE);
                    }
                }

                return true;
            case R.id.menu_item_user_picture_pick:
                if (fullCapacity) {
                    Toast.makeText(activity, activity.getString(R.string.pictures_limit_reach
                            , AppConstants.MAX_NUMBER_OF_PICTURES_PER_USER), Toast.LENGTH_SHORT).show();
                    return true;
                }

                Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK);
                choosePhotoIntent.setType("image/*");

                if (choosePhotoIntent.resolveActivity(activity.getPackageManager()) != null) {
                    // to return to my pictures
                    activity.toUserData = true;
                    UserDataFragment.raiseGoToMyPicturesFlag();
                    getParentFragment().startActivityForResult(choosePhotoIntent, REQUEST_CODE_GALLERY_PICKER);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // generate image name
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png";

        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE &&
                resultCode == UserHomeActivity.RESULT_OK) {
            // prepare picture for upload
            uploadPictureToServer(fileName, photoFile.getAbsolutePath());
        } else if (requestCode == REQUEST_CODE_GALLERY_PICKER &&
                resultCode == UserHomeActivity.RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            // Get the cursor
            Cursor cursor = activity.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            if (cursor != null) {
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgPath = cursor.getString(columnIndex);
                cursor.close();

                if (imgPath != null) {
                    Log.d(TAG, "Prepare for picture upload, uploading: " + fileName);
                    uploadPictureToServer(fileName, imgPath);
                }
            }

        }
    }

    private void promptForSetAsProfilePicture(final Picture pic) {
        final QuestionAlert alert = new QuestionAlert(activity, activity.getResources().getString(R.string.question)
                , activity.getResources().getString(R.string.set_as_profile_picture), -1, R.string.yes, R.string.no);

        alert.show();

        alert.setPositiveButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestsInterface requestsInterface = new RequestsInterface(activity);
                ServerResponse<Boolean> sr = requestsInterface.setProfilePicture(currentUser, pic.getId());

                String messageToToast;

                if (sr.isSuccess() && sr.getResult()) {
                    currentUser.setProfilePicture(pic.getId());
                    profilePicture = currentUser.getProfilePicture();

                    // update profile picture in few places
                    Glide.with(activity.getApplicationContext())
                            .load(pic.getThumbnailFullPath())
                            .asBitmap()
                            .fitCenter()
                            .placeholder(R.drawable.avatar)
                            .into(imgViewProfile);
                    activity.updateProfilePictureInNavigationDrawer(pic.getThumbnailFullPath());

                    messageToToast = activity.getString(R.string.profile_picture_change_successfully);
                } else {
                    messageToToast = sr.getErrorMsg();
                }

                Toast.makeText(activity, messageToToast, Toast.LENGTH_SHORT).show();
                alert.dismiss();
            }
        });

        alert.setNegativeButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });


    }

    private class DialogListClickListener implements DialogInterface.OnClickListener {

        private int position;

        public DialogListClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // set as profile picture
            final Picture pic = listOfPictures.get(position);

            RequestsInterface requestsInterface = new RequestsInterface(activity);
            String messageToToast;
            ServerResponse<Boolean> sr;
            switch (which) {    // which command was chosen
                case 0:
                    // Set chosen picture as profile picture
                    sr = requestsInterface.setProfilePicture(currentUser, pic.getId());
                    if (sr.isSuccess() && sr.getResult()) {
                        currentUser.setProfilePicture(pic.getId());
                        profilePicture = currentUser.getProfilePicture();

                        // set profile picture in drawer and up in this fragment
                        Glide.with(activity.getApplicationContext())
                                .load(pic.getThumbnailFullPath())
                                .asBitmap()
                                .fitCenter()
                                .placeholder(R.drawable.avatar)
                                .into(imgViewProfile);
                        activity.updateProfilePictureInNavigationDrawer(pic.getThumbnailFullPath());

                        messageToToast = activity.getString(R.string.profile_picture_change_successfully);
                    } else {
                        messageToToast = activity.getString(R.string.profile_picture_change_failed);
                    }

                    Toast.makeText(activity, messageToToast, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    // Delete chosen picture
                    sr = requestsInterface.deletePhoto(currentUser, pic.getId());

                    if (sr.isSuccess() && sr.getResult()) {
                        // if deleted picture is profile picture, update in few places
                        if (profilePicture != null && profilePicture.getId() == pic.getId()) {
                            imgViewProfile.setImageResource(R.drawable.avatar);
                            imgViewProfile.invalidate();
                            activity.updateProfilePictureInNavigationDrawer(R.drawable.avatar);
                            currentUser.removeProfilePicture();
                        }

                        // remove photo
                        listOfPictures.remove(pic);

                        updateCapacityFlag();

                        adapter.notifyDataSetChanged();

                        messageToToast = activity.getString(R.string.picture_was_removed);


                    } else {
                        messageToToast = activity.getString(R.string.profile_picture_delete_failed);
                    }

                    Toast.makeText(activity, messageToToast, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    // Details of the chosen picture
                    String message = activity.getString(R.string.picture_details_template, pic.getPictureName(), pic.getCreatedDate());
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    AlertDialog detailsAlert = builder.setTitle(R.string.more_details)
                            .setMessage(message)
                            .create();
                    detailsAlert.setCanceledOnTouchOutside(true);
                    detailsAlert.show();
                    break;
                default:
            }
        }
    }


    //////// Upload Picture To Server ///////////
    private void uploadPictureToServer(final String fileName, final String picturePath) {
        // show uploading dialog
        final PleaseWaitDialog alert = new PleaseWaitDialog(activity, R.string.uploading_your_photo_to_server);
        alert.show();

        Thread t = new Thread(new UploadPictureJob(fileName, picturePath, new MyCallback() {
            @Override
            public void onSuccessCallback(final Picture pic) {
                // add picture to list of pictures for this currentUser
                listOfPictures.add(pic);

                // after last picture was added successfully
                updateCapacityFlag();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        adapter.notifyDataSetChanged();

                        // dismiss the alert dialog
                        alert.dismiss();

                        promptForSetAsProfilePicture(pic);

                        Toast.makeText(activity, activity.getString(R.string.uploading_photo_was_successfull)
                                , Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailCallback(String err) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alert.dismiss();

                        Toast.makeText(activity, activity.getString(R.string.uploading_photo_was_failure)
                                , Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }));
        t.start();
    }

    private class UploadPictureJob implements Runnable {
        private String fileName;
        private String picturePath;
        private MyCallback callbacks;

        public UploadPictureJob(String fileName, String picturePath,
                                MyCallback callbacks) {
            this.fileName = fileName;
            this.picturePath = picturePath;
            this.callbacks = callbacks;
        }

        @Override
        public void run() {
            Log.d(TAG, "Encoding image");
            String encodedImage = encodeImageToString();

            if (encodedImage != null) {
                Log.d(TAG, "Image is encoded successfully, uploading to server");
                uploadEncodedImage(encodedImage);
            } else {
                Log.d(TAG, "Failed to encode image");
                callbacks.onFailCallback("Failed to encode image");
            }
        }

        private String encodeImageToString() {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath,
                    options);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            // Must compress the Image to reduce image size to make upload easy
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream);
            byte[] byte_arr = stream.toByteArray();

            // Encode Image to 64 base String
            return Base64.encodeToString(byte_arr, 0);
        }

        private void uploadEncodedImage(String encodedImage) {
            // send image to server
            ServerResponse<Picture> sr = new RequestsInterface(activity.getApplicationContext())
                    .uploadImageToServer(currentUser, encodedImage, fileName);

            if (sr != null) {
                if (sr.isSuccess()) {
                    Log.d(TAG, "Image was uploaded successfully to server");
                    callbacks.onSuccessCallback(sr.getResult());
                } else {
                    Log.d(TAG, "Image was not uploaded to server. Reason: " + sr.getErrorMsg());
                    callbacks.onFailCallback(sr.getErrorMsg());
                }
            } else {
                Log.d(TAG, "Failed to upload the image to server, server response is null");
                callbacks.onFailCallback("Failed to upload the image to server, server response is null");
            }
        }

    }

    private interface MyCallback {
        void onSuccessCallback(Picture pic);

        void onFailCallback(String err);
    }

}
