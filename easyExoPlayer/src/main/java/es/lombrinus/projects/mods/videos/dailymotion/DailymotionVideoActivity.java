package es.lombrinus.projects.mods.videos.dailymotion;

import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import es.lombrinus.projects.mods.videos.dailymotion.components.DMEventListener;
import es.lombrinus.projects.mods.videos.dailymotion.components.DMWebVideoView;
import es.lombrinus.projects.mods.videos.dailymotion.components.DailymotionConfiguration;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

import es.lombrinus.projects.mods.videos.R;

/**
 * Actividad que reproduce un video de dailymotion con ID enviado con la clave ID_VIDEO_KEY.
 *
 * Se puede configurar su funcionamiento enviando un objeto DailymotionConfiguration con las opciones deseadas. Para ello usaremos la clave CONFIGURATION_KEY.
 *
 * Created by antonio.hormigo on 10/10/16.
 */

public class DailymotionVideoActivity extends AppCompatActivity {

    public final static String ID_VIDEO_KEY = "ID_VIDEO_KEY";
    public final static String CONFIGURATION_KEY = "CONFIGURATION_KEY";
    public static final int DURATION = 400;
    private static final int HIDE_PROGRESS = 1;
    private static final int SHOW_PROGRESS = 2;

    private DMWebVideoView mVideoView;

    private DailymotionConfiguration config = new DailymotionConfiguration();

    private View mPlay;
    private View mPause;
    private View mBack;
    private View mFull;

    private View viewBg;

    private View mBottomControls;

    private SeekBar mSeekBar;

    private TextView mCurrentTime;

    private int progressTime = 0;
    private boolean mFromUser = false;
    private boolean playClicked = false;

    private String mIdVideo;
    private Handler mHandler = new MessageHandler(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dailymotion_player);

        if (getIntent() != null && getIntent().hasExtra(ID_VIDEO_KEY)) {
            mIdVideo = getIntent().getStringExtra(ID_VIDEO_KEY);

            if (getIntent().hasExtra(CONFIGURATION_KEY)) {
                config = getIntent().getParcelableExtra(CONFIGURATION_KEY);
            }


            loadViews();
            loadVideo();

        } else {
            finish();
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    }

