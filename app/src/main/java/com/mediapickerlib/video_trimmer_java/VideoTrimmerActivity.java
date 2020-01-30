package com.mediapickerlib.video_trimmer_java;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mediapickerlib.R;
import com.mediapickerlib.builder.Media;
import com.mediapickerlib.video_trimmer_java.customVideoViews.BarThumb;
import com.mediapickerlib.video_trimmer_java.customVideoViews.CustomRangeSeekBar;
import com.mediapickerlib.video_trimmer_java.customVideoViews.OnRangeSeekBarChangeListener;
import com.mediapickerlib.video_trimmer_java.customVideoViews.OnVideoTrimListener;
import com.mediapickerlib.video_trimmer_java.customVideoViews.TileView;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import hu.agocs.rxmp4parser.RxMp4Parser;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.mediapickerlib.util.Constants.EXTRA_MEDIA;
import static com.mediapickerlib.util.Constants.INTENT_VIDEO_FILE;


public class VideoTrimmerActivity extends AppCompatActivity implements View.OnClickListener {

    //  private AppCompatTextView txtVideoCancel;
    private FloatingActionButton txtVideoUpload;
    private AppCompatTextView tv_title;

    private TextView txtVideoTrimSeconds;
    private RelativeLayout rlVideoView;
    private TileView tileView;
    private CustomRangeSeekBar mCustomRangeSeekBarNew;
    private VideoView mVideoView;
    private ImageView imgPlay;
    private SeekBar seekBarVideo;
    private TextView txtVideoLength;
    private FrameLayout frameLayout;
    //private ProgressBar progressBarTop;

    private int mDuration = 0;
    private int mTimeVideo = 0;
    private int mStartPosition = 0;
    private int mEndPosition = 0;
    // set your max video trim seconds
    private int mMaxDuration = 30;
    private Handler mHandler = new Handler();

    public ProgressDialog mProgressDialog;
    String srcFile;
    String dstFile;
    private static final String TAG = "VideoTrimmerActivity-->";

    public static final String VIDEO_FORMAT = ".mp4";

    private static VideoTrimmerActivity instant = null;

