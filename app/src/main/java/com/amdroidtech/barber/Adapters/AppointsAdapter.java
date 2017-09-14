package com.amdroidtech.barber.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.amdroidtech.barber.Appoint;
import com.amdroidtech.barber.R;
import com.amdroidtech.barber.Utilities.FirebaseUtil;
import com.amdroidtech.barber.Utilities.SharedPrefUtilities;
import com.amdroidtech.barber.Widgets.FcmNotificationBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.amdroidtech.barber.Utilities.Constants.CUSTOMER;

public class AppointsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String TAG = "AppointsAdapter";
    private Context context;
    private List<Appoint> appointList;
    private SharedPrefUtilities mSharedPrefUtilities;

    public AppointsAdapter(Context context , List<Appoint> appointList) {
        this.context = context;
        this.appointList = appointList;
        mSharedPrefUtilities  = new SharedPrefUtilities(context);
    }

    public List<Appoint> getAppointsItems() {
        return appointList;
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.delete)
        FrameLayout deleteFr;

        @Bind(R.id.phone_num)
        TextView phoneNum;

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

        View view = LayoutInflater.from(context).inflate(R.layout.appoint_item,parent, false);
        RecyclerView.ViewHolder holder = null;
            holder = new CardViewHolder(view);
            final CardViewHolder cardHolder = (CardViewHolder)holder;

        //set click listener on accept linear
        cardHolder.deleteFr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Appoint appoint = appointList.get(cardHolder.getAdapterPosition());
                //show alarm dialog
                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Cancel Appoint?")
                        .setContentText("Cancel your appoint?")
                        .setCancelText("No")
                        .setConfirmText("Yes")
                        .showCancelButton(true)
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.cancel();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(final SweetAlertDialog sDialog) {
                                //show progress
                                sDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                sDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                                sDialog.setContentText("");
                                sDialog.setTitleText("Loading");
                                sDialog.setCancelable(false);
                                sDialog.showCancelButton(false);
                                sDialog.show();

                                //make delete appoint
                                FirebaseUtil.getAppointsRef().child(appoint.key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            sDialog.setTitleText("Canceled!")
                                                    .showCancelButton(false)
                                                    .setContentText("Done!")
                                                    .setConfirmText("OK")
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {

                                                            sDialog.dismiss();
                                                        }
                                                    })
                                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                            //send notification to customer if phone number assoicated
                                            sendNotification(appoint);

                                        }else {
                                            sDialog.setTitleText("Failed!")
                                                    .showCancelButton(false)
                                                    .setConfirmText("OK")
                                                    .setConfirmClickListener(null)
                                                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                        }
                                    }
                                });


                            }

                        })
                        .show();
            }
        });

        return holder;
    }

    /**
     * try to send push notification to customer of caceling appoint
     * @param appoint
     */
    private void sendNotification(Appoint appoint) {
        if (appoint.phone!=null && appoint.cust_push_id!=null){
                String username = mSharedPrefUtilities.getUserEmail().substring(0,mSharedPrefUtilities.getUserEmail().indexOf('@'));
                String messageStr ="Appoint "+ appoint.date +" has been canceled by admin "+username;
                String uid =FirebaseUtil.getCurrentUserId();
                String firebaseToken = appoint.user_push_id;
                String receiverFirebaseToken = appoint.cust_push_id;
                FcmNotificationBuilder.initialize()
                        .title(username)
                        .message(messageStr)
                        .username(CUSTOMER)
                        .uid(uid)
                        .firebaseToken(firebaseToken)
                        .receiverFirebaseToken(receiverFirebaseToken)
                        .send();
        }
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

            if(appoint.phone!=null)
                cardViewHolder.phoneNum.setText(appoint.phone);
             else
                cardViewHolder.phoneNum.setVisibility(View.GONE);

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