package com.example.ghost.myapplication;

/**
 * Created by ghost on 22/03/2016.
 */
public class UserData {
    //Variables
    int _id;
    String _imei;
    String _name;
    String _message;

    public String get_imei() {
        return _imei;
    }

    public void set_imei(String _imei) {
        this._imei = _imei;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_message() {
        return _message;
    }

    public void set_message(String _message) {
        this._message = _message;
    }


    public UserData(int _id, String _imei, String _name, String _message) {
        this._id = _id;
        this._imei = _imei;
        this._name = _name;
        this._message = _message;
    }


    public UserData(){

    }


    public String toString(){
        return "UserInfo [name= " +_name+ "]";
    }


}
