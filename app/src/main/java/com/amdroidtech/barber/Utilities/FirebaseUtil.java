package com.amdroidtech.barber.Utilities;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {
    public static String profileImages = "profileImages";

    public static DatabaseReference getBaseRef() {
//         FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        return FirebaseDatabase.getInstance().getReference();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static DatabaseReference getPushRef() {
        return getBaseRef().child(FirebaseUtil.getCurrentUserId()).child("push_id");
    }

    public static DatabaseReference getAppointsRef() {
        return getBaseRef().child("appoints");
    }

}
