package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.media.ThumbnailUtils.createVideoThumbnail;
import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;
import static com.example.myapplication.StorageUtil.checkSDCard;
import static com.example.myapplication.StorageUtil.getRealPathFromURI;
import static com.example.myapplication.StorageUtil.musicFilter;
import static com.example.myapplication.StorageUtil.videoFilter;
import static com.example.myapplication.Util.FROM_FILE_BROWSER;
import static com.example.myapplication.Util.FROM_MUSIC_ACTIVITY;
import static com.example.myapplication.Util.FROM_RECORD_ACTIVITY;
import static com.example.myapplication.Util.FROM_VIDEO_ACTIVITY;
import static com.example.myapplication.Util.setFromWhichActivity;
import static com.example.myapplication.Util.showToastIns;


public class VideoActivity extends AppCompatActivity {

    private static final String TAG = "Alan";

    private Button VideoBrowserB;
    private TextView VideoPath;
    private TextView RecordTime;
    private TextView RecordHint;
    private ImageView VideoPlayPause;
    private ImageView AudioRecord;
    private TextView endTimeField, startTimeField;
    private SeekBar VideoSeek;

    private File[] files;
    private File[] Music_Files;

    private VideoView videoView;
    private String uriPath;

    //private SurfaceView mSurfaceView;
    //private SurfaceHolder mHolder;

    private int Videoindex=0;
    SharedPreferences mem_MediaSource;
    SharedPreferences mem_MediaType;
    SharedPreferences mem_MediaVideoPath;
    SharedPreferences mem_MediaMusicPath;
    SharedPreferences mem_MediaRecordPath;

    private int finalTime=0, startTime=0;
    private Handler myHandler = new Handler();


    private boolean isRecording=false;
    private MediaRecorder myAudioRecorder;
    //private AudioRecord myRecorder;

    private int rec_Time=0;
    private Handler recHandler = new Handler();

    private String recFile;
    private static final String default_video_path=Environment.getExternalStorageDirectory() + "/Movies/";
    private static final String default_music_path=Environment.getExternalStorageDirectory() + "/Music/";
    private static final String default_rec_path=Environment.getExternalStorageDirectory() + "/Music";

    private static final Boolean MEDIA_TYPE_MUSIC=false;
    private static final Boolean MEDIA_TYPE_VIDEO=true;
    //private static final Boolean MEDIA_SOURCE_RAW=false;
    private static final Boolean MEDIA_SOURCE_SD=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        //Log.w(TAG, "VideoActivity:onCreate");
        mem_MediaSource = getSharedPreferences("MEDIA_SOURCE", MODE_PRIVATE);
        mem_MediaType = getSharedPreferences("MEDIA_TYPE", MODE_PRIVATE);
        mem_MediaVideoPath = getSharedPreferences("MEDIA_VIDEO_PATH", MODE_PRIVATE);
        mem_MediaMusicPath = getSharedPreferences("MEDIA_MUSIC_PATH", MODE_PRIVATE);
        mem_MediaRecordPath = getSharedPreferences("MEDIA_RECORDER_PATH", MODE_PRIVATE);

