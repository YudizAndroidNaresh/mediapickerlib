package com.mediapickerlib.fetcher;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.mediapickerlib.modals.Img;
import com.mediapickerlib.util.Utility;

import java.util.ArrayList;
import java.util.Calendar;

public class VideoFetcher extends AsyncTask<Void, Void, ArrayList<Img>> {
    private ArrayList<Img> LIST = new ArrayList<>();
    private Context context;

    public VideoFetcher(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<Img> doInBackground(Void... voids) {
        int date;
        int data;
        int contentUrl;
        int size;

        Cursor cursorVideo = Utility.Companion.getCursorVideo(context);
        date = cursorVideo.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
        data = cursorVideo.getColumnIndex(MediaStore.Video.Media.DATA);
        contentUrl = cursorVideo.getColumnIndex(MediaStore.Video.Media._ID);
        size = cursorVideo.getColumnIndex(MediaStore.Video.Media.SIZE);
        String header = "";
        int limit = 100;
        if (cursorVideo.getCount() < 100) {
            limit = cursorVideo.getCount();
        }
        cursorVideo.move(limit - 1);
        for (int i = limit; i < cursorVideo.getCount(); i++) {
            cursorVideo.moveToNext();
            Uri curl = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + cursorVideo.getInt(contentUrl));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursorVideo.getLong(date));
            String dateDifference = Utility.Companion.getDateDifference(context, calendar);

            if (!header.equalsIgnoreCase(dateDifference)) {
                header = dateDifference;
                LIST.add(new Img(dateDifference, "", "", 0, false, "", calendar.getTime()));
            }
            LIST.add(new Img(header, curl.toString(), cursorVideo.getString(data), cursorVideo.getLong(size), true, "", calendar.getTime()));
        }
        cursorVideo.close();

        return LIST;
    }

}