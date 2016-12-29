package com.example.android.firechat;

/**
 * Created by kmvkrish on 26-12-2016.
 */

public class Message {

    private String text;
    private String photoUrl;
    private String name;
    private String key;

    public Message(String text, String photoUrl, String name) {
        this.text = text;
        this.photoUrl = photoUrl;
        this.name = name;
    }

    public Message() {
    }

    public String getText() {
        return text;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