        InitialView();
        InitialRecorder();
    }

    @Override
    protected void onResume() {
        Log.w(TAG, "VideoActivity:onResume");

        //---------------------------------------------------------
        // 隱藏BAR，API Level 16以上，離開當前Activity時會被清除，
        // 所以要放在onResum，下次再進來時才會被執行
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN |    // 隱藏status bar
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |    // 隱藏Navigation bar
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |       // 滑動一下出現半透明bar，過一下子消失
                                                            // 如果沒有這個，按一下畫面bar出現後就不會消失了
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |   // 將layout充滿整個畫面，就算bar出來也不會縮回(bar蓋住內容)
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE;    // 加了這個不知為何bar的地方變成留白了
                                                        // 需配合SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN才不會留白
        decorView.setSystemUiVisibility(uiOptions);
        //---------------------------------------------------------

        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        // 隱藏app的title，一定要放在onCreate裡
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();
        //---------------------------------------------------------

        InitialVideoPlayer();
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        Log.w(TAG, "VideoActivity:onDestroy");
        videoView.stopPlayback();
        setFromWhichActivity(FROM_FILE_BROWSER);
        super.onDestroy();
    }


    // 按back key
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 如果是由FileManagerActivity回來這，按back又會至FileManagerActivity,
            // 為避免這個情況，強制回至MainActivity，並且設定FLAG_ACTIVITY_CLEAR_TOP
            Log.w(TAG, "press back");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    //=================================================================
    // Initial View
    //=================================================================
    @SuppressLint("ClickableViewAccessibility")
    private void InitialView(){
        String temp;
        videoView = findViewById(R.id.videoView);
        videoView.setOnTouchListener(viewTouch);

        VideoBrowserB = findViewById(R.id.VideoBrowserBtn);
        VideoPlayPause = findViewById(R.id.VideoPlayImg);
        ImageView videoPrevious = findViewById(R.id.VideoPreviousImg);
        ImageView videoNextImg = findViewById(R.id.VideoNextImg);
        ImageView videoFW = findViewById(R.id.VideoFFImg);
        ImageView videoBW = findViewById(R.id.VideoRewImg);
        AudioRecord = findViewById(R.id.RecordImg);
        Button recordBrowserB = findViewById(R.id.RecBrowserButton);

        VideoBrowserB.setOnClickListener(VideoButton);
        VideoPlayPause.setOnClickListener(VideoButton);
        videoPrevious.setOnClickListener(VideoButton);
        videoNextImg.setOnClickListener(VideoButton);
        videoFW.setOnClickListener(VideoButton);
        videoBW.setOnClickListener(VideoButton);
        AudioRecord.setOnClickListener(VideoButton);
        recordBrowserB.setOnClickListener(VideoButton);

        VideoPlayPause.setImageResource(android.R.drawable.ic_media_play);

        VideoPath = findViewById(R.id.VideoPath);
        VideoPath.setSelected(true); // 跑馬燈

        endTimeField = findViewById(R.id.finalPos);
        startTimeField = findViewById(R.id.currentPos);

        VideoSeek = findViewById(R.id.VideoSeekbar);
        temp="0 min, 0 sec";
        endTimeField.setText(temp);
        startTimeField.setText(temp);
        VideoSeek.setMax(100);
        VideoSeek.setProgress(0);
        VideoSeek.setOnSeekBarChangeListener(seekBarOnSeekBarChange);

        Switch videoSwitch = findViewById(R.id.MediaSourceSwitch);
        videoSwitch.setOnCheckedChangeListener(VideoSW);
        videoSwitch.setChecked(MediaSourceGet());   // RAW=0 or SDCard=1
        if (MediaSourceGet()==MEDIA_SOURCE_SD)
            VideoBrowserB.setEnabled(true);
        else
            VideoBrowserB.setEnabled(false);

        Switch musicSwitch = findViewById(R.id.MediaTypeSwitch);
        musicSwitch.setOnCheckedChangeListener(VideoSW);
        musicSwitch.setChecked(MediaTypeGet());

        TextView recordPath = findViewById(R.id.RecordFile);

        String media_recorder_path = checkRECPath();
        recFile= media_recorder_path +"/myrecording.3gp";
        Log.w(TAG, "recFile="+recFile);

        temp="錄音路徑:"+recFile;
        recordPath.setText(temp);
        recordPath.setSelected(true);   // 跑馬燈

        RecordTime = findViewById(R.id.RecTime);
        temp="00:00/10:00";
        RecordTime.setText(temp);
        RecordTime.setTextColor(Color.BLUE);

        RecordHint = findViewById(R.id.RecHintText);
        RecordHint.setText(getString(R.string.stopRecord));
        RecordHint.setTextColor(Color.BLUE);
    }

    //=================================================================
    // 取得Video縮圖
    //=================================================================
    private void InitialBG(){
        // 設定背景圖，但開始播沒法正常更新畫面
        Bitmap bm;
        BitmapDrawable bd;

        if (MediaSourceGet()==MEDIA_SOURCE_SD && MediaTypeGet()==MEDIA_TYPE_VIDEO) {   // 只有SD的Video才有預覽背景圖
            uriPath = getRealPathFromURI(Uri.fromFile(files[Videoindex]));

            // 方法一
            bm = createVideoThumbnail(uriPath, MINI_KIND); // 用API的方式取圖

            // 方法二
            //Bitmap bm=decodeFrame(uriPath, 10000);    // 取得特定時間點的圖

            bd= new BitmapDrawable(getResources(), bm);
        }else{
            bm= BitmapFactory.decodeResource(getResources(), R.drawable.delta);
            bd= new BitmapDrawable(getResources(), bm);
        }
        videoView.setBackground(bd);
    }

    //=================================================================
    // Initial VideoView
    //=================================================================
    private void InitialVideoPlayer(){
        MediaController mc = new MediaController(this);
        mc.setKeepScreenOn(true);   // 保持螢幕開著

        // 移除下面預設控制列，改用自定義控制方式
        //videoView.setMediaController(mc); // 啟用Video控制列

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.w(TAG, "VideoActivity:檔案準備好了");
                InitSeekBar();

                // 切換曲目先暫停
                videoView.pause();
                VideoPlayPause.setImageResource(android.R.drawable.ic_media_play);

                InitialBG();    // 未播放之前顯示背景圖

                // 開始播放後，將背景圖透明，開始顯示媒體內容
                mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mediaPlayer, int what, int i1) {
                        //Log.w(TAG, "背景透明:"+what);

                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) { // 3

                            videoView.setBackgroundColor(Color.TRANSPARENT);
                        }
                        return true;
                    }
                });

                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener(){

                    @Override
                    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int weight, int height) {
                        int mVideoWidth = mediaPlayer.getVideoWidth();
                        int mVideoHeight = mediaPlayer.getVideoHeight();
                        Log.w(TAG, "width:"+mVideoWidth+",height:"+mVideoHeight);

                        //refreshPortraitScreen
                    }
                });
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.w(TAG, "VideoActivity:影片播完了");
                VideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                VideoSeek.setProgress(0);   // seekbar回到最前面
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Log.w(TAG, "VideoActivity:影片播放失敗"+i+","+i1);
                return true;
            }
        });

        if (MediaSourceGet()==MEDIA_SOURCE_SD)
        {
            if (MediaTypeGet()==MEDIA_TYPE_VIDEO) { // SD Video
                InitialVideoSDcard(checkVideoPath());
            }
            else {    // SD Music
                InitialMusicSDcard(checkMusicPath());
            }
        }else{
            InitialMediaRAWStream();
        }
    }

    //=================================================================
    // 取出Video影片中的frame做為bitmap --- 方法二使用
    //=================================================================
    //private Bitmap decodeFrame(String path,long timeMs) {
    //    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    //    retriever.setDataSource(path);
    //    return retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
    //}

    //=================================================================
    // Initial RAW source
    //=================================================================
    private void InitialMediaRAWStream(){

        if (MediaTypeGet()==MEDIA_TYPE_VIDEO) {   // Video
            uriPath = "android.resource://" + getPackageName() + "/raw/" + "samsung";
        }else { // Music
            uriPath = "android.resource://" + getPackageName() + "/raw/" + "aa";
        }
        Uri UrlPath = Uri.parse(uriPath);
        VideoPath.setText(uriPath);
        videoView.setVideoURI(UrlPath);
        videoView.requestFocus();
    }

    //=================================================================
    // Initial SD card video source
    //=================================================================
    private void InitialVideoSDcard(String folder){
        if (checkSDCard()){
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                files = getVideos(folder);  // 將此路徑裡的相關檔案取出

                if ((files != null ? files.length : 0) != 0) {
                    Log.w(TAG, "folder not empty:"+files.length);
                    VideoPath.setText(getRealPathFromURI(Uri.fromFile(files[Videoindex])));
                    videoView.setVideoURI(Uri.fromFile(files[Videoindex]));  // 用URI方式
                    videoView.requestFocus();
                }else{
                    Log.w(TAG,"Without Video Files.");
                }
            }else{
                Log.w(TAG, "MEDIA_MOUNTED fail.");
            }
        }else {
            Log.w(TAG, "SD card doesn't exist.");
        }
    }

    // 將取得的檔案放至array
    private File[] getVideos(String path){
        File folder= new File(path);
        if (folder.isDirectory()){
            return folder.listFiles(videoFilter);
        }
        return null;
    }

    //=================================================================
    // Initial SD card music source
    //=================================================================
    private void InitialMusicSDcard(String folder){
        if (checkSDCard()){
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Music_Files = getMusics(folder);  // 將此路徑裡的相關檔案取出

                if ((Music_Files != null ? Music_Files.length : 0) != 0) {
                    Log.w(TAG, "folder not empty:"+Music_Files.length);
                    VideoPath.setText(getRealPathFromURI(Uri.fromFile(Music_Files[Videoindex])));

                    videoView.setVideoURI(Uri.fromFile(Music_Files[Videoindex]));  // 用URI方式
                    videoView.requestFocus();
                }else{
                    Log.w(TAG,"Without Music Files.");
                }
            }else{
                Log.w(TAG, "MEDIA_MOUNTED fail.");
            }
        }else {
            Log.w(TAG, "SD card doesn't exist.");
        }
    }

    private File[] getMusics(String path){
        File folder= new File(path);
        if (folder.isDirectory()){
            return folder.listFiles(musicFilter);
        }
        return null;
    }
