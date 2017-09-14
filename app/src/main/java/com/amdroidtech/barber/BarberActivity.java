package com.amdroidtech.barber;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amdroidtech.barber.Adapters.AppointsAdapter;
import com.amdroidtech.barber.Utilities.FirebaseUtil;
import com.amdroidtech.barber.Utilities.SharedPrefUtilities;
import com.amdroidtech.barber.Utilities.Utils;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BarberActivity extends AppCompatActivity {

    private static final String TAG = "BarberActivity";
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.appoints_recycler_view)
    RecyclerView mRecyclerView;

    private SharedPrefUtilities mSharedPrefUtilities;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private AppointsAdapter mAppointsAdapter;
    private List<Appoint> appointList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barber);
        ButterKnife.bind(this);
       //set toolbar
        setSupportActionBar(mToolbar);
        //get instance from shared pref utility
        mSharedPrefUtilities = new SharedPrefUtilities(this);
        //set toolbar title
        getSupportActionBar().setTitle("Welcome "+mSharedPrefUtilities.getUserEmail().substring(0,mSharedPrefUtilities.getUserEmail().indexOf('@')));

        //get instance from firebase auth
        mAuth = FirebaseAuth.getInstance();

        setProgress();

        //setup feed
        setupFeed();

        //get appoints
        getAppoins();
    }

    private void getAppoins() {

        //check internet first
        if(Utils.checkInternet(this)){

            FirebaseUtil.getAppointsRef().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null){
                        appointList.clear();

                        for (DataSnapshot appointSnap: dataSnapshot.getChildren()) {
                            Appoint appoint = appointSnap.getValue(Appoint.class);
                            try {
                                String datestr = appoint.date.substring(0,19)+" "+appoint.date.substring(appoint.date.length() - 4);
                                Date appointDate = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy").parse(datestr);
                                if ( !appointDate.before(getCurrentDate()) ){
                                    appointList.add(appoint);
                                }else {
                                    //try to remove appoint from firebase
                                    FirebaseUtil.getAppointsRef().child(appoint.key).removeValue();
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                        }
                        Collections.sort(appointList);
                        mAppointsAdapter.addAll(appointList);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                //    Toast.makeText(BarberActivity.this,"Error Occurred",Toast.LENGTH_LONG).show();
                }
            });

        }else{
            Toast.makeText(BarberActivity.this,"Error Occurred",Toast.LENGTH_LONG).show();
        }

    }

    private void setupFeed() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        mRecyclerView.setLayoutManager(linearLayoutManager);

        appointList = new ArrayList<>();
        mAppointsAdapter = new AppointsAdapter(this,appointList);
        mRecyclerView.setAdapter(mAppointsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_logout:
                LogOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * logout from current user
     */
    private void LogOut() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.log_out_action));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //remove firebase auth
                mAuth.signOut();
                mSharedPrefUtilities.clearData();
                intentToWelcomeScreen();
            }
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), null);
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * go to welcome screen and clear current activity from stack
     */
    private void intentToWelcomeScreen(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activity
    }

    /**
     * on click fab to add new appointment
     */
    @OnClick(R.id.fab)
    void addAppoint(){

        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, 3);
        dt = c.getTime();

        new SingleDateAndTimePickerDialog.Builder(this)
               // .bottomSheet()
              //  .curved()
                .minutesStep(30)
                .mustBeOnFuture()
                .maxDateRange(dt)
                //.displayHours(false)
                //.displayMinutes(false)

                .displayListener(new SingleDateAndTimePickerDialog.DisplayListener() {
                    @Override
                    public void onDisplayed(SingleDateAndTimePicker picker) {
                        //retrieve the SingleDateAndTimePicker
                    }
                })

                .title("pick appointment")
                .listener(new SingleDateAndTimePickerDialog.Listener() {
                    @Override
                    public void onDateSelected(Date date) {
                        //try to add this date
                        //checl if date after current date
                        if (!date.before(getCurrentDate())){
                            addAppointToFirebase(date);
                        }else {
                            Toast.makeText(BarberActivity.this,"Date is passed",Toast.LENGTH_LONG).show();
                        }

                    }
                }).display();
    }

    private Date getCurrentDate(){
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
       return c.getTime();
    }

    /**
     * push date t firebase
     * @param date
     */
    private void addAppointToFirebase(final Date date) {
        final StringBuilder myDate = new StringBuilder(date.toString());
        myDate.setCharAt(17, '0');
        myDate.setCharAt(18, '0');
        progressDialog.show();
        if (Utils.checkInternet(this)){
            FirebaseUtil.getAppointsRef().orderByChild("date").equalTo(myDate.toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        progressDialog.dismiss();
                        Toast.makeText(BarberActivity.this,"Appointment Already Exist",Toast.LENGTH_LONG).show();
                    } else {
                        saveNewDate(myDate.toString());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressDialog.dismiss();
                    Toast.makeText(BarberActivity.this,"Error Occurred",Toast.LENGTH_LONG).show();
                }
            });
        }else {
            progressDialog.dismiss();
            Toast.makeText(BarberActivity.this,"Error Occurred",Toast.LENGTH_LONG).show();
        }


    }

    /**
     * save new date to firebase
     */
    private void saveNewDate(String date) {

        String pushId =  FirebaseInstanceId.getInstance().getToken();
        Appoint appoint = new Appoint(date,  FirebaseUtil.getAppointsRef().push().getKey() , pushId);

        FirebaseUtil.getAppointsRef().child(appoint.key).setValue(appoint).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(BarberActivity.this,"Appoint Added",Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(BarberActivity.this,"Error Occurred",Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    /**
     * setup progress dialog of loading indicator
     */
    private void setProgress(){
        progressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading.....");
    }
}
