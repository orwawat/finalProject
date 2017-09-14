package com.amdroidtech.barber.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.amdroidtech.barber.Appoint;
import com.amdroidtech.barber.R;
import com.amdroidtech.barber.Utilities.FirebaseUtil;
import com.amdroidtech.barber.Utilities.SharedPrefUtilities;
import com.amdroidtech.barber.Widgets.FcmNotificationBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.amdroidtech.barber.Utilities.Constants.BARBER;

public class ReserveAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String TAG = "ReserveAdapter";
    private Context context;
    private List<Appoint> appointList;
    private SharedPrefUtilities mSharedPrefUtilities;
    private boolean should_run = false;

    public ReserveAdapter(Context context , List<Appoint> appointList) {
        this.context = context;
        this.appointList = appointList;
        mSharedPrefUtilities  = new SharedPrefUtilities(context);
    }

    public List<Appoint> getAppointsItems() {
        return appointList;
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.checkbox)
        CheckBox checkBox;

        @Bind(R.id.date_txt)
        TextView dateTxt;

        @Bind(R.id.appoint_item)
        CardView appointItem;

        public CardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.reserve_item,parent, false);
        RecyclerView.ViewHolder holder = null;
            holder = new CardViewHolder(view);
            final CardViewHolder cardHolder = (CardViewHolder)holder;

        cardHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isReserveBefore() || !cardHolder.checkBox.isChecked()){
                    cardHolder.checkBox.setEnabled(false);
                    final Appoint appoint = appointList.get(cardHolder.getAdapterPosition());
                    if (!cardHolder.checkBox.isChecked()){//so it's true and he want to cancel reservation
                        //  cardHolder.checkBox.setChecked(false);
                        cancelAppoint(appoint,cardHolder);
                    }else {//want to reserve
                        //     cardHolder.checkBox.setChecked(true);
                        reserveAppoint(appoint,cardHolder);
                    }
                }
                else {
                    cardHolder.checkBox.setChecked(false);
                    Toast.makeText(context,"You have already Appoint Reserved!!",Toast.LENGTH_LONG).show();
                }

            }
        });


        return holder;
    }

    private void reserveAppoint(final Appoint appoint, final CardViewHolder cardHolder) {

            Map<String, Object> updateValues = new HashMap<>();
            updateValues.put("cust_push_id", FirebaseInstanceId.getInstance().getToken());
            updateValues.put("phone", FirebaseUtil.getCurrentUser().getPhoneNumber());

            appoint.cust_push_id = FirebaseUtil.getCurrentUser().getPhoneNumber();
            appoint.phone = FirebaseUtil.getCurrentUser().getPhoneNumber();

            FirebaseUtil.getAppointsRef().child(appoint.key).updateChildren(
                    updateValues,
                    new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {

                            if (firebaseError != null) {
                                Toast.makeText(context,"Error Occurred",Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(context,"Appoint Reserved!!",Toast.LENGTH_LONG).show();
                                sendNotification(appoint);
                            }
                            cardHolder.checkBox.setEnabled(true);
                        }
                    });

    }

    private void cancelAppoint(final Appoint appoint, final CardViewHolder cardHolder) {
        //make cancel appoint
        FirebaseUtil.getAppointsRef().child(appoint.key).child("phone").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //send notification to customer if phone number assoicated
                    Toast.makeText(context,"canceled",Toast.LENGTH_LONG).show();
                    FirebaseUtil.getAppointsRef().child(appoint.key).child("cust_push_id").removeValue();
                    //send notification to user of canceling
                    sendCancelNotification(appoint);
                }else {


                    sendNotification(appoint);
                    Toast.makeText(context,"Error while cancel",Toast.LENGTH_LONG).show();
                }
                cardHolder.checkBox.setEnabled(true);
            }
        });


    }

    /**
     * check if current user reserve before
     * @return
     */
    private boolean isReserveBefore(){
        for (Appoint appoint:appointList){
            if (appoint.phone!=null){
                if (appoint.phone.equals(FirebaseUtil.getCurrentUser().getPhoneNumber()))
                    return true;
            }
        }
        return false;
    }

    private void sendNotification(Appoint appoint) {
        String username = FirebaseUtil.getCurrentUser().getPhoneNumber();
        String messageStr ="Appoint "+appoint.date +" has been reserved by "+username;
        String uid =FirebaseUtil.getCurrentUserId();
        String firebaseToken = appoint.cust_push_id;
        String receiverFirebaseToken = appoint.user_push_id;
        FcmNotificationBuilder.initialize()
                .title(username)
                .message(messageStr)
                .username(BARBER)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(receiverFirebaseToken)
                .send();
    }


    /**
     * try to send push notification to customer of caceling appoint
     * @param appoint
     */
    private void sendCancelNotification(Appoint appoint) {
        String username = FirebaseUtil.getCurrentUser().getPhoneNumber();
        String messageStr = "Appoint "+appoint.date +" has been canceled by "+username;
        String uid =FirebaseUtil.getCurrentUserId();
        String firebaseToken = appoint.cust_push_id;
        String receiverFirebaseToken = appoint.user_push_id;
        FcmNotificationBuilder.initialize()
                .title(username)
                .message(messageStr)
                .username(BARBER)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(receiverFirebaseToken)
                .send();
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final CardViewHolder cardViewHolder = (CardViewHolder) holder;

        Appoint appoint = appointList.get(cardViewHolder.getAdapterPosition());

        cardViewHolder.dateTxt.setText(appoint.date.substring(0,16));

        if (appoint.phone!=null)
            cardViewHolder.checkBox.setChecked(true);
        else
            cardViewHolder.checkBox.setChecked(false);
    }

    @Override
    public int getItemCount() {
            return appointList.size();
    }

    public void addAll(List<Appoint> itemList){
//        appointList.clear();
//        appointList.addAll(itemList);
        notifyDataSetChanged();
    }

    public void addUser(Appoint appoint){
        appointList.add(appoint);
        notifyDataSetChanged();
    }

    public void removeUser(Appoint appoint) {
        appointList.remove(appoint);
        notifyDataSetChanged();
    }

}