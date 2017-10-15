package com.example.gunesyurdakul.cp1v2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class UserFragment extends Fragment {
    Singleton singleton =Singleton.getSingleton();


    public UserFragment() {
        // Required empty public constructor
    }

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user, container, false);
        //Views
        TextView userName = view.findViewById(R.id.userName);
        TextView name = view.findViewById(R.id.name);
        ImageView pp = view.findViewById(R.id.profilePicture);
        Button logout = view.findViewById(R.id.logout);
        TextView password = view.findViewById(R.id.password);
        //Users information are set to corresponding views
        name.setText(singleton.currentUser.name + " " + singleton.currentUser.surname);
        userName.setText(singleton.currentUser.user_name);
        password.setText(singleton.currentUser.password);
        //Set profile picture to image view
        if (singleton.currentUser.profilePicture != null) {
            try{
                Bitmap src = BitmapFactory.decodeByteArray(singleton.currentUser.profilePicture, 0, singleton.currentUser.profilePicture.length);
                RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(),src);
                dr.setCornerRadius(200);
                pp.setImageDrawable(dr);
            }catch (Exception e){
                Log.e("picture","error");
            }
        }
        //Log put button
        logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Redirect user to login page
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }


}
