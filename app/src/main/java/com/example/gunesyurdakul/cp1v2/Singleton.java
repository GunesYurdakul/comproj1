package com.example.gunesyurdakul.cp1v2;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gunesyurdakul on 01/10/2017.
 */

//Singleton class enables that there is only one object is created from this class in the lifetime of the app
public class Singleton {
    private static Singleton singleton = new Singleton();
    private Singleton(){}
    User currentUser;
    boolean unsuccesfulLogin;
    Map<String,User> userMap=new HashMap<String, User>();//User Map storing existing users
    public static Singleton getSingleton() {

        return singleton;
    }
}
