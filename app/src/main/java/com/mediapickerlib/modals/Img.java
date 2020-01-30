package com.mediapickerlib.modals;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class Img implements Serializable, Comparable<Img> {

    private String headerDate;
    private String contentUrl;
    private String url;
    private Date dateTaken;
    private long size;
    private boolean isVideo;
    private String mMessage = "";

    public Img(String headerDate, String contentUrl, String url, long size, boolean isVideo, String message, Date dateTaken) {
        this.headerDate = headerDate;
        this.contentUrl = contentUrl;
        this.url = TextUtils.isEmpty(url) ? contentUrl : url;
        this.size = size;
        this.isVideo = isVideo;
        this.mMessage = message;
        this.dateTaken = dateTaken;
    }

    public String getHeaderDate() {
        return headerDate;
    }

    public void setHeaderDate(String headerDate) {
        this.headerDate = headerDate;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    @Override
    public int compareTo(@NonNull Img img) {
        return img.dateTaken.compareTo(dateTaken);
    }
}