    private void loadViews() {

        viewBg = findViewById(R.id.viewBg);
        mVideoView = (DMWebVideoView) findViewById(R.id.dmWebVideoView);
        mBottomControls = findViewById(R.id.bottomControls);
        mBottomControls.setVisibility(View.GONE);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        mCurrentTime = (TextView) findViewById(R.id.currentTimeText);
        if (config.getCurrentTimeTextColor() != 0)
            mCurrentTime.setTextColor(getColorResource(config.getCurrentTimeTextColor()));

        int color = getColorResource(config.getSeekBarColor());
        mSeekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSeekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        mPlay = findViewById(R.id.playBtn);
        setButtonConfig(config.getPlayIconResource(), mPlay);
        mPause = findViewById(R.id.pauseBtn);
        setButtonConfig(config.getPauseIconResource(), mPause);
        mBack = findViewById(R.id.backBtn);
        setButtonConfig(config.getBackIconResource(), mBack);
        mFull = findViewById(R.id.fullscreenBtn);
        setButtonConfig(config.getFullscreenIconResource(), mFull);

        if (!config.isAllowFullscreen()) {
            mFull.setVisibility(View.INVISIBLE);
        }

        if (config.isOnlyFullscreen()) {
            mFull.setVisibility(View.INVISIBLE);
            mVideoView.onFullscreenMode();
        }

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoView.play();
            }
        });

        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoView.pause();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        viewBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!config.isAutoPlay() && !playClicked) {
                    playClicked = true;
                    mVideoView.play();
                } else {
                    show();
                }
            }
        });

        mFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoView.onFullscreenClicked();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    mFromUser = true;
                    progressTime = progress;
                    mCurrentTime.setText(getFormattedTime(progressTime * 1000) + " / " + getFormattedTime((int) mVideoView.duration * 1000));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mFromUser) {
                    mFromUser = false;
                    mVideoView.seek(progressTime);
                    mCurrentTime.setText(getFormattedTime(progressTime * 1000) + " / " + getFormattedTime((int) mVideoView.duration * 1000));
                }
            }
        });
    }

    private void setButtonConfig(int resource, View view) {
        if (resource != 0
                && view instanceof ImageView) {
            ((ImageView) view).setImageResource(resource);
        }

        if (config.getButtonsColor() != 0
                && view instanceof ImageView) {
            ((ImageView) view).setColorFilter(ContextCompat.getColor(this, config.getButtonsColor()));
        }
    }

    private int getColorResource(int res) {
        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = getColor(res);
        } else {
            color = getResources().getColor(res);
        }
        return color;
    }

    private void loadVideo() {

        mVideoView.setVideoId(mIdVideo);
        mVideoView.setAutoPlay(config.isAutoPlay());
        mVideoView.setFullscreenOrientation(config.getFullscreenOrientation());
        mVideoView.load(this);
        mVideoView.setControls(false);

        if (config.isAutoPlay())
            mPlay.setVisibility(View.VISIBLE);

        mVideoView.setEventListener(new DMEventListener() {
            @Override
            public void onStart() {
                show();
                mVideoView.setControls(false);
            }

            @Override
            public void onPlay() {
                mPause.setVisibility(View.VISIBLE);
                mPlay.setVisibility(View.GONE);
            }

            @Override
            public void onPause() {
                mPause.setVisibility(View.GONE);
                mPlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgress(int progress) {
                mSeekBar.setMax((int) mVideoView.duration);
                mSeekBar.setProgress(progress);
                mCurrentTime.setText(getFormattedTime(progress * 1000) + " / " + getFormattedTime((int) mVideoView.duration * 1000));
            }

            @Override
            public void onBuffered(double progress) {

            }

            @Override
            public void onRebuffering(boolean rebuffering) {
            }

            @Override
            public void onError(String error) {
                finish();
                Toast.makeText(DailymotionVideoActivity.this, R.string.dailymotion_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFullscreenChange(boolean fullscreen) {
                if (fullscreen) {
                    setButtonConfig(config.getCloseFullscreenIconResource(), mFull);
                } else {
                    setButtonConfig(config.getFullscreenIconResource(), mFull);
                }
            }

            @Override
            public void onFinished() {
                finish();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mVideoView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mVideoView.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        mVideoView.handleBackPress();
    }

    /**
     * Metodo que muestra los controles de video y reinicia el contador para que estos vuelvan a ocultarse
     *
     */
    private void show() {
        if (mBottomControls != null) {
            setVisibilityWithAnimation(View.VISIBLE, DURATION, mBack, mBottomControls);

            mHandler.sendEmptyMessage(SHOW_PROGRESS);

            Message msg = mHandler.obtainMessage(HIDE_PROGRESS);
            mHandler.removeMessages(HIDE_PROGRESS);
            mHandler.sendMessageDelayed(msg, 3000);
        }
    }

    private class MessageHandler extends Handler {
        private final WeakReference<DailymotionVideoActivity> activities;

        MessageHandler(DailymotionVideoActivity activity) {
            activities = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            DailymotionVideoActivity activity = activities.get();
            switch (msg.what) {
                case HIDE_PROGRESS:
                    if (activity != null && activity.mBack != null && activity.mBottomControls != null) {
                        setVisibilityWithAnimation(View.GONE, DURATION, activity.mBack, activity.mBottomControls);
                    }
                    break;
                case SHOW_PROGRESS:
                    if (activity != null && activity.mBottomControls != null && activity.mBottomControls.getVisibility() == View.VISIBLE) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (activity.mSeekBar.getProgress() % 1000));
                    }
                    break;
            }
        }
    }

    /**
     * Metodo que formatea el tiempo de milisegundos a texto en hora:minutos:segundos
     *
     * @param millis milisegundos
     * @return texto con el tiempo formateado
     */
    private String getFormattedTime(int millis) {
        StringBuilder mFormatBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = millis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60 % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        return hours > 0 ? mFormatter.format(config.getCurrentTimeLongFormat(), new Object[]{hours, minutes, seconds})
                .toString() : mFormatter.format(config.getCurrentTimeShortFormat(), new Object[]{minutes, seconds}).toString();
    }

    /**
     *
     * Metodo que modifica la visibilidad de las vistas pasadas por parámetro con una animación
     *
     * @param visibility visible or gone
     * @param duration animation time
     * @param views all views to change visibility
     */
    private void setVisibilityWithAnimation(final int visibility, int duration, View... views) {
        AlphaAnimation alphaAnimation;
        for (final View view : views) {
            alphaAnimation = null;
            if (visibility == View.VISIBLE && view.getVisibility() == View.GONE) {
                view.setVisibility(View.INVISIBLE);
                alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            } else if (visibility == View.GONE && view.getVisibility() == View.VISIBLE) {
                alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            }
            if (alphaAnimation != null) {
                alphaAnimation.setDuration(duration);
                alphaAnimation.setStartOffset(200);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(visibility);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                view.startAnimation(alphaAnimation);
            }
        }
    }
}
