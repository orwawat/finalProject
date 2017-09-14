package com.amdroidtech.barber.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;



/**
 * this is helper Singleton class to communicate with  shared preference
 */
public class SharedPrefUtilities {

    //constants
    private static String USER_ID = "userID";
    private static String USER_PHONE = "userphone";
    private static String USER_IMAGE = "userImage";
    private static String USER_EMAIL = "userEmail";
    private static String USER_RULE = "userRule";
    private static String PIN_ENABLED = "pinEnabled";
    private static String PIN_PASSWORD = "pinPassword";
    private static String USER_PUSH_ID = "user_pushID";
    private static String CUST_PUSH_ID = "cust_pushID";
    SharedPreferences preferences;

    public  SharedPrefUtilities(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * clear data from shared pref
     */
    public void clearData(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     *
     * @param userID
     * add user id to shared preference
     */
    public void setUserID(String userID) {
        preferences.edit().putString(USER_ID, userID).apply();
    }

    /**
     * @return user id from shared preference
     */
    public String getUserID() {
        return preferences.getString(USER_ID, null);
    }

    /**
     *
     * @param userphone
     * add user name to shared preference
     */
    public void setUserPhone(String userphone) {
        preferences.edit().putString(USER_PHONE, userphone).apply();
    }

    /**
     * @return user name from shared preference
     */
    public String getUserPhone() {
        return preferences.getString(USER_PHONE, null);
    }

    /**
     *
     * @param userEmail
     * add user email to shared preference
     */
    public void setUserEmail(String userEmail) {
        preferences.edit().putString(USER_EMAIL, userEmail).apply();
    }

    /**
     * @return user email from shared preference
     */
    public String getUserEmail() {
        return preferences.getString(USER_EMAIL, null);
    }

    /**
     *
     * @param userImage
     * add user image to shared preference
     */
    public void setUserImage(String userImage) {
        preferences.edit().putString(USER_IMAGE, userImage).apply();
    }

    /**
     * @return user image from shared preference
     */
    public String getUserImage() {
        return preferences.getString(USER_IMAGE, null);
    }

    /**
     *
     * @param pinEnabled
     * add pinEnabled to shared preference
     */
    public void setPinEnabled(boolean pinEnabled) {
        preferences.edit().putBoolean(PIN_ENABLED, pinEnabled).apply();
    }

    /**
     * @return pinEnabled from shared preference
     */
    public boolean getPinEnabled() {
        return preferences.getBoolean(PIN_ENABLED, false);
    }

    /**
     *
     * @param userRole
     * add user rule to shared preference
     */
    public void setUserRule(int userRole) {
        preferences.edit().putInt(USER_RULE, userRole).apply();
    }

    /**
     * @return user rule from shared preference
     */
    public int getUserRule() {
        return preferences.getInt(USER_RULE, 0);
    }

    /**
     *
     * @param pinPassword
     * add pinPassword to shared preference
     */
    public void setPinPassword(String pinPassword) {
        preferences.edit().putString(PIN_PASSWORD, pinPassword).apply();
    }

    /**
     * @return pinPassword from shared preference
     */
    public String getPinPassword() {
        return preferences.getString(PIN_PASSWORD, null);
    }

    /**
     *
     * @param pushID
     * add push id to shared preference
     */
    public void setPushIDCust(String pushID) {
        preferences.edit().putString(CUST_PUSH_ID, pushID).apply();
    }

    /**
     * @return push id from shared preference
     */
    public String getPushIDCust() {
        return preferences.getString(CUST_PUSH_ID, null);
    }

    /**
     *
     * @param pushID
     * add push id to shared preference
     */
    public void setPushIDUser(String pushID) {
        preferences.edit().putString(USER_PUSH_ID, pushID).apply();
    }

    /**
     * @return push id from shared preference
     */
    public String getPushIDUser() {
        return preferences.getString(USER_PUSH_ID, null);
    }


}