/*
    // 將 URI 轉成 path 字串回傳
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentURI, null, null, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }*/

    //=================================================================
    // Video Type 儲存/讀取
    // RAW=0 or SDCard=1
    //=================================================================
    private boolean MediaSourceGet(){
        return mem_MediaSource.getBoolean("MEDIA_SOURCE", true);
    }

    private void MediaSourceSet(boolean bEnable){
        SharedPreferences.Editor editor = mem_MediaSource.edit(); //獲取編輯器
        editor.putBoolean("MEDIA_SOURCE", bEnable);
        editor.apply();
        editor.commit();    //提交
        VideoBrowserB.setEnabled(bEnable);
    }

    //=================================================================
    // Media Type 儲存/讀取
    // 0:music, 1:video
    //=================================================================
    private boolean MediaTypeGet(){
        return mem_MediaType.getBoolean("MEDIA_TYPE", true);
    }

    private void MediaTypeSet(boolean bEnable){
        SharedPreferences.Editor editor = mem_MediaType.edit(); //獲取編輯器
        editor.putBoolean("MEDIA_TYPE", bEnable);
        editor.apply();
        editor.commit();    //提交
    }

    //=================================================================
    // VideoView Touch event
    //=================================================================
    private View.OnTouchListener viewTouch= new View.OnTouchListener() {
        GestureDetector mGesture;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mGesture == null) {
                mGesture = new GestureDetector(VideoActivity.this, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {

                        Log.w(TAG, "onDown");
                        if (isRecording){
                            showToastIns(getApplicationContext(),
                                    getString(R.string.RecordingNoAvailable),
                                    Toast.LENGTH_SHORT);
                        }else {
                            VideoPlay();    // 按View的地方播放或暫停
                        }
                        return true; //返回false的話只能響應長按事件
                    }
                    @Override
                    public void onLongPress(MotionEvent e) {
                        Log.w(TAG, "onLongPress");
                        if (isRecording) {
                            showToastIns(getApplicationContext(),
                                    getString(R.string.RecordingNoAvailable),
                                    Toast.LENGTH_SHORT);
                        }else {
                            VideoPause();   // 長按時暫停
                        }
                        super.onLongPress(e);
                    }
                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        Log.w(TAG, "onScroll");
                        return super.onScroll(e1, e2, distanceX, distanceY);
                    }
                });
            }

            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    Log.w(TAG, "ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.w(TAG, "ACTION_UP");
                    view.performClick();    // 加入此行可解除warning message
                    break;
            }
            return mGesture.onTouchEvent(motionEvent);
        }
    };

    //=================================================================
    // SeekBar Event
    //=================================================================
    private SeekBar.OnSeekBarChangeListener seekBarOnSeekBarChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //停止拖曳時觸發事件
            Log.w(TAG, "放開bar:"+seekBar.getProgress());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //開始拖曳時觸發事件
            Log.w(TAG, "按下bar:"+seekBar.getProgress());
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //拖曳途中觸發事件，回傳參數 progress 告知目前拖曳數值
            //Media_BarText.setText("目前:" + progress);
            //Log.w(TAG, "進度:"+progress);
            if (fromUser)
                videoView.seekTo(progress);
        }
    };

    //=================================================================
    // Switch Event
    //=================================================================
    private CompoundButton.OnCheckedChangeListener VideoSW= new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (R.id.MediaSourceSwitch ==compoundButton.getId()) {
                MediaSourceSet(b);
                InitialVideoPlayer();
            }else if (R.id.MediaTypeSwitch ==compoundButton.getId()){
                MediaTypeSet(b);
                InitialVideoPlayer();
            }
        }
    };

    //=================================================================
    // FF video
    //=================================================================
    private void VideoFF(){
        if (videoView!=null && videoView.canSeekForward()) {
            int position = videoView.getCurrentPosition();
            int total = videoView.getDuration();

            //Log.w(TAG,"Total:"+videoView.getDuration());

            if (position+10000>total)
                videoView.seekTo(total);
            else
                videoView.seekTo(position + 10000);
        }
    }

    //=================================================================
    // RW video
    //=================================================================
    private void VideoRW(){
        if (videoView!=null && videoView.canSeekBackward()) {
            int position = videoView.getCurrentPosition();

            if (position-10000<0)
                videoView.seekTo(0);
            else
                videoView.seekTo(position - 10000);
        }
    }

    //=================================================================
    // Play video
    //=================================================================
    private void VideoPlay(){
        if (videoView.isPlaying()) {
            VideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
            videoView.pause();
        }
        else {
            myHandler.postDelayed(UpdateSongTime, 100); // 開始更新seekbar

            VideoPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            videoView.start();
        }
    }

    //=================================================================
    // Pause video
    //=================================================================
    private void VideoPause(){
        if (videoView.isPlaying()) {
            VideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
            videoView.pause();
        }
    }

    //=================================================================
    // Initial seekbar
    //=================================================================
    @SuppressLint("DefaultLocale")
    private void InitSeekBar(){
        finalTime = videoView.getDuration();
        startTime = videoView.getCurrentPosition();
        //Log.w(TAG, "total:"+finalTime);

        endTimeField.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes((long) finalTime))));
        startTimeField.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes((long) startTime))));

        VideoSeek.setMax(finalTime);
        VideoSeek.setProgress(startTime);
    }

    //=================================================================
    // Update seekbar
    //=================================================================
    private Runnable UpdateSongTime = new Runnable() {
        @SuppressLint("DefaultLocale")
        public void run() {
            if (videoView.isPlaying()){
                startTime = videoView.getCurrentPosition();
                startTimeField.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
                );
                VideoSeek.setProgress(startTime);
                myHandler.postDelayed(this, 100);
            }
        }
    };

    //=================================================================
    // Button Event
    //=================================================================
    private View.OnClickListener VideoButton=new View.OnClickListener() {
        Intent intent;

        @Override
        public void onClick(View view) {
            switch(view.getId()){

                case R.id.VideoPlayImg:
                    //Log.w(TAG, "VideoActivity: play:"+videoView.isPlaying());
                    if (isRecording) {
                        showToastIns(getApplicationContext(),
                                getString(R.string.RecordingNoAvailable),
                                Toast.LENGTH_SHORT);
                    }else{
                        VideoPlay();
                    }
                    break;

                case R.id.VideoFFImg:
                    if (isRecording) {
                        showToastIns(getApplicationContext(),
                                getString(R.string.RecordingNoAvailable),
                                Toast.LENGTH_SHORT);
                    }else {
                        VideoFF();
                    }
                    break;

                case R.id.VideoRewImg:
                    if (isRecording) {
                        showToastIns(getApplicationContext(),
                                getString(R.string.RecordingNoAvailable),
                                Toast.LENGTH_SHORT);
                    }else {
                        VideoRW();
                    }
                    break;

                case R.id.RecordImg:
                    if (isRecording){
                        StopRecord();
                    }else {
                        AudioRecording();
                    }
                    break;

                case R.id.VideoPreviousImg:
                    if (isRecording) {
                        showToastIns(getApplicationContext(),
                                getString(R.string.RecordingNoAvailable),
                                Toast.LENGTH_SHORT);
                    }else{
                        if (videoView.isPlaying()) {
                        videoView.resume(); //回至最前面
                    }
                    else {
                            if (MediaSourceGet()==MEDIA_SOURCE_SD)
                            {
                                if (MediaTypeGet()==MEDIA_TYPE_VIDEO) {
                                    if (Videoindex > 0) {
                                        Videoindex--;
                                    } else {
                                        Videoindex = files.length - 1;
                                    }
                                    videoView.setVideoURI(Uri.fromFile(files[Videoindex]));  // 用URI方式
                                    VideoPath.setText(getRealPathFromURI(Uri.fromFile(files[Videoindex])));
                                } else {
                                    if (Videoindex > 0) {
                                        Videoindex--;
                                    } else {
                                        Videoindex = Music_Files.length - 1;
                                    }
                                    videoView.setVideoURI(Uri.fromFile(Music_Files[Videoindex]));  // 用URI方式
                                    VideoPath.setText(getRealPathFromURI(Uri.fromFile(Music_Files[Videoindex])));
                                }
                            } else {
                                videoView.resume(); //回至最前面
                            }
                        }
                    }
                    break;

                case R.id.VideoNextImg:
                    if (isRecording) {
                        showToastIns(getApplicationContext(),
                                getString(R.string.RecordingNoAvailable),
                                Toast.LENGTH_SHORT);
                    }else {
                        if (MediaSourceGet()==MEDIA_SOURCE_SD)
                        {
                            if (MediaTypeGet()==MEDIA_TYPE_VIDEO) {
                                if (Videoindex < (files.length - 1)) {
                                    Videoindex++;
                                } else {
                                    Videoindex = 0;
                                }
                                videoView.setVideoURI(Uri.fromFile(files[Videoindex]));  // 用URI方式
                                VideoPath.setText(getRealPathFromURI(Uri.fromFile(files[Videoindex])));
                            } else {
                                if (Videoindex < (Music_Files.length - 1)) {
                                    Videoindex++;
                                } else {
                                    Videoindex = 0;
                                }
                                videoView.setVideoURI(Uri.fromFile(Music_Files[Videoindex]));  // 用URI方式
                                VideoPath.setText(getRealPathFromURI(Uri.fromFile(Music_Files[Videoindex])));

                            }
                        } else {
                            videoView.seekTo(finalTime);
                        }
                    }
                    break;

                case R.id.VideoBrowserBtn:
                    if (MediaTypeGet())   // 0:music, 1:Video
                        setFromWhichActivity(FROM_VIDEO_ACTIVITY);
                    else
                        setFromWhichActivity(FROM_MUSIC_ACTIVITY);

                    intent = new Intent(VideoActivity.this, FileManagerActivity.class);
                    startActivity(intent);
                    break;

                case R.id.RecBrowserButton:
                    setFromWhichActivity(FROM_RECORD_ACTIVITY);
                    intent = new Intent(VideoActivity.this, FileManagerActivity.class);
                    startActivity(intent);
                    break;
            }
        }

    };

    // 被始化錄音器
    private void InitialRecorder(){
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  // 設定錄音來源
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);  // 設定輸出格式
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB); // 設定編碼方式
        myAudioRecorder.setOutputFile(recFile);  // 設定輸出的檔案名稱
    }

    // 開始錄音
    private void AudioRecording(){
        AudioRecord.setImageResource(android.R.drawable.presence_audio_busy);
        isRecording = true;

        if (myAudioRecorder!=null) {
            try {
                myAudioRecorder.prepare();  // 使用設定的內容準備錄音
                myAudioRecorder.start();    // 開始錄音
                RecordHint.setText(getString(R.string.startRcord));
                RecordHint.setTextColor(Color.RED);

                Log.w(TAG, getString(R.string.startRcord));

                showToastIns(getApplicationContext(),
                        getString(R.string.startRcord),
                        Toast.LENGTH_SHORT);
                VideoSeek.setClickable(false);
                rec_Time=0;
                RecordTime.setText(finalRecordTime(rec_Time));
                RecordTime.setTextColor(Color.RED);
                recHandler.postDelayed(UpdateRecordTime, 1000); // 開始更新

                VideoBrowserB.setEnabled(false);

            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 停止錄音
    private void StopRecord(){
        AudioRecord.setImageResource(android.R.drawable.presence_audio_online);
        isRecording=false;

        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder  = null;

        VideoSeek.setClickable(true);

        Log.w(TAG, getString(R.string.stopRecord));
        showToastIns(getApplicationContext(),
                getString(R.string.stopRecord),
                Toast.LENGTH_SHORT);

        if (MediaTypeGet()==MEDIA_TYPE_MUSIC)
            InitialMusicSDcard(checkMusicPath());   // 重新讀取資料夾裡檔案

        InitialRecorder();  // 因為stop有release，需要重新初始化

        RecordHint.setText(getString(R.string.stopRecord));
        RecordHint.setTextColor(Color.BLUE);
        RecordTime.setTextColor(Color.BLUE);

        VideoBrowserB.setEnabled(true);
    }

    //=================================================================
    // Update recorder time
    //=================================================================
    private Runnable UpdateRecordTime = new Runnable() {
        public void run() {
            if (isRecording){
                rec_Time++;
                //Log.w(TAG, ""+rec_Time);
                if (rec_Time>=600)  // 限時10分鐘
                {
                    StopRecord();
                }
                RecordTime.setText(finalRecordTime(rec_Time));
                recHandler.postDelayed(this, 1000);
            }
        }
    };

    // 錄音時間字串，counter轉成string
    private String finalRecordTime(int count){
        String temp;
        int min, sec;

        if (count>0) {
            min = count / 60;
            sec = count % 60;
        }else{
            min=0;
            sec=0;
        }

        if (sec<10)
            temp=min+":0"+sec;
        else
            temp = min+":"+sec;

        return temp+"/10:00";
    }

    void saveRECPath(String path){
        Log.w(TAG, "save rec path:"+path);
        SharedPreferences.Editor editor = mem_MediaRecordPath.edit(); //獲取編輯器
        editor.putString("MEDIA_RECORDER_PATH", path);
        editor.apply();
        editor.commit();    //提交
    }

    private String checkRECPath(){
        String recpath;

        Intent intent = this.getIntent();//取得傳遞過來的資料
        recpath = intent.getStringExtra("rec_path");
        //Log.w(TAG, "rec path:"+recpath);

        if (recpath == null){    // 直接進入video activity會得到null
            recpath=mem_MediaRecordPath.getString("MEDIA_RECORDER_PATH", "");

            if (recpath.isEmpty()){  // 還沒有存路徑
                //Log.w(TAG, "use default path");
                recpath = default_rec_path;
                saveRECPath(recpath);
            }
        }else {
            saveRECPath(recpath);
        }
        return recpath;
    }

    void saveVideoPath(String path){
        SharedPreferences.Editor editor = mem_MediaVideoPath.edit(); //獲取編輯器
        editor.putString("MEDIA_VIDEO_PATH", path);
        editor.apply();
        editor.commit();    //提交
    }
	
    private String checkVideoPath(){
        String path;

        Intent intent = this.getIntent();//取得傳遞過來的資料
        path = intent.getStringExtra("video_path");
        //Log.w(TAG, "video path:"+path);

        if (path == null){    // 直接進入video activity會得到null
            path=mem_MediaVideoPath.getString("MEDIA_VIDEO_PATH", "");

            if (path.isEmpty()){  // 還沒有存路徑
                //Log.w(TAG, "use default path");
                path = default_video_path;
                saveVideoPath(path);
            }
        }else {
            saveVideoPath(path);
        }
        return path;
    }

    void saveMusicPath(String path){
        SharedPreferences.Editor editor = mem_MediaMusicPath.edit(); //獲取編輯器
        editor.putString("MEDIA_MUSIC_PATH", path);
        editor.apply();
        editor.commit();    //提交
    }
	
    private String checkMusicPath(){
        String path;

        Intent intent = this.getIntent();//取得傳遞過來的資料
        path = intent.getStringExtra("music_path");
        //Log.w(TAG, "music path:"+path);

        if (path == null){    // 直接進入video activity會得到null
            path=mem_MediaMusicPath.getString("MEDIA_MUSIC_PATH", "");

            if (path.isEmpty()){  // 還沒有存路徑
                //Log.w(TAG, "use default path");
                path = default_music_path;
                saveMusicPath(path);
            }
        }else {
            saveMusicPath(path);
        }
        return path;
    }

}
