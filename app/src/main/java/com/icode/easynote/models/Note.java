package com.icode.easynote.models;

import java.io.Serializable;

public class Note implements Serializable {
    private long id;
    private String title;
    private String subtitle;
    private String date;
    private String note;
    private String color;
    private String photoPath;
    private String link;

    public Note(long id, String title, String subtitle, String date, String note, String color, String photoPath, String link) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.date = date;
        this.note = note;
        this.color = color;
        this.photoPath = photoPath;
        this.link = link;
    }

    public Note(String title, String subtitle, String date, String note, String color, String photoPath, String link) {
        this(-1, title, subtitle, date, note, color, photoPath, link);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
