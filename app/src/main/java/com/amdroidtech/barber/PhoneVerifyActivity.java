package com.amdroidtech.barber;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class PhoneVerifyActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";


    //variables section
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final int RC_SIGN_IN = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verify);
        ButterKnife.bind(this);

        //hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //make full screen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        //initiate firebase auth
        initiateFirebaseAuth();

    }

    @Override
    public void onStart() {
        super.onStart();
        //attach firebase listener
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        //remove firebase listener
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * firebase auth logic goes here
     */
    private void initiateFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (user.getPhoneNumber()!=null && !user.getPhoneNumber().equals("")){
                        // User is signed in
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                        moveToNextScreen();

                    }else
                        startVerify();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    startVerify();
                }
                // ...
            }
        };
    }

    @OnClick(R.id.btn_login)
    void login() {
        startVerify();
    }

    private void startVerify(){
        // not signed in
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()
                                ))
                        .build(),
                RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                moveToNextScreen();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.e("Login","Login canceled by User");
                    Toast.makeText(PhoneVerifyActivity.this,"Login canceled by User!",Toast.LENGTH_SHORT)
                       .show();
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {

                    Toast.makeText(PhoneVerifyActivity.this,"No Internet Connection!",Toast.LENGTH_SHORT)
                            .show();
                    Log.e("Login","No Internet Connection");
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.e("Login","Unknown Error");
                    Toast.makeText(PhoneVerifyActivity.this,"Error Occurred!",Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
            }
            Log.e("Login","Unknown sign in response");
        }
    }

    /**
     * move to next activity and finish current activity
     * extra code as follow
     * 0 new pin
     * 1 change pin
     * 2 enter pin to login
     */
    private void moveToNextScreen() {

            Intent intent = new Intent(this,CustomerActivty.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish(); // Call once you redirect to another activity
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

}