    public VideoTrimmerActivity getInstant(){
        return instant;
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(VideoTrimmerActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();

    }

    @Override
    public void onBackPressed() {
        saveVideoDialog();
    }

    private void saveVideoDialog() {
        new AlertDialog.Builder(VideoTrimmerActivity.this)
                .setTitle(getString(R.string.save))
                .setMessage(getString(R.string.save_it))
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveVideo();
                    }
                })
                .setNeutralButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        VideoTrimmerActivity.this.finish();
                    }
                })
                .create()
                .show();
    }

    OnVideoTrimListener mOnVideoTrimListener = new OnVideoTrimListener() {
        @Override
        public void onTrimStarted() {
            mProgressDialog = new ProgressDialog(VideoTrimmerActivity.this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setTitle("Saving....");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            if (mProgressDialog != null) {
                mProgressDialog.show();
            }

        }

        @Override
        public void getResult(Uri uri) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            Intent intent = new Intent();
            intent.putExtra(INTENT_VIDEO_FILE, uri.getPath());
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void cancelAction() {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }

        @Override
        public void onError(String message) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            Toast.makeText(VideoTrimmerActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//
//
//    }

    @Override
    public void onClick(View view) {
        if (view == frameLayout) {
            onBackPressed();
        } else if (view == txtVideoUpload) {

            saveVideo();

            // without ffmpeg lib

//                try {
//                    new AsyncTaskRunner(file, dstFile, mStartPosition * 1000, mEndPosition * 1000, mOnVideoTrimListener).execute();
//                } catch (Exception e) {
//                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }


            //FFmpeg video cropping

//                File resultFile = new File(dstFile);
//                try {
//                    resultFile.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if (resultFile.exists()) {
//                    String cmd_str = "-ss "+mStartPosition+" -y -i "+file.getAbsolutePath()+" -t "+(mEndPosition - mStartPosition)+" -s 320x240 -r 15 -vcodec mpeg4 -b:v 2097152 -b:a 48000 -ac 2 -ar 22050 "+resultFile.getAbsolutePath();
//
//                    String[] cmd = cmd_str.split(" ");
//
//                    Log.d("Command-->", cmd_str);
//
//                    execFFmpegBinary(cmd);
//
//                }


//                BackgroundTask.execute(
//                        new BackgroundTask.Task("", 0L, "") {
//                            @Override
//                            public void execute() {
//                                try {
//
//
//
//                                    Utility.startTrim(file, dstFile, mStartPosition * 1000, mEndPosition * 1000, mOnVideoTrimListener);
//                                } catch (final Throwable e) {
//                                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
//                                }
//                            }
//                        }
//                );
            //   }

        } else if (view == imgPlay) {
            if (mVideoView.isPlaying()) {
                if (mVideoView != null) {
                    mVideoView.pause();
                    //txtVideoLength.setText(seekBarVideo.getProgress());
                    imgPlay.setBackgroundResource(R.drawable.ic_white_play);
                }
            } else {
                if (mVideoView != null) {
                    mVideoView.start();
                    imgPlay.setBackgroundResource(R.drawable.ic_white_pause);
                    if (seekBarVideo.getProgress() == 0) {
                        txtVideoLength.setText("00:00");
                        updateProgressBar();
                    }
                }
            }
        } else if (view == frameLayout) {
            onBackPressed();
        }
    }

    private void saveVideo() {

        int diff = mEndPosition - mStartPosition;
        if (diff < 3) {
            Toast.makeText(VideoTrimmerActivity.this, getString(R.string.video_length_validation),
                    Toast.LENGTH_LONG).show();
        } else {
            MediaMetadataRetriever
                    mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(VideoTrimmerActivity.this, Uri.parse(srcFile));
            final File file = new File(srcFile);

            //notify that video trimming started
            if (mOnVideoTrimListener != null) {
                mOnVideoTrimListener.onTrimStarted();
            }

            final File f = new File(srcFile);
            if (!f.exists())
                try {
                    InputStream is = getAssets().open(srcFile);
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(buffer);
                    fos.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            final File output = new File(dstFile);
            RxMp4Parser.concatenateInto(
                    //The output, where should be stored the resulting Movie object
                    output,
                    //A full video
                    //  RxMp4Parser.from(f),
                    //Cropped video
                    RxMp4Parser.crop(f, mStartPosition, mEndPosition)
                    //Cropped video
                    // RxMp4Parser.crop(f.getAbsolutePath(), 5, 10),
                    //Another full video
//                        RxMp4Parser.from(f.getAbsolutePath())
//                                .lift(new CropMovie(18f, 20f))
            )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<File>() {
                        @Override
                        public void call(File file) {
                            if (file.exists()) {
                                // Toast.makeText(VideoTrimmerActivity.this, "Test successful: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                MediaScannerConnection.scanFile(VideoTrimmerActivity.this, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                                    @Override
                                    public void onMediaScannerConnected() {
                                    }

                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mOnVideoTrimListener.getResult(Uri.parse(output.getAbsolutePath()));
                                                // Toast.makeText(VideoTrimmerActivity.this, "Scan complete", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            } else {
                                mOnVideoTrimListener.onError("Video Cropping Failed!");
                                // Toast.makeText(VideoTrimmerActivity.this, "Test failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mOnVideoTrimListener.onError("Video Cropping Failed!");
                        }
                    });
        }
    }

    public static String convertArrayToStringMethod(String[] strArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strArray.length; i++) {
            stringBuilder.append(strArray[i]);
        }
        return stringBuilder.toString();
    }

    private void setBitmap(Uri mVideoUri) {
        tileView.setVideo(mVideoUri);
    }

    private void onVideoPrepared(@NonNull MediaPlayer mp) {
        // Adjust the size of the video
        // so it fits on the screen
        //TODO manage proportion for video
        /*int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = rlVideoView.getWidth();
        int screenHeight = rlVideoView.getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;
        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        mVideoView.setLayoutParams(lp);*/

        mDuration = mVideoView.getDuration() / 1000;
        setSeekBarPosition();
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (seekBarVideo.getProgress() >= seekBarVideo.getMax()) {
                seekBarVideo.setProgress((mVideoView.getCurrentPosition() - mStartPosition * 1000));
                txtVideoLength.setText(milliSecondsToTimer(seekBarVideo.getProgress()) + "");
                mVideoView.seekTo(mStartPosition * 1000);
                mVideoView.pause();
                seekBarVideo.setProgress(0);
                txtVideoLength.setText("00:00");
                imgPlay.setBackgroundResource(R.drawable.ic_white_play);
            } else {
                seekBarVideo.setProgress((mVideoView.getCurrentPosition() - mStartPosition * 1000));
                txtVideoLength.setText(milliSecondsToTimer(seekBarVideo.getProgress()) + "");
                mHandler.postDelayed(this, 100);
            }
        }
    };

    private void setSeekBarPosition() {
        if (mDuration >= mMaxDuration) {
            mStartPosition = 0;
            mEndPosition = mMaxDuration;
            mCustomRangeSeekBarNew.setThumbValue(0, (mStartPosition * 100) / mDuration);
            mCustomRangeSeekBarNew.setThumbValue(1, ((mStartPosition + 30) * 100) / mDuration);

        } else {
            mStartPosition = 0;
            mEndPosition = mDuration;
            mCustomRangeSeekBarNew.setThumbValue(0, (mStartPosition * 100) / mDuration);
            //  mCustomRangeSeekBarNew.setThumbValue(1, (((mStartPosition) * 100) / mDuration)+30);
            mCustomRangeSeekBarNew.setThumbValue(1, ((mEndPosition * 100) / mDuration));
        }


        mTimeVideo = mDuration;
        mCustomRangeSeekBarNew.initMaxWidth();
        seekBarVideo.setMax(mMaxDuration * 1000);
        mVideoView.seekTo(mStartPosition * 1000);

        String mStart = mStartPosition + "";
        if (mStartPosition < 10)
            mStart = "0" + mStartPosition;

        int startMin = Integer.parseInt(mStart) / 60;
        int startSec = Integer.parseInt(mStart) % 60;

        String mEnd = mEndPosition + "";
        if (mEndPosition < 10)
            mEnd = "0" + mEndPosition;

        int endMin = Integer.parseInt(mEnd) / 60;
        int endSec = Integer.parseInt(mEnd) % 60;

        txtVideoTrimSeconds.setText(String.format(Locale.US, "%02d:%02d - %02d:%02d", startMin, startSec, endMin, endSec));
    }

    /**
     * called when playing video completes
     */
    private void onVideoCompleted() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        seekBarVideo.setProgress(0);
        mVideoView.seekTo(mStartPosition * 1000);
        mVideoView.pause();
        imgPlay.setBackgroundResource(R.drawable.ic_white_play);
    }

    /**
     * Handle changes of left and right thumb movements
     *
     * @param index index of thumb
     * @param value value
     */
    private void onSeekThumbs(int index, float value) {
        switch (index) {
            case BarThumb.LEFT: {
                mStartPosition = (int) ((mDuration * value) / 100L);
                mVideoView.seekTo(mStartPosition * 1000);
                break;
            }
            case BarThumb.RIGHT: {
                mEndPosition = (int) ((mDuration * value) / 100L);
                break;
            }
        }
        mTimeVideo = (mEndPosition - mStartPosition);
        seekBarVideo.setMax(mTimeVideo * 1000);
        seekBarVideo.setProgress(0);
        mVideoView.seekTo(mStartPosition * 1000);

        String mStart = mStartPosition + "";
        if (mStartPosition < 10)
            mStart = "0" + mStartPosition;

        int startMin = Integer.parseInt(mStart) / 60;
        int startSec = Integer.parseInt(mStart) % 60;

        String mEnd = mEndPosition + "";
        if (mEndPosition < 10)
            mEnd = "0" + mEndPosition;
        int endMin = Integer.parseInt(mEnd) / 60;
        int endSec = Integer.parseInt(mEnd) % 60;

        txtVideoTrimSeconds.setText(String.format(Locale.US, "%02d:%02d - %02d:%02d", startMin, startSec, endMin, endSec));
    }

    private void onStopSeekThumbs() {
//        mMessageHandler.removeMessages(SHOW_PROGRESS);
//        mVideoView.pause();
//        mPlayView.setVisibility(View.VISIBLE);
    }


    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString;
        String minutesString;


        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = "" + minutes;
        }
        finalTimerString = finalTimerString + minutesString + ":" + secondsString;
        // return timer string
        return finalTimerString;
    }


    // implementation "com.googlecode.mp4parser:isoparser:1.1.22"

