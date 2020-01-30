package com.mediapickerlib.camera2.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.io.File;


public class CameraUtil {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final int SHUTTER_ONE_WAY_TIME = 150;
    public static final int MEDIA_SELECTION_COUNT = 10;

    public static final String EXTRA_IS_MEDIA_TYPE_BOTH = "is_media_type_both";

    public static final String EXTRA_SELECTION_COUNT = "selection_count";
    public static final String EXTRA_IS_FROM_TIMELINE = "is_from_timeline";

    public static final int REQ_CODE_CAMERA = 1001;
    public static final int REQ_MEDIA_PICK = 1003;
    public static final int REQ_EDIT_IMAGE = 1004;

    private static final String TAG = "CameraUtil";

    /**
     * Clamps x to between min and max (inclusive on both ends, x = min --> min,
     * x = max --> max).
     */
    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    /**
     * Clamps x to between min and max (inclusive on both ends, x = min --> min,
     * x = max --> max).
     */
    public static float clamp(float x, float min, float max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static void inlineRectToRectF(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }

    public static Rect rectFToRect(RectF rectF) {
        Rect rect = new Rect();
        inlineRectToRectF(rectF, rect);
        return rect;
    }

    public static RectF rectToRectF(Rect r) {
        return new RectF(r.left, r.top, r.right, r.bottom);
    }

    /**
     * Linear interpolation between a and b by the fraction t. t = 0 --> a, t =
     * 1 --> b.
     */
    public static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    /**
     * Given (nx, ny) \in [0, 1]^2, in the display's portrait coordinate system,
     * returns normalized sensor coordinates \in [0, 1]^2 depending on how the
     * sensor's orientation \in {0, 90, 180, 270}.
     * <p>
     * Returns null if sensorOrientation is not one of the above.
     * </p>
     */
    public static PointF normalizedSensorCoordsForNormalizedDisplayCoords(
            float nx, float ny, int sensorOrientation) {
        switch (sensorOrientation) {
            case 0:
                return new PointF(nx, ny);
            case 90:
                return new PointF(ny, 1.0f - nx);
            case 180:
                return new PointF(1.0f - nx, 1.0f - ny);
            case 270:
                return new PointF(1.0f - ny, nx);
            default:
                return null;
        }
    }

    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        // MEDIA_STORAGE_FOLDER_PATH
        File mediaImgStorageDir = new File("" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/DropApp/Media/DropApp Images/"), "DropApp Camera");
        File mediaVidStorageDir = new File("" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/DropApp/Media/DropApp Video/"),"DropApp Camera");


        // Create the storage directory if it does not exist
        if (!mediaImgStorageDir.exists()) {
            if (!mediaImgStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        if (!mediaVidStorageDir.exists()) {
            if (!mediaVidStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = ""+ System.currentTimeMillis();
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaImgStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaVidStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        Log.e(TAG, "getOutputMediaFile: " + mediaFile.getAbsolutePath());

        return mediaFile;
    }

    /**
     * Get the file of creating
     * record audio path
     *
     */
    public static File getRecordAudioFilePath() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "DropApp/Record");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.d("File", "failed to create directory");
                return null;
            }
        }
        File recordAudioFile = new File(dir, "DROP_RECORD_" + System.currentTimeMillis() + ".AAC");
        if (recordAudioFile.exists()) {
            recordAudioFile.delete();
        }
        return recordAudioFile;
    }


    /**
     * Rotate Bitmap
     */
    public static Bitmap rotate(Bitmap in, int angle) {
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        return Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), mat, true);
    }

    /**
     * Space between two finger
     */
    public static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Single tab slide mZoomRect
     */
    public static float getFingerSpacing(MotionEvent event, float xOld, float yOld) {
        float x = xOld - event.getX(0);
        float y = yOld - event.getY(0);
        //Log.e(TAG, "x-> "+x+" y-> "+y);
        //Log.e(TAG, "xOld-> "+xOld+" yOld-> "+yOld);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Get Latest Pic from gallery
     */
    public static Uri getLatestPic(Context context) {
        Uri mImageUri = null;
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = context.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        if (cursor.moveToFirst()) {
            String imageLocation = cursor.getString(1);
            File imageFile = new File(imageLocation);
            //Log.e(TAG, "getLatestPic: "+imageFile.getAbsolutePath());
            if (imageFile.exists()) {
                //mImagePath =  imageFile.getAbsolutePath();
                mImageUri = Uri.fromFile(imageFile);
                // Bitmap bm = BitmapFactory.decodeFile(imageLocation);
                //return bm;
            }
        }
        return mImageUri;
    }

    public static void shotAnimation(final View textureView) {
        final int colorFrom = Color.TRANSPARENT;
        int colorTo = 0xaf000000;
        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setInterpolator(new DecelerateInterpolator());
        colorAnimation.setDuration(SHUTTER_ONE_WAY_TIME);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textureView.setBackgroundColor((int) animation.getAnimatedValue());
            }
        });
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                textureView.setBackgroundColor(colorFrom);
            }
        });
        colorAnimation.start();
        textureView.postDelayed(new Runnable() {
            @Override
            public void run() {
                colorAnimation.reverse();
            }
        }, SHUTTER_ONE_WAY_TIME);
    }
}
