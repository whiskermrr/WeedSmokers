package com.wiktorwolski.mrr.mobile_programming_final_project;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;


public class UserProfileFragment extends Fragment implements FragmentManager.OnBackStackChangedListener, View.OnClickListener {

    TextView tUserFullName;
    TextView tUserEmail;
    EditText etUserProfilelOldPassword;
    EditText etUserProfilelNewPassword;
    ImageView imageUserAvatar;
    Button bChangePassword;
    UserHandler userHandler;
    SharedPreferences sharedPreferences;
    DbBitmapUtility dbBitmapUtility;
    private int userID;

    private static final int RESULT_LOAD_IMAGE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tUserFullName = (TextView) getActivity().findViewById(R.id.tUserFullName);
        tUserEmail = (TextView) getActivity().findViewById(R.id.tUserEmail);
        etUserProfilelOldPassword = (EditText) getActivity().findViewById(R.id.etUserProfilelOldPassword);
        etUserProfilelNewPassword = (EditText) getActivity().findViewById(R.id.etUserProfilelNewPassword);
        bChangePassword = (Button) getActivity().findViewById(R.id.bChangePassword);
        imageUserAvatar = (ImageView) getActivity().findViewById(R.id.imageUserAvatar);

        imageUserAvatar.setOnClickListener(this);

        bChangePassword.setOnClickListener(this);

        userHandler = new UserHandler(getActivity(), null, null, 1);

        sharedPreferences = getActivity().getSharedPreferences(LoginActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        userID = sharedPreferences.getInt(LoginActivity.USER_ID, -1);

        dbBitmapUtility = new DbBitmapUtility();

        Cursor cursor = userHandler.getCursorOfLoggedUser(userID);

        if(cursor != null) {

            cursor.moveToFirst();
            tUserFullName.setText(cursor.getString(cursor.getColumnIndexOrThrow("firstname")) + " " +
            cursor.getString(cursor.getColumnIndexOrThrow("lastname")));
            tUserEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            cursor.close();
            setUserAvatarUsingImageInDatabase();
        }
    }

    public void changePassword() {

        if(userHandler.changePassword(etUserProfilelOldPassword.getText().toString(), etUserProfilelNewPassword.getText().toString(), userID)) {

            Toast.makeText(getActivity(), "Password Changed!", Toast.LENGTH_SHORT).show();
            etUserProfilelOldPassword.setText("");
            etUserProfilelNewPassword.setText("");
        }
        else {

            Toast.makeText(getActivity(), "Wrong old password!", Toast.LENGTH_SHORT).show();
            etUserProfilelOldPassword.setText("");
            etUserProfilelNewPassword.setText("");
        }
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            case R.id.bChangePassword:
                changePassword();
                break;

            case R.id.imageUserAvatar:
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, RESULT_LOAD_IMAGE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();
            imageUserAvatar.setImageURI(selectedImage);
            addImageToDatabase();
        }

    }

    @Override
    public void onBackStackChanged() {

    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }


    public void addImageToDatabase() {

        Bitmap image = ((BitmapDrawable) imageUserAvatar.getDrawable()).getBitmap();

        byte[] imageByte = dbBitmapUtility.getBytes(image);
        userHandler.addImage(imageByte);
    }

    public boolean isBitArrayEmpty(byte[] array) {

        for(byte b: array) {

            if(b != 0) {
                return false;
            }
        }

        return true;
    }

    public void setUserAvatarUsingImageInDatabase() {

        byte[] imageByte = userHandler.getUserImage(userID);

        if(!isBitArrayEmpty(imageByte)) {

            Bitmap image = dbBitmapUtility.getImage(imageByte);
            imageUserAvatar.setImageBitmap(image);
        }

        else {

            imageUserAvatar.setImageResource(R.drawable.myavatar);
        }
    }

}
