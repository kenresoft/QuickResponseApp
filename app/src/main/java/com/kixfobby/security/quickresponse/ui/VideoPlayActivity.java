package com.kixfobby.security.quickresponse.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kixfobby.security.quickresponse.BaseActivity;
import com.kixfobby.security.quickresponse.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VideoPlayActivity extends BaseActivity {

    public static final int PERMISSION_READ = 0;
    private VideoView videoView;
    private ImageView prev, next, pause;
    private SeekBar seekBar;
    private int video_index = 0;
    private Uri video_uri;
    private String video_type, file;
    private double current_pos, total_duration;
    private TextView current, total;
    private LinearLayout showProgress;
    private Handler mHandler, handler;
    private boolean isVisible = true;
    private RelativeLayout relativeLayout;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        setSupportActionBar(findViewById(R.id.toolbar));

        if (checkPermission()) {
            setVideo();
        }
        if (getSupportActionBar().isShowing()) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(file);
        }
    }

    public void setVideo() {

        videoView = findViewById(R.id.videoview);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        pause = findViewById(R.id.pause);
        seekBar = findViewById(R.id.seekbar);
        current = findViewById(R.id.current);
        total = findViewById(R.id.total);
        showProgress = findViewById(R.id.showProgress);
        relativeLayout = findViewById(R.id.relative);

        video_index = getIntent().getIntExtra("pos", 0);
        video_uri = getIntent().getData();
        video_type = getIntent().getType();
        String path = Objects.requireNonNull(getIntent().getData()).getPath();
        if (path != null) {
            int i = path.lastIndexOf("/");
            file = path.substring(i + 1);
        } else file = getString(R.string.unnamed);

        mHandler = new Handler();
        handler = new Handler();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                /*video_index++;
                if (video_index < (videoArrayList.size())) {
                    playVideo(video_index);
                } else {*/
                video_index = 0;
                playVideo(video_index);
                // }
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                setVideoProgress();
            }
        });

        playVideo(video_index);
        /*prevVideo();
        nextVideo();*/
        setPause();
        hideLayout();
    }

    // play video file
    @SuppressLint("ShowToast")
    public void playVideo(int pos) {
        if (video_type.equals("video/mp4")) {
            try {
                videoView.setVideoURI(video_uri);
                videoView.start();
                pause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                video_index = pos;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(video_uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            finish();
        }
    }

    // display video progress
    public void setVideoProgress() {
        //get the video duration
        current_pos = videoView.getCurrentPosition();
        total_duration = videoView.getDuration();

        //display video duration
        total.setText(timeConversion((long) total_duration));
        current.setText(timeConversion((long) current_pos));
        seekBar.setMax((int) total_duration);
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    current_pos = videoView.getCurrentPosition();
                    current.setText(timeConversion((long) current_pos));
                    seekBar.setProgress((int) current_pos);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException ed) {
                    ed.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);

        //seekbar change listner
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                current_pos = seekBar.getProgress();
                videoView.seekTo((int) current_pos);
            }
        });
    }

    //play previous video
    public void prevVideo() {
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video_index > 0) {
                    video_index--;
                    playVideo(video_index);
                } else {
                    /*video_index = videoArrayList.size() - 1;
                    playVideo(video_index);*/
                }
            }
        });
    }

    //play next video
    public void nextVideo() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (video_index < (videoArrayList.size() - 1)) {
                    video_index++;
                    playVideo(video_index);
                } else {
                    video_index = 0;
                    playVideo(video_index);
                }*/
            }
        });
    }

    //pause video
    public void setPause() {
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    pause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                } else {
                    videoView.start();
                    pause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                }
            }
        });
    }

    //time conversion
    @SuppressLint("DefaultLocale")
    public String timeConversion(long value) {
        String songTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            songTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            songTime = String.format("%02d:%02d", mns, scs);
        }
        return songTime;
    }

    // hide progress when the video is playing
    public void hideLayout() {

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                showProgress.setVisibility(View.GONE);
                getSupportActionBar().hide();
                isVisible = false;
            }
        };
        handler.postDelayed(runnable, 5000);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(runnable);
                if (isVisible) {
                    showProgress.setVisibility(View.GONE);
                    getSupportActionBar().hide();
                    isVisible = false;
                } else {
                    showProgress.setVisibility(View.VISIBLE);
                    getSupportActionBar().show();
                    mHandler.postDelayed(runnable, 5000);
                    isVisible = true;
                }
            }
        });

    }

    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if ((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_READ) {
            if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getApplicationContext(), "Please allow storage permission", Toast.LENGTH_LONG).show();
                } else {
                    setVideo();
                }
            }
        }
    }
}