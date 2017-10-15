package com.example.gunesyurdakul.cp1v2;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.hash.Hashing;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;

import java.nio.charset.Charset;


public class LoginFragment extends Fragment {
    Bitmap tocheck;

    static final int CAMERA_REQUEST = 1888;
    FaceServiceClient faceServiceClient;

    Singleton singleton = Singleton.getSingleton();
    View view;

    public LoginFragment() {
        // Required empty public constructor
    }


    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        faceServiceClient = new FaceServiceRestClient(getActivity().getResources().getString(R.string.endpoint), getActivity().getResources().getString(R.string.subscription_key));

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);
        //views
        final Button login = (Button) view.findViewById(R.id.sign_in);
        final TextView signUp = (TextView) view.findViewById(R.id.sign_up);
        final EditText idText = (EditText) view.findViewById(R.id.id_edit);
        final TextView warning = (TextView) view.findViewById(R.id.warning_login);
        final EditText pass = (EditText) view.findViewById(R.id.password);

        //If activity starts because of 3 unsuccesful login attempt the below warning will shown
        if(singleton.unsuccesfulLogin)
        {
            singleton.unsuccesfulLogin=false;
            warning.setText("3 unsuccessful login attempts");
        }
        //Sign in button click listener
        login.setOnClickListener(new View.OnClickListener() {
                                     public void onClick(View v) {
                                         //sign in constraints
                                         String id = idText.getText().toString();
                                         String password = pass.getText().toString();
                                         final String hashed = Hashing.sha256()
                                                 .hashString(password, Charset.forName("UTF-8"))
                                                 .toString();
                                         //if username field is blank
                                         if (id.length() == 0) {
                                             warning.setText("Username is blank!");
                                         }
                                         else {
                                            //User is found from user map using its username as key of the map
                                             User logging_in = singleton.userMap.get(id);
                                             //If password field is blank
                                             if (password.trim().length() == 0) {
                                                 warning.setText("Password is blank!");
                                             }
                                             //If there is no such username in existing users
                                             else if (logging_in == null) {
                                                 warning.setText("No such username exists!");
                                             }
                                             //if all conditions are satisfied
                                             else if (logging_in.password.equals(hashed)) {
                                                 singleton.currentUser = singleton.userMap.get(id);
                                                 BiometricControl biometricControl = new BiometricControl();
                                                 FragmentManager fm = getFragmentManager();
                                                 FragmentTransaction ft = fm.beginTransaction();
                                                 Bundle args = new Bundle();
                                                 biometricControl.setArguments(args);
                                                 ft.replace(R.id.fragment, biometricControl);
                                                 ft.addToBackStack("addemployee");
                                                 ft.commit();
                                                 warning.setText("Başarılı:)");
                                             } else if (!logging_in.password.equals(pass.getText().toString())) {
                                                 warning.setText("Password and Id do not match!");
                                             }
                                         }

                                     }

                                     ;
                                 }
        );
        //Sign up button click listener, if the user wants to register add new user fragment replaces the existing fragment
        signUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewUser userFragment = new addNewUser();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Bundle args = new Bundle();
                userFragment.setArguments(args);
                ft.replace(R.id.fragment, userFragment);
                ft.addToBackStack("addUser");
                ft.commit();
            }

            ;
        });
        return view;
    }


}