//    class AsyncTaskRunner extends AsyncTask<String, String, String> {
//
//        private String resp;
//        File src;
//        String dst;
//        long startMs;
//        long endMs;
//        OnVideoTrimListener callback;
//        Boolean isError = false;
//
//
//        public AsyncTaskRunner(@NonNull File src, @NonNull String dst, long startMs,
//                               long endMs, @NonNull OnVideoTrimListener callback) {
//            this.src = src;
//            this.dst = dst;
//            this.startMs = startMs;
//            this.endMs = endMs;
//            this.callback = callback;
//        }
//
//
//        @Override
//        protected String doInBackground(String... params) {
//
//            try {
//                Movie movie = null;
//                if (src.exists()) {
//                    FileDataSourceImpl file = new FileDataSourceImpl(src);
//                    movie = MovieCreator.build(file);
//                } else {
//                    return "File is not exists";
//                }
//
//                List<Track> tracks = movie.getTracks();
//                movie.setTracks(new LinkedList<Track>());
//                // remove all tracks we will create new tracks from the old
//
//                double startTime1 = startMs / 1000;
//                double endTime1 = endMs / 1000;
//
//                boolean timeCorrected = false;
//
//                // Here we try to find a track that has sync samples. Since we can only start decoding
//                // at such a sample we SHOULD make sure that the start of the new fragment is exactly
//                // such a frame
//                for (Track track : tracks) {
//                    if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
//                        if (timeCorrected) {
//                            // This exception here could be a false positive in case we have multiple tracks
//                            // with sync samples at exactly the same positions. E.g. a single movie containing
//                            // multiple qualities of the same video (Microsoft Smooth Streaming file)
//
//                            throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
//                        }
////                startTime1 = correctTimeToSyncSample(track, startTime1, false);
////                endTime1 = correctTimeToSyncSample(track, endTime1, true);
//                        timeCorrected = true;
//                    }
//                }
//
//                for (Track track : tracks) {
//                    long currentSample = 0;
//                    double currentTime = 0;
//                    double lastTime = -1;
//                    long startSample1 = -1;
//                    long endSample1 = -1;
//
//                    for (int i = 0; i < track.getSampleDurations().length; i++) {
//                        long delta = track.getSampleDurations()[i];
//                        if (currentTime > lastTime && currentTime <= startTime1) {
//                            // current sample is still before the new starttime
//                            startSample1 = currentSample;
//                        }
//                        if (currentTime > lastTime && currentTime <= endTime1) {
//                            // current sample is after the new start time and still before the new endtime
//                            endSample1 = currentSample;
//                        }
//                        lastTime = currentTime;
//                        currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
//                        currentSample++;
//                    }
//                    movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
//                }
//
//                Container out = new DefaultMp4Builder().build(movie);
//
//                FileOutputStream fos = new FileOutputStream(dst);
//                FileChannel fc = fos.getChannel();
//                out.writeContainer(fc);
//
//                fc.close();
//                fos.close();
//                isError = false;
//
//            } catch (IOException e) {
//                resp = e.getMessage();
//                isError = true;
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                resp = e.getMessage();
//                isError = true;
//            }
//
//            return resp;
//        }
//
//
//        @Override
//        protected void onPostExecute(String result) {
//            if (callback != null && !isError) {
//                callback.getResult(Uri.parse(dst));
//            } else {
//                callback.onError(result);
//            }
//
//        }
//
//        @Override
//        protected void onPreExecute() {
//
//        }
//
//
//    }


    private String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri.
     */
    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_video_trimmer);

        instant = this;

        init();
    }


    protected void init() {
        txtVideoUpload = findViewById(R.id.txtVideoUpload);
        // tv_title = findViewById(R.id.tv_title);

        txtVideoTrimSeconds = findViewById(R.id.txtVideoTrimSeconds);
        rlVideoView = findViewById(R.id.llVideoView);
        tileView = findViewById(R.id.timeLineView);
        mCustomRangeSeekBarNew = findViewById(R.id.timeLineBar);
        mVideoView = findViewById(R.id.videoView);
        imgPlay = findViewById(R.id.imgPlay);
        seekBarVideo = findViewById(R.id.seekBarVideo);
        txtVideoLength = findViewById(R.id.txtVideoLength);
//        progressBarTop = findViewById(R.id.progress_bar_top);
//        progressBarTop.setVisibility(View.GONE);
        frameLayout = findViewById(R.id.iv_back);
        frameLayout.setOnClickListener(this);

        //   tv_title.setText(getString(R.string.video_trimmer));

//        DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) mVideoView.getLayoutParams();
//        params.width =  metrics.widthPixels;
//        params.height = metrics.heightPixels;
//        params.leftMargin = 0;
//        mVideoView.setLayoutParams(params);


        if (getIntent().getExtras() != null) {
            srcFile = getIntent().getExtras().getString(EXTRA_MEDIA);
        }
        dstFile = Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + new Date().getTime()
                + VIDEO_FORMAT;

        tileView.post(new Runnable() {
            @Override
            public void run() {
                setBitmap(Uri.parse(srcFile));
                mVideoView.setVideoURI(Uri.parse(srcFile));
            }
        });

        //  txtVideoCancel.setOnClickListener(this);
        txtVideoUpload.setOnClickListener(this);

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                onVideoPrepared(mp);
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onVideoCompleted();
            }
        });

        // handle your range seekbar changes
        mCustomRangeSeekBarNew.addOnRangeSeekBarListener(new OnRangeSeekBarChangeListener() {
            @Override
            public void onCreate(CustomRangeSeekBar customRangeSeekBarNew, int index, float value) {
                // Do nothing
            }

            @Override
            public void onSeek(CustomRangeSeekBar customRangeSeekBarNew, int index, float value) {
                onSeekThumbs(index, value);
            }

            @Override
            public void onSeekStart(CustomRangeSeekBar customRangeSeekBarNew, int index, float value) {
                if (mVideoView != null) {
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    seekBarVideo.setProgress(0);
                    mVideoView.seekTo(mStartPosition * 1000);
                    mVideoView.pause();
                    imgPlay.setBackgroundResource(R.drawable.ic_white_play);
                }
            }

            @Override
            public void onSeekStop(CustomRangeSeekBar customRangeSeekBarNew, int index, float value) {
                onStopSeekThumbs();
            }
        });

        imgPlay.setOnClickListener(this);

        // handle changes on seekbar for video play
        seekBarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mVideoView != null) {
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    seekBarVideo.setMax(mTimeVideo * 1000);
                    seekBarVideo.setProgress(0);
                    mVideoView.seekTo(mStartPosition * 1000);
                    mVideoView.pause();
                    imgPlay.setBackgroundResource(R.drawable.ic_white_play);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                mVideoView.seekTo((mStartPosition * 1000) - seekBarVideo.getProgress());
            }
        });


    }


}
