//  The MIT License (MIT)

//  Copyright (c) 2018 Intuz Solutions Pvt Ltd.

//  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
//  (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
//  merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:

//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
//  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.mediapickerlib.video_trimmer_java.utils;


//import com.coremedia.iso.boxes.Container;
//import com.googlecode.mp4parser.authoring.Movie;
//import com.googlecode.mp4parser.authoring.Track;
//import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
//import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
//import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
//import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

public class Utility {

//    public static final String VIDEO_FORMAT = ".mp4";
//    private static final String TAG = Utility.class.getSimpleName();
//
//    public static void startTrim(@NonNull File src, @NonNull String dst, long startMs, long endMs,
//                                 @NonNull OnVideoTrimListener callback) throws IOException {
//        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
//
//        File file = new File(dst);
//        file.getParentFile().mkdirs();
//        Log.d(TAG, "Generated file path " + dst);
//
//        try {
//            generateVideo(src, file, startMs, endMs, callback);
//        } catch (IOException e) {
//            Log.d(TAG, "trimmer video -> IOException" + e.getMessage());
//        } catch (Exception e) {
//            Log.d(TAG, "trimmer video -> Exception" + e.getMessage());
//        }
//
//    }
//
//    private static void generateVideo(@NonNull File src, @NonNull File dst, long startMs,
//                                      long endMs, @NonNull OnVideoTrimListener callback) throws IOException {
//        // NOTE: Switched to using FileDataSourceViaHeapImpl since it does not use memory mapping (VM).
//        // Otherwise we get OOM with large movie files.
//        Movie movie = MovieCreator.build(src.getAbsolutePath());
//
//        List<Track> tracks = movie.getTracks();
//        movie.setTracks(new LinkedList<Track>());
//        // remove all tracks we will create new tracks from the old
//
//        double startTime1 = startMs / 1000;
//        double endTime1 = endMs / 1000;
//
//        boolean timeCorrected = false;
//
//        // Here we try to find a track that has sync samples. Since we can only start decoding
//        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
//        // such a frame
//        for (Track track : tracks) {
//            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
//                if (timeCorrected) {
//                    // This exception here could be a false positive in case we have multiple tracks
//                    // with sync samples at exactly the same positions. E.g. a single movie containing
//                    // multiple qualities of the same video (Microsoft Smooth Streaming file)
//
//                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
//                }
////                startTime1 = correctTimeToSyncSample(track, startTime1, false);
////                endTime1 = correctTimeToSyncSample(track, endTime1, true);
//                timeCorrected = true;
//            }
//        }
//
//        for (Track track : tracks) {
//            long currentSample = 0;
//            double currentTime = 0;
//            double lastTime = -1;
//            long startSample1 = -1;
//            long endSample1 = -1;
//
//            for (int i = 0; i < track.getSampleDurations().length; i++) {
//                long delta = track.getSampleDurations()[i];
//
//
//                if (currentTime > lastTime && currentTime <= startTime1) {
//                    // current sample is still before the new starttime
//                    startSample1 = currentSample;
//                }
//                if (currentTime > lastTime && currentTime <= endTime1) {
//                    // current sample is after the new start time and still before the new endtime
//                    endSample1 = currentSample;
//                }
//                lastTime = currentTime;
//                currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
//                currentSample++;
//            }
//            movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
//        }
//
//        Container out = new DefaultMp4Builder().build(movie);
//
//        FileOutputStream fos = new FileOutputStream(dst);
//        FileChannel fc = fos.getChannel();
//        out.writeContainer(fc);
//        fc.close();
//        fos.close();
//        if (callback != null)
//            callback.getResult(Uri.parse(dst.toString()));
//    }
//
//
//    public class AsyncTaskRunner extends AsyncTask<String, String, String> {
//
//        private String resp;
//        File src;
//        File dst;
//        long startMs;
//        long endMs;
//        OnVideoTrimListener callback;
//        Boolean isError  = false;
//
//        public AsyncTaskRunner(@NonNull File src, @NonNull File dst, long startMs,
//                               long endMs, @NonNull OnVideoTrimListener callback) {
//            this.src = src;
//            this.dst = dst;
//            this.startMs = startMs;
//            this.endMs = endMs;
//            this.callback = callback;
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//
//
//            try {
//
//                Movie movie = MovieCreator.build(src.getAbsolutePath());
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
//                isError=false;
//
//            } catch (IOException e) {
//                resp = e.getMessage();
//                isError=true;
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                resp = e.getMessage();
//                isError=true;
//            }
//
//            return resp;
//        }
//
//
//        @Override
//        protected void onPostExecute(String result) {
//            if (callback != null && !isError)
//                callback.getResult(Uri.parse(dst.toString()));
//        }
//        @Override
//        protected void onPreExecute() {
//
//        }
//
//
//    }


}
