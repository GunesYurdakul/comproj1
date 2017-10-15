package com.example.gunesyurdakul.cp1v2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import android.hardware.fingerprint.FingerprintManager;


public class MainActivity extends AppCompatActivity {

    Singleton singleton =Singleton.getSingleton();
    public static final String EXTRA_MESSAGE = "com.example.gunesyurdakul.myapplication.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set view
        setContentView(R.layout.activity_main);

        readFile();//read users from file

        writeFile();
        LoginFragment fragment = new LoginFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, fragment).commit();
        transaction.addToBackStack("addemployee");
        transaction.commit();

    }

    //Read json file and save existing users to singleton's user map
    public void readFile(){
        try {
            Gson rson=new Gson();
            Reader reader = new FileReader(getFilesDir()+ "/user.json");
            rson = new GsonBuilder().create();
            singleton.userMap=rson.fromJson(reader,new TypeToken<Map<String,User>>(){}.getType());
            System.out.println(singleton.userMap);


            reader.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //Write users to json file which is stored in phone
    public void writeFile(){
        try {
            Writer writer = new FileWriter(getFilesDir()+ "/user.json");
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

}
