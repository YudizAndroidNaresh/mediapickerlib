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

public class ImageFetcher extends AsyncTask<Void, Void, ArrayList<Img>> {
    private ArrayList<Img> LIST = new ArrayList<>();
    private Context context;

    public ImageFetcher(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<Img> doInBackground(Void... voids) {
        int date;
        int data;
        int contentUrl;
        int size;

        Cursor cursorImage = Utility.Companion.getCursorImage(context);
        date = cursorImage.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
        data = cursorImage.getColumnIndex(MediaStore.Images.Media.DATA);
        contentUrl = cursorImage.getColumnIndex(MediaStore.Images.Media._ID);
        size = cursorImage.getColumnIndex(MediaStore.Images.Media.SIZE);
        String header = "";
        int limit = 100;
        if (cursorImage.getCount() < 100) {
            limit = cursorImage.getCount();
        }
        cursorImage.move(limit - 1);
        for (int i = limit; i < cursorImage.getCount(); i++) {
            cursorImage.moveToNext();
            Uri curl = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + cursorImage.getInt(contentUrl));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursorImage.getLong(date));
            String dateDifference = Utility.Companion.getDateDifference(context, calendar);

            if (!header.equalsIgnoreCase(dateDifference)) {
                header = dateDifference;
                LIST.add(new Img(dateDifference, "", "", 0, false, "", calendar.getTime()));
            }
            LIST.add(new Img(header, curl.toString(), cursorImage.getString(data), cursorImage.getLong(size), false, "", calendar.getTime()));
        }
        cursorImage.close();

        return LIST;
    }

}