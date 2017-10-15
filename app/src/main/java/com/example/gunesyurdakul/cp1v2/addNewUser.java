package com.example.gunesyurdakul.cp1v2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

public class addNewUser extends Fragment implements View.OnClickListener{
    FaceServiceClient faceServiceClient;                //For Face Api
    byte[] byteArray;                                   //profile photo
    Bitmap photo;                                       //Camera returns photo as bitmap
    boolean status;                                     //status of photo, it is equal to true if only one face is detected else false
    View view;                                          //view of fragment
    Singleton singleton =Singleton.getSingleton();      //Singleton
    Button signup;                                        //sign up button
    EditText name,surname,password,userName,email;      //views on add user page
    TextView warning;
    ImageView pp;
    static User newUser=new User();                     //Initialise new user object
    //used after receiving camera intent result to compare with that result
    private static final int CAMERA_REQUEST = 1888;

    public addNewUser() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        faceServiceClient = new FaceServiceRestClient(getActivity().getResources().getString(R.string.endpoint), getActivity().getResources().getString(R.string.subscription_key));
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_new_user, container, false);
        //set views
        warning = view.findViewById(R.id.warning);
        name = view.findViewById(R.id.name);
        surname = view.findViewById(R.id.surname);
        password = view.findViewById(R.id.password);
        signup=view.findViewById(R.id.addNewEmployee);
        userName=view.findViewById(R.id.id);
        pp=(ImageView)view.findViewById(R.id.profilePicture);
        email=view.findViewById(R.id.email);
        newUser.profilePicture=null;

        //sign up click listener
        signup.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(singleton.userMap.get(userName.getText().toString())!=null){
                    warning.setText("This id already exists!");
                }
                else if(name.getText().toString().trim().length()==0){
                    warning.setText("Name field is blank!");
                }
                else if(surname.getText().toString().trim().length()==0){
                    warning.setText("Surname field is blank!");
                }
                else if(email.getText().toString().trim().length()==0){
                    warning.setText("Email address field is blank!");
                }
                else if(password.getText().toString().trim().length()==0){
                    warning.setText("Password field is blank!");
                }
                else if(password.toString().length() < 6){
                    warning.setText("Password should be longer than 6 characters!");
                }
                else if(newUser.profilePicture==null){
                    warning.setText("Choose a profile picture");
                }
                else{
                    //If all conditions are satisfied the user can be added to users
                    newUser.name=name.getText().toString();
                    newUser.surname=surname.getText().toString();
                    String pass =password.getText().toString();
                    newUser.email=email.getText().toString();
                    Log.d("INFO","addTask");
                    //Password is first hashed, then the hashed version is stored.
                    final String hashed = Hashing.sha256()
                            .hashString(pass, Charset.forName("UTF-8"))
                            .toString();
                    newUser.password = hashed;
                    //Add user to usermap
                    singleton.userMap.put(userName.getText().toString(),newUser);
                    //transform into json and write to file
                    writeFile();
                    //Return to login page
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                }

            };
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //when the image view is clicked camera opens
        pp.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);

            }

        });
    }

    @Override
    public void onClick(View v) {}
    public void writeFile(){
        try {
            Writer writer = new FileWriter(getContext().getFilesDir()+ "/user.json");
            Gson gson=new Gson();
            gson = new GsonBuilder().create();
            gson.toJson(singleton.userMap, writer);
            String str=gson.toJson(singleton.userMap);
            System.out.println(str);
            writer.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("IMAGE","IMAGE");

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            //Photo taken by the user
            photo = (Bitmap) data.getExtras().get("data");
            pp.setImageBitmap(photo);
            Bitmap bmp = ((BitmapDrawable)pp.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            //The photo as byte array, since user type object has byte array type profile photo
            byteArray = stream.toByteArray();
            //detect function is called to check if the profile photo includes a person, only one
            detect(photo);

        }


    }

    private void detect(final Bitmap imageBitmap)
    {
        status=false;
        warning.setText("Checking profile picture...");
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
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
                                return null;
                            }
                            else if(result.length>1)
                            {
                                //If multiple faces detected
                                Log.d("Face",String.format("Detection Finished. %d face(s) detected. Multiple faces not allowed!", result.length));
                            }
                            if(result.length==1)
                                status=true;
                            publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));
                            Log.d("Face", String.format("Detection Finished. %d face(s) detected", result.length));

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
                        //Since detection is a asynck task the photo should be assigned to new user in the post execute method
                        if(status){
                            newUser.profilePicture=byteArray;
                            Bitmap src = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getActivity().getResources(),src);
                            dr.setCornerRadius(200);
                            pp.setImageDrawable(dr);
                            warning.setText("Succesful");
                        }
                        else
                            warning.setText("Multiple faces or no face detected!");

                    }
                };
        detectTask.execute(inputStream);
    }
}
