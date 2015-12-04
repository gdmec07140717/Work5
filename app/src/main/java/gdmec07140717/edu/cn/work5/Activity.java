package gdmec07140717.edu.cn.work5;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;


public class Activity extends android.app.Activity {
    private Display currDisplay;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer player;
    private int vWidth,vHeight;
    private Timer timer;
    private ImageButton rew; //快退
    private ImageButton pause;//暂停
    private ImageButton start;//开始
    private ImageButton ff;//快进
    private TextView play_time;//己播放时间
    private TextView	all_time;//总播放时f司
    private TextView	title;//播放文件名#
    private SeekBar seekbar;//进度条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Intent intent =  getIntent();
        Uri uri = intent.getData();
        String mPath = "";
        if(uri!=null){
            mPath = uri.getPath();
        }else {
            Bundle localBundle = getIntent().getExtras();
            if(localBundle!=null){
                String t_path =localBundle.getString("path");
                if(t_path!=null&&!"".equals(t_path)){
                    mPath=t_path;
                }
            }
        }
        title  = (TextView) findViewById(R.id.title);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        rew= (ImageButton) findViewById(R.id.rew);
        pause = (ImageButton) findViewById(R.id.pause);
        start = (ImageButton) findViewById(R.id.start);
        ff= (ImageButton) findViewById(R.id.ff);

        play_time = (TextView) findViewById(R.id.play_time);
        all_time= (TextView) findViewById(R.id.all_time);
        seekbar= (SeekBar) findViewById(R.id.seekbar);

        holder  =surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                player.setDisplay(holder);
                player.prepareAsync();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int heigth) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
        });
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                vWidth = player.getVideoWidth();
                vHeight = player.getVideoHeight();
                if (vWidth > currDisplay.getWidth() || vHeight > currDisplay.getHeight()) {
                    float wRatio = (float) vWidth / (float) currDisplay.getWidth();
                    float hRatio = (float) vHeight / (float) currDisplay.getHeight();
                    float ratio = Math.max(wRatio, hRatio);
                    vWidth = (int) Math.ceil((float) vWidth / ratio);
                    vHeight = (int) Math.ceil((float) vHeight / ratio);
                    surfaceView.setLayoutParams(new LinearLayout.LayoutParams(vWidth, vHeight));
                    player.start();
                } else {
                    player.start();
                }
                if (timer != null) {
                    timer.cancel();
                    timer = null;

                }
                timer = new Timer();
                timer.schedule(new MyTask(), 50, 500);
            }
        });
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            if(!mPath.equals("")){
                title.setText(mPath.substring(mPath.lastIndexOf("/")+1));
                player.setDataSource(mPath);

            }else {
                AssetFileDescriptor adf = this.getResources().openRawResourceFd(R.raw.exodus);
                player.setDataSource(adf.getFileDescriptor(),adf.getStartOffset(),adf.getDeclaredLength());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause.setVisibility(view.GONE);
                start.setVisibility(view.VISIBLE);
                player.pause();
                if(timer!=null){
                    timer.cancel();
                    timer=null;
                }
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause.setVisibility(view.GONE);
                start.setVisibility(view.VISIBLE);
                player.start();
                if(timer!=null){
                    timer.cancel();
                    timer=null;
                }
                timer =new Timer();
                timer.schedule(new MyTask(), 50, 500);
            }
        });
        rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player.isPlaying()){
                    int currenpo = player.getCurrentPosition();
                    if (currenpo-10000>0){
                        player.seekTo(currenpo-10000);
                    }
                }
            }
        });
        ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player.isPlaying()){
                    int currenpo = player.getCurrentPosition();
                    if (currenpo-10000<player.getDuration()){
                        player.seekTo(currenpo-10000);
                    }
                }
            }
        });
        currDisplay =this.getWindowManager().getDefaultDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0,1,0,"文件夹");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==1){
            Intent intent = new Intent(Activity.this,Activity2.class);

        }

        return super.onOptionsItemSelected(item);
    }
    public class  MyTask extends TimerTask{


        @Override
        public void run() {
            Message message= new Message();
            message.what=1;
            handler.sendMessage(message);
        }
    }

    private  final Handler handler = new Handler() {

        public void handleMessage(Message msg){

            switch (msg.what){
                case 1:
                    Time progress = new Time(player.getCurrentPosition());
                    Time allTime = new Time(player.getDuration());
                    String timrstr = progress.toString();
                    Log.v("progress", timrstr);
                    String timeStr2 = allTime.toString();
                    play_time.setText(timrstr.substring(timrstr.indexOf(":")+1));
                    all_time.setText(timeStr2.substring(timeStr2.indexOf(":")+1));
                    int progressvalue = 0;
                    if(player.getDuration()>0){
                        progressvalue = seekbar.getMax()*player.getCurrentPosition()/player.getDuration();

                    }
                    seekbar.setProgress(progressvalue);
                    break;
            }
            super.handleMessage(msg);
        }
    };

}
