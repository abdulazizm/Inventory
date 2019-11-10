package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.util.concurrent.locks.ReentrantLock;

public class LocalDb {

    private SQLiteDatabase db;
    private ReentrantLock lock = new ReentrantLock();

    public LocalDb(Context ctx){
        lock.lock();
        db=ctx.openOrCreateDatabase("FPEOpenSourceInventory", Context.MODE_PRIVATE,null);
        //db.execSQL("CREATE TABLE IF NOT EXISTS orders(series INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,username VARCHAR,orderid VARCHAR, pid VARCHAR, order_date VARCHAR,address VARCHAR, payment VARCHAR, price VARCHAR,giftwrap VARCHAR, phone VARCHAR, status VARCHAR, delivery VARCHAR, quantity VARCHAR, delivery_fee VARCHAR, imageurl VARCHAR, title VARCHAR,order_month VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS userdata( username VARCHAR, password VARCHAR, company VARCHAR, cid INT, email VARCHAR, address VARCHAR, phone VARCHAR, loggedin VARCHAR)");

        //db.execSQL("CREATE TABLE IF NOT EXISTS company(series INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,company_name VARCHAR,cid VARCHAR, pass VARCHAR, reg_date VARCHAR,address VARCHAR, gst VARCHAR, phone VARCHAR, acc_status VARCHAR, total_products_number VARCHAR, imageurl VARCHAR, category VARCHAR)");
        //db.execSQL("CREATE TABLE IF NOT EXISTS product( series INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, company_name VARCHAR, cid VARCHAR, creation_timestamp VARCHAR, prod_price VARCHAR, prod_status VARCHAR, prod_stock VARCHAR, prod_name VARCHAR,prod_description VARCHAR, prod_image VARCHAR, prod_category VARCHAR)");
        //db.execSQL("CREATE TABLE IF NOT EXISTS category( series INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, company_name VARCHAR, cid VARCHAR, creation_timestamp VARCHAR, category_name VARCHAR, category_image VARCHAR, category_description VARCHAR, category_rank VARCHAR)");

        lock.unlock();

    }

    void addUser(String username,String password,String company, int cid, String email,String address,String phone){
        lock.lock();
        db.execSQL("INSERT INTO userdata VALUES('"+username+"','"+password+"','"+company+"','"+cid+"','"+email+"','"+address+"','"+phone+"', 'true');");
        lock.unlock();
    }

    boolean checkUserLoggedin(){
        lock.lock();
        Cursor c=db.rawQuery("SELECT loggedin FROM userdata LIMIT 1",null);
        lock.unlock();
        if(c.moveToFirst()){
            if(c.getString(0).equals("true")) {
                c.close();
                return true;
            } else {
                c.close();
                return false;
            }
        }
        return true;
    }

    void logout(){
            lock.lock();
            db.execSQL("UPDATE userdata SET loggedin ='false'");
            lock.unlock();
    }

    String getUsername(){
        lock.lock();
        Cursor c=db.rawQuery("SELECT username FROM userdata LIMIT 1" ,null);
        lock.unlock();

        if(c.moveToFirst()){
            String getString = c.getString(0);
            c.close();
            return getString;
        }
        c.close();
        return null;
    }

    int getCID(){
        lock.lock();
        Cursor c=db.rawQuery("SELECT cid FROM userdata LIMIT 1" ,null);
        lock.unlock();

        if(c.moveToFirst()){
            int getInt = c.getInt(0);
            c.close();
            return getInt;
        }
        c.close();
        return 0;
    }

    String getCompanyName(){
        lock.lock();
        Cursor c=db.rawQuery("SELECT company FROM userdata LIMIT 1" ,null);
        lock.unlock();

        if(c.moveToFirst()){
            String get = c.getString(0);
            c.close();
            return get;
        }
        c.close();
        return null;
    }

    boolean checkNewUser(){
        lock.lock();
        Cursor c=db.rawQuery("SELECT count(*) FROM orders",null);
        lock.unlock();
        if(c.moveToFirst()){
            if(c.getInt(0)<1) {
                c.close();
                return true;
            } else {
                c.close();
                return false;
            }
        }
        return false;
    }
    void login(){
        lock.lock();
        db.execSQL("UPDATE userdata SET loggedin ='true'");
        lock.unlock();
    }
    boolean alreadyRegistered(){
        lock.lock();
        Cursor c=db.rawQuery("SELECT username FROM userdata",null);
        lock.unlock();
        if(c.moveToFirst()){
            if(c.getString(0)!=null&&!c.getString(0).equals("null")) {
                c.close();
                return true;
            } else {
                c.close();
                return false;
            }
        }
        return false;
    }

    boolean checkUser(String username, String password){
        lock.lock();
        Cursor c=db.rawQuery("SELECT username,password FROM userdata",null);
        lock.unlock();
        if(c.moveToFirst()){
            if(c.getString(0).equals(username)&&c.getString(1).equals(password)) {
                c.close();
                return true;
            } else {
                c.close();
                return false;
            }
        }
        return false;
    }
}
