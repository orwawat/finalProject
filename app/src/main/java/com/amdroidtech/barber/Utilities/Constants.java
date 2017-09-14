package com.amdroidtech.barber.Utilities;


import java.util.regex.Pattern;

/**
 * this is class represent constant in app
 */
public class Constants {

    //on activity result codes
    public static int RC_GOOGLE_SIGN_IN = 1;

    public static String BARBER = "barber";
    public static String CUSTOMER = "customer";

    //validation patterns
    public static final Pattern PASSWORD_PATTERN = Pattern
            .compile("[a-zA-Z0-9]{1,250}");
    public static final Pattern EMAIL_PATTERN = Pattern
            .compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    public static final Pattern PHONE_PATTERN = Pattern
            .compile("[0-9]{1,250}");

    /*
      Logging flag
     */
    public static final boolean LOGGING = false;



}
