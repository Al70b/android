package com.al70b.core.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.DisplayPictureActivity;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.fragments.Dialogs.PleaseWaitAlert;
import com.al70b.core.fragments.Dialogs.QuestionAlert;
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

    private static boolean fullCapacity;

    private UserHomeActivity activity;
    private CurrentUser user;

    // grid view and image adapter
    private GridView gridView;
    private ImageAdapter adapter;

    // to hold profile image
    private ImageView imgViewProfile;

    private List<Picture> listOfPictures;

    private Picture profilePicture;

    // File for captured photo
    private File photoFile;

    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();

        activity = UserHomeActivity.getUserHomeActivity();

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
            user = ((MyApplication)getActivity().getApplication()).getCurrentUser();

            // get user photos
            listOfPictures = user.getPicturesList();

            updateCapacityFlag();

            // get user profile picture
            profilePicture = user.getProfilePicture();

            // create adapter and set it to grid view
            adapter = new ImageAdapter(activity, listOfPictures);

            gridView.setAdapter(adapter);
        }

        if (profilePicture != null)
            // get user profile picture
            Glide.with(activity)
                    .load(profilePicture.getThumbnailFullPath())
                    .placeholder(R.drawable.default_user_photo)
                    .into(imgViewProfile);


        imgViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((MyApplication)getActivity().getApplication()).getCurrentUser().isProfilePictureSet())
                    return;

                activity.toUserData = true;

                Intent intent = new Intent(activity, DisplayPictureActivity.class);
                intent.putExtra("DisplayPicture.image", profilePicture);
                startActivity(intent);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Picture pic = (Picture) adapter.getItem(i);

                activity.toUserData = true;

                Intent intent = new Intent(activity, DisplayPictureActivity.class);
                intent.putExtra("DisplayPicture.image", pic);
                startActivity(intent);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder
                        .setItems(R.array.picture_commands, new DialogListClickListener(i))
                        .create()
                        .show();

                return true;
            }
        });

        return viewGroup;
    }


    @Override
    public void onStart() {
        super.onStart();

        Log.d("A7%StartFragment-Pictur", "UserDataPicturesFragment: " + this.getId());
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("A7%PauseFragment-Pictur", "UserDataPicturesFragment: " + this.getId());
    }

    @Override
    public void onStop() {
        super.onStop();

        fullCapacity = false;
        Log.d("A7%StopFragment-Pictur", "UserDataPicturesFragment: " + this.getId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("A7%DestroyFragme-Pictur", "UserDataPicturesFragment: " + this.getId());
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
            if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
                menu.findItem(R.id.menu_item_user_picture_capture).setEnabled(false);
            else
                menu.findItem(R.id.menu_item_user_picture_capture).setEnabled(true);

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
                        Log.d("TakingPictureIO", ex.toString());
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));

                        // to return to my pictures
                        activity.toUserData = true;

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

        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE && resultCode == UserHomeActivity.RESULT_OK) {
            // prepare picture for upload
            prepareForPictureUpload(fileName, photoFile.getAbsolutePath());
        } else if (requestCode == REQUEST_CODE_GALLERY_PICKER && resultCode == UserHomeActivity.RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            // Get the cursor
            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imgPath = cursor.getString(columnIndex);
            cursor.close();


            if (imgPath != null) {
                prepareForPictureUpload(fileName, imgPath);
            }

        }
    }

    private void prepareForPictureUpload(final String fileName, final String picturePath) {

        final PleaseWaitAlert alert = new PleaseWaitAlert(getActivity(), R.string.uploading_your_photo_to_server);
        alert.show();

        // thread to upload picture to server
        new Thread(new Runnable() {

            String messageToDisplay;

            @Override
            public void run() {
                encodeImageToString();
            }

            public void encodeImageToString() {
                BitmapFactory.Options options;
                options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                Bitmap bitmap = BitmapFactory.decodeFile(picturePath,
                        options);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream);
                byte[] byte_arr = stream.toByteArray();

                // Encode Image to String
                String encodedString = Base64.encodeToString(byte_arr, 0);

                // Encode Image to String
                final ServerResponse<Picture> sr = new RequestsInterface(activity.getApplicationContext())
                        .uploadImageToServer(user, encodedString, fileName);

                if (sr != null && sr.isSuccess()) {
                    final Picture pic = sr.getResult();

                    // add picture to list of pictures for this user
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
                        }
                    });

                    messageToDisplay = activity.getString(R.string.uploading_photo_was_successfull);
                } else {
                    messageToDisplay = activity.getString(R.string.uploading_photo_was_failure);
                    // dismiss the alert dialog
                    alert.dismiss();
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, messageToDisplay, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void promptForSetAsProfilePicture(final Picture pic) {
        final QuestionAlert alert = new QuestionAlert(activity, activity.getResources().getString(R.string.question)
                , activity.getResources().getString(R.string.set_as_profile_picture), -1, R.string.yes, R.string.no);

        alert.show();

        alert.setPositiveButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {

                    Bitmap bitmap;

                    @Override
                    public void run() {

                        try {
                            RequestsInterface requestsInterface = new RequestsInterface(activity);

                            final ServerResponse<Boolean> sr = requestsInterface.setProfilePicture(user, pic.getId());

                            if (sr.isSuccess() && sr.getResult()) {
                                try {
                                    bitmap = Glide.with(activity).
                                            load(pic.getThumbnailFullPath()).asBitmap().
                                            into(-1, -1).get();

                                } catch (Exception ex) {
                                    Log.d("ExceptionProfileSet", ex.toString());
                                }

                                if (bitmap != null) {
                                    user.setProfilePicture(pic.getId());

                                    profilePicture = user.getProfilePicture();

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // set profile picture in drawer and up in the page
                                            imgViewProfile.setImageBitmap(bitmap);

                                            imgViewProfile.invalidate();

                                            //activity.updateProfilePicture(bitmap);

                                            Toast.makeText(activity, activity.getString(R.string.profile_picture_change_successfully), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, sr.getErrorMsg(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception ex) {
                        }

                        alert.dismiss();
                    }
                }).start();
            }
        });

        alert.setNegativeButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });


    }

    private void updateCapacityFlag() {
        if (listOfPictures.size() >= AppConstants.MAX_NUMBER_OF_PICTURES_PER_USER)
            fullCapacity = true;
        else
            fullCapacity = false;
    }

    private class DialogListClickListener implements DialogInterface.OnClickListener {

        private int position;

        public DialogListClickListener(int position) {
            this.position = position;
        }

        public void onClick(DialogInterface dialog, int which) {
            // set as profile picture
            final Picture pic = listOfPictures.get(position);

            final RequestsInterface requestsInterface = new RequestsInterface(activity);

            switch (which) {    // which command was chosen
                case 0:
                    new Thread(new Runnable() {

                        Bitmap bitmap;

                        @Override
                        public void run() {

                            final ServerResponse<Boolean> sr = requestsInterface.setProfilePicture(user, pic.getId());

                            if (sr.isSuccess() && sr.getResult()) {
                                try {
                                    bitmap = Glide.with(activity).
                                            load(pic.getThumbnailFullPath()).
                                            asBitmap().into(-1, -1).get();

                                } catch (Exception ex) {
                                }

                                if (bitmap != null) {
                                    user.setProfilePicture(pic.getId());

                                    profilePicture = user.getProfilePicture();

                                    gridView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // set profile picture in drawer and up in the page
                                            imgViewProfile.setImageBitmap(bitmap);

                                            imgViewProfile.invalidate();

                                            //activity.updateProfilePicture(bitmap);

                                            Toast.makeText(activity, activity.getString(R.string.profile_picture_change_successfully), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                gridView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, activity.getString(R.string.profile_picture_change_failed), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).start();

                    break;
                case 1:
                    // delete picture
                    new Thread(new Runnable() {

                        @Override
                        public void run() {

                            final ServerResponse<Boolean> sr = requestsInterface.deletePhoto(user, pic.getId());

                            if (sr.isSuccess() && sr.getResult()) {
                                gridView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // if delete picture is profile picture
                                        if (profilePicture != null && profilePicture.getId() == pic.getId()) {
                                            imgViewProfile.setImageResource(R.drawable.default_user_photo);
                                            imgViewProfile.invalidate();
                                            //activity.updateProfilePicture(R.drawable.default_user_photo);
                                            user.removeProfilePicture();
                                        }

                                        // remove photo
                                        listOfPictures.remove(pic);

                                        updateCapacityFlag();

                                        adapter.notifyDataSetChanged();

                                        Toast.makeText(activity, activity.getString(R.string.picture_was_removed), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                gridView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, activity.getString(R.string.profile_picture_delete_failed), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).start();

                    break;
                default:
            }
        }
    }

    private class ImageAdapter extends BaseAdapter {
        private Context context;
        private List<Picture> listOfPictures;
        private int dp110;

        public ImageAdapter(Context context, List<Picture> listOfPictures) {
            this.listOfPictures = listOfPictures;
            this.context = context.getApplicationContext();
            dp110 = (int) (110 * context.getResources().getDisplayMetrics().density);
        }

        public int getCount() {
            return listOfPictures.size();
        }

        public Object getItem(int position) {
            return listOfPictures.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(context);
                imageView.setImageResource(R.drawable.default_user_photo);
                imageView.setLayoutParams(new GridView.LayoutParams(dp110, dp110));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            Glide.with(activity)
                    .load(listOfPictures.get(position).getThumbnailFullPath())
                    .fitCenter()
                    .centerCrop()
                    .placeholder(R.drawable.default_user_photo)
                    .into(imageView);

            return imageView;
        }

        @Override
        public void notifyDataSetChanged() {
            Log.d("listSize", listOfPictures.size() + "");
            super.notifyDataSetChanged();
        }
    }
}
