package com.example.gunesyurdakul.cp1v2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.VerifyResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;


public class BiometricControl extends Fragment {
    Bitmap tocheck,original;
    boolean status0=false,status1=false;
    UUID mFaceId0;                          //faceIds returned by detection
    UUID mFaceId1;
    int counter = 0;                        //login attempt counter
    static final int CAMERA_REQUEST = 1888;
    FaceServiceClient faceServiceClient;
    Singleton singleton =Singleton.getSingleton();
    View view;
    TextView warning;
    public BiometricControl() {
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
        view = inflater.inflate(R.layout.fragment_biometric_control, container, false);
        //views on main view
        ImageView bio = view.findViewById(R.id.bio);
        warning = (TextView) view.findViewById(R.id.warning);
        //when fragment starts camera opens
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
        //After photo is taken if verification is unsuccessful, the user can click on the prev. photo to try again
        //which will start camera again
        bio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("IMAGE","IMAGE");
        ImageView imageView = view.findViewById(R.id.bio);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            //when photo is taken, phpto and users actual photo should be verified
            tocheck = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(tocheck);
            original = BitmapFactory.decodeByteArray(singleton.currentUser.profilePicture, 0, singleton.currentUser.profilePicture.length);
            detect(tocheck,0);
            detect(original,1);
        }

    }

    private void detect(final Bitmap imageBitmap, final int index)
    {
        warning.setText("Verifying...");
        if(index==0)
            status0=false ;
        else
            status1=false;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            //Detection returns Face type object
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    null           // returnFaceAttributes: a string like "age, gender"
                            );
                            //If there is no person in the photo
                            if (result == null)
                            {
                                publishProgress("Detection Finished. Nothing detected");
                                Log.d("Face","Detection Finished. Nothing Detected");
                                if(index==0)
                                    status0=false ;
                                else
                                    status1=false;
                                return null;
                            }
                            //If multiple faces detected
                            else if(result.length>1)
                            {
                                Log.d("Face",String.format("%d face(s) detected!", result.length));
                            }
                            publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));
                            Log.d("Face", String.format("Detection Finished. %d face(s) detected", result.length));
                            if(result.length==1){
                                //If there is only one face
                                if(index==0) {
                                    mFaceId0 = result[0].faceId;
                                    status0=true;
                                }
                                else{
                                    mFaceId1 = result[0].faceId ;
                                    status1=true;
                                }
                            }

                            return result;
                        } catch (Exception e) {
                            //If there is a failure exception is thrown
                            publishProgress("Detection failed");
                            Log.d("Face","Detection failed");
                            Log.d("Face",e.getMessage());
                            return null;
                        }
                    }
                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        if(status0 && status1){
                            //If there is only one face in both photos,  verification task is called
                            new VerificationTask(mFaceId0, mFaceId1).execute();
                        }
                        else if(!status0) {
                            //In every failed login counter is incremented
                            counter++;
                            //If counter reaches 3 the user is directed to login page
                            if(counter==3){
                                singleton.unsuccesfulLogin=true;
                                Intent intent = new Intent(getContext(),MainActivity.class);
                                startActivity(intent);
                            }
                            warning.setText("No match, try again!");
                        }


                    }
                };
        detectTask.execute(inputStream);
    }

    //Verification Task
    private class VerificationTask extends AsyncTask<Void, String, VerifyResult> {
        // The ids of faces
        VerificationTask (UUID faceId0, UUID faceId1) {
            mFaceId0 = faceId0;
            mFaceId1 = faceId1;
        }

        @Override
        protected VerifyResult doInBackground(Void... params) {
            try{
                publishProgress("Verifying...");
                // verification starts
                return faceServiceClient.verify(mFaceId0, mFaceId1);
            }  catch (Exception e) {
                publishProgress(e.getMessage());
                Log.d("Verification",e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            Log.d("Verification","Request: Verifying face " + mFaceId0 + " and face " + mFaceId1);
        }

        @Override
        protected void onProgressUpdate(String... progress) {

        }

        @Override
        protected void onPostExecute(VerifyResult result) {
            if (result != null) {
                Log.d("Verification","Response: Success. Face " + mFaceId0 + " and face "
                        + mFaceId1 + (result.isIdentical ? " " : " don't ")
                        + "belong to the same person");
            }
            if(result.isIdentical){
                //If faces belong to same person the user is directed to logged in user page
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                tocheck.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                //The photo of user is updated, so that the photo always stays up to date
                singleton.currentUser.profilePicture = outputStream.toByteArray();
                gotouserFragment();
            }
            else{
                Log.d("Verification","No match");
                warning.setText("No match, try again!");
                //In every failed login counter is incremented
                counter++;
                //If counter reaches 3 the user is directed to login page
                if(counter==3){
                    singleton.unsuccesfulLogin=true;
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                }
            }

        }
    }

    private void gotouserFragment(){

        UserFragment page = new UserFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Bundle args = new Bundle();
        page.setArguments(args);
        ft.replace(R.id.fragment, page);
        ft.addToBackStack("addemployee");
        ft.commit();


    }
}


