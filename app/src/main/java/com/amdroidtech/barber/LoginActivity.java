package com.amdroidtech.barber;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.amdroidtech.barber.Utilities.SharedPrefUtilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.amdroidtech.barber.Utilities.Constants.DATABASE_NAME;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    @Bind(R.id.input_email)
    EditText emailText;
    @Bind(R.id.input_password)
    EditText passwordText;
    @Bind(R.id.btn_login)
    Button loginButton;
    @Bind(R.id.password_layout)
    TextInputLayout passwordLayout;
    @Bind(R.id.email_layout)
    TextInputLayout emailLayout;
    @Bind(R.id.link_forget_password)
    TextView forgetPasswordText;

    //variables section
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDataRef;

    private SharedPrefUtilities mSharedPrefUtilities;

    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //make full screen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);



        //initiate firebase database
        initiateFirebaseDatabase();


        //initiate firebase auth
        initiateFirebaseAuth();


        //get instance from shared pref utility
        mSharedPrefUtilities = new SharedPrefUtilities(this);
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
     * firebase database logic goes here
     */
    private void initiateFirebaseDatabase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDataRef = mFirebaseDatabase.getReference(DATABASE_NAME);
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
                    if (user.getEmail()!=null && !user.getEmail().equals("")){
                        // User is signed in
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                        // Name, email address, and profile photo Url
                        String name = user.getDisplayName();
                        String email = user.getEmail();
                        Uri photoUrl = user.getPhotoUrl();

                        // The user's ID, unique to the Firebase project. Do NOT use this value to
                        // authenticate with your backend server, if you have one. Use
                        // FirebaseUser.getToken() instead.
                        String uid = user.getUid();
                        //save user data to shared pref
                        mSharedPrefUtilities.setUserID(uid);
                        mSharedPrefUtilities.setUserEmail(email);

                        moveToNextScreen();

                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    /**
     * move to next activity and finish current activity
     * extra code as follow
     * 0 new pin
     * 1 change pin
     * 2 enter pin to login
     */
    private void moveToNextScreen() {
            mSharedPrefUtilities.setPinEnabled(false);
            Intent intent = new Intent(this,BarberActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish(); // Call once you redirect to another activity
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * show forget dialog to user
     */
    @OnClick(R.id.link_forget_password)
    void showForgetDialog() {

        new MaterialDialog.Builder(LoginActivity.this)
                .title("Reset Password!")
                .positiveText("Send")
                .negativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .theme(Theme.LIGHT)
                .cancelable(false)
                .positiveColorRes(R.color.colorPrimaryDark)
                .negativeColorRes(R.color.colorPrimaryDark)
                .buttonRippleColorRes(R.color.colorPrimaryDark)
                .content("Type your email and we will contact you shortly!")
                .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT)
                .autoDismiss(false)
                .input("Email Address", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(final MaterialDialog dialog, CharSequence input) {
                        //get email as string
                        String email = input.toString();
                        // check if email is valid
                        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            dialog.getInputEditText().setError("Wrong Email!");
                        }else{
                            //disable buttons
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                            dialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);
                            mAuth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Email sent.");
                                                dialog.dismiss();
                                                Toast.makeText(LoginActivity.this,"Check your email!",Toast.LENGTH_SHORT)
                                                        .show();
                                            }else{
                                                //enable buttons
                                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                                dialog.getActionButton(DialogAction.NEGATIVE).setEnabled(true);

                                                dialog.getInputEditText().setError("Wrong email address");
                                            }
                                        }
                                    });
                        }
                    }
                }).show();
    }

    @OnClick(R.id.btn_login)
    void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Logging.....");
        progressDialog.show();

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            onLoginFailed();
                            progressDialog.dismiss();
                        }else if(task.isSuccessful()){
                            moveToNextScreen();
                        }

                    }
                });

    }

    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "error on login!!", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("error on email address!");
            valid = false;
        } else {
            emailLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError("type password!!");
            valid = false;
        }else{
            passwordLayout.setError(null);
        }

        return valid;
    }

}
