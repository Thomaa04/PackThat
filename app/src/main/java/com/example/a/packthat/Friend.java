package com.example.a.packthat;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Serializable;

/**
 * Friends of the user, if they have them
 */
public class Friend implements Serializable{
    public int friendId;
    public String friendName, email, profileImg;
    public Bitmap profileImageBitmap;

    public Friend(int friendId, String friendName, String email, String profileImg) {
        this.friendId = friendId;
        this.friendName = friendName;
        this.email = email;
        this.profileImg = profileImg;
        this.profileImageBitmap = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.default_profile);
    }
}
