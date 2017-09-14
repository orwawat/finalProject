package com.amdroidtech.barber;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class Appoint implements Comparable<Appoint>{
    public String date;
    public String key;
    public String phone;
    public String cust_push_id;
    public String user_push_id;

    public Appoint() {
    }

    public Appoint(String date, String key ,String user_push_id) {
        this.date = date;
        this.key = key;
        this.user_push_id=user_push_id;
    }

    @Override
    public int compareTo(Appoint o) {
        Date date1 =null;
        Date objDate = null;
        try {
            date1 = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy").parse(date);
            objDate = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy").parse(o.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1.compareTo(objDate);
    }
}
