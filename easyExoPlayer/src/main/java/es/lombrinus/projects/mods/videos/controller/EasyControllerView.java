package es.lombrinus.projects.mods.videos.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.util.Util;

import java.util.Formatter;
import java.util.Locale;

import es.lombrinus.projects.mods.videos.R;

/**
 * Created by antonio.hormigo on 9/5/17.
 */

public class EasyControllerView extends FrameLayout implements View.OnClickListener, EasyControllerListener {


    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;
    public static final String LONG_FORMAT = "%d:%02d:%02d";
    public static final String SHORT_FORMAT = "%02d:%02d";
    public static final String LIVE = "LIVE";

    /**
     * listener to thread player events
     */
    private EasyMediaPlayerControl controller;

    /**
     * seek bar to control video position
     */

    private TimeBar mSeekBar;

    /**
     * view to show current position
     */
    private TextView mCurrentPosition;

    /**
     * view to show time left
     */
    private TextView mTimeLeft;

    /**
     * view to show duration
     */
    private TextView mDuration;

    /**
     * separator text view
     */
    private TextView mSeparator;

    /**
     * play button view
     */
    private View mPlay;

    /**
     * pause button view
     */
    private View mPause;

    private View previousButton;
    private View nextButton;
    private View fastForwardButton;
    private View rewindButton;

    /**
     * update progress runnable, it call itself every 1000 milliseconds
     */
    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private boolean isAttachedToWindow;
    private boolean scrubbing;
    private int rewindMs;
    private int fastForwardMs;
    private int showTimeoutMs;
    private long hideAtMs;
    private final Timeline.Period period;
    private final Timeline.Window window;
    public boolean visibleControl = true;


    public EasyControllerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        period = new Timeline.Period();
        window = new Timeline.Window();
        initView();
    }

    public EasyControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        period = new Timeline.Period();
        window = new Timeline.Window();
        initView();
    }

    public EasyControllerView(Context context, EasyMediaPlayerControl controller) {
        super(context);

        this.controller = controller;
        period = new Timeline.Period();
        window = new Timeline.Window();

        initView();
    }

    private void initView() {

        setAlpha(0.0f);
        setVisibility(GONE);

        rewindMs = PlaybackControlView.DEFAULT_REWIND_MS;
        fastForwardMs = PlaybackControlView.DEFAULT_FAST_FORWARD_MS;
        showTimeoutMs = PlaybackControlView.DEFAULT_SHOW_TIMEOUT_MS;

        View view = inflate(getContext(), R.layout.easy_controller_layout, null);
        addView(view);

        mPlay = view.findViewById(R.id.eControllerPlay);
        mPlay.setOnClickListener(this);

        mPause = view.findViewById(R.id.eControllerPause);
        mPause.setOnClickListener(this);

        previousButton = findViewById(com.google.android.exoplayer2.ui.R.id.exo_prev);
        if (previousButton != null) {
            previousButton.setOnClickListener(this);
        }
        nextButton = findViewById(com.google.android.exoplayer2.ui.R.id.exo_next);
        if (nextButton != null) {
            nextButton.setOnClickListener(this);
        }
        rewindButton = findViewById(com.google.android.exoplayer2.ui.R.id.exo_rew);
        if (rewindButton != null) {
            rewindButton.setOnClickListener(this);
        }
        fastForwardButton = findViewById(com.google.android.exoplayer2.ui.R.id.exo_ffwd);
        if (fastForwardButton != null) {
            fastForwardButton.setOnClickListener(this);
        }

        mCurrentPosition = (TextView) view.findViewById(R.id.actualPosition);
        mDuration = (TextView) view.findViewById(R.id.duration);
        mSeparator = (TextView) view.findViewById(R.id.separator);

        //mTimeLeft = (TextView) view.findViewById(R.id.timeLeft);

        mSeekBar = ((TimeBar) view.findViewById(R.id.seekBar));

        mSeekBar.setListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar) {
                removeCallbacks(hideAction);
                scrubbing = true;
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                if (mCurrentPosition != null) {
                    mCurrentPosition.setText(getFormattedTime((int) position));
                }
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                scrubbing = false;
                if (!canceled && controller != null && controller.getExoPlayer() != null) {
                    controller.getExoPlayer().seekTo(position);
                }
                hideAfterTimeout();
            }
        });
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
        return hours > 0 ? mFormatter.format(LONG_FORMAT, new Object[]{hours, minutes, seconds})
                .toString() : mFormatter.format(SHORT_FORMAT, new Object[]{minutes, seconds}).toString();
    }

    @Override
    public void onClick(View view) {

        if (controller == null)
            return;

        if (view == mPlay) {
            controller.start();
            mPlay.setVisibility(GONE);
            mPause.setVisibility(VISIBLE);
        } else if (view.getId() == mPause.getId()) {
            controller.pause();
            mPlay.setVisibility(VISIBLE);
            mPause.setVisibility(GONE);
        } else if (nextButton == view) {
            next();
        } else if (previousButton == view) {
            previous();
        } else if (fastForwardButton == view) {
            fastForward();
        } else if (rewindButton == view) {
            rewind();
        }

        hideAfterTimeout();

    }

    @Override
    public void showControl() {
        if (!isVisible()) {
            show();
        } else {
            hide();
        }
    }

    /**
     * Metodo que muestra los controles de video y reinicia el contador para que estos vuelvan a ocultarse
     */
    public void show() {

        if (isLive()) {
            mSeekBar.setEnabled(false);
            mCurrentPosition.setVisibility(GONE);
            mDuration.setVisibility(GONE);
            mSeparator.setText(LIVE);
        }

        if (!isVisible()) {
            visibleControl = true;
            setVisibility(VISIBLE);
            animate().alpha(1.0f).setDuration(500);
            updateAll();
            requestPlayPauseFocus();
        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();
    }

    /**
     * Hides the controller.
     */
    public void hide() {
        if (isVisible()) {
            visibleControl = false;
            animate().alpha(0.0f).setDuration(500);
            removeCallbacks(updateProgressAction);
            removeCallbacks(hideAction);
            hideAtMs = C.TIME_UNSET;
        }
    }

    public void hideInmediatly() {
        if (isVisible()) {
            visibleControl = false;
            setVisibility(GONE);
            removeCallbacks(updateProgressAction);
            removeCallbacks(hideAction);
            hideAtMs = C.TIME_UNSET;
        }
    }

    public boolean isVisible() {
        return visibleControl;
    }

    private void hideAfterTimeout() {
        removeCallbacks(hideAction);
        if (showTimeoutMs > 0) {
            hideAtMs = SystemClock.uptimeMillis() + showTimeoutMs;
            postDelayed(hideAction, showTimeoutMs);
        } else {
            hideAtMs = C.TIME_UNSET;
        }
    }

    private void updateAll() {
        updatePlayPauseButton();
        updateNavigation();
        updateProgress();
    }

    @Override
    public void updatePlayPauseButton() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        boolean requestPlayPauseFocus = false;
        boolean playing = controller != null && controller.getExoPlayer() != null && controller.getExoPlayer().getPlayWhenReady();
        if (mPlay != null) {
            requestPlayPauseFocus |= playing && mPlay.isFocused();
            mPlay.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (mPause != null) {
            requestPlayPauseFocus |= !playing && mPause.isFocused();
            mPause.setVisibility(!playing ? View.GONE : View.VISIBLE);
        }
        if (requestPlayPauseFocus) {
            requestPlayPauseFocus();
        }
    }

    private void updateNavigation() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        Timeline timeline = controller.getExoPlayer() != null ? controller.getExoPlayer().getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = timeline != null && !timeline.isEmpty();
        boolean isSeekable = false;
        boolean enablePrevious = false;
        boolean enableNext = false;
        if (haveNonEmptyTimeline) {
            int windowIndex = controller.getExoPlayer().getCurrentWindowIndex();
            timeline.getWindow(windowIndex, window);
            isSeekable = window.isSeekable;
            enablePrevious = windowIndex > 0 || isSeekable || !window.isDynamic;
            enableNext = (windowIndex < timeline.getWindowCount() - 1) || window.isDynamic;
            if (timeline.getPeriod(controller.getExoPlayer().getCurrentPeriodIndex(), period).isAd) {
                // Always hide player controls during ads.
                hide();
            }
        }
        setButtonEnabled(enablePrevious, null);
        setButtonEnabled(enableNext, null);
        setButtonEnabled(fastForwardMs > 0 && isSeekable, null);
        setButtonEnabled(rewindMs > 0 && isSeekable, null);
        if (mSeekBar != null) {
            mSeekBar.setEnabled(isSeekable);
        }
    }

    public boolean isLive() {
        if (controller != null && controller.getExoPlayer() != null
                && (controller.getExoPlayer().isCurrentWindowDynamic()
                || controller.getExoPlayer().getDuration() == C.TIME_UNSET)) {
            return true;
        } else {
            return false;
        }
    }

    private void updateProgress() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }

        long position = 0;
        long bufferedPosition = 0;
        long duration = 0;

        if (controller != null && controller.getExoPlayer() != null) {
            position = controller.getExoPlayer().getCurrentPosition();
            bufferedPosition = controller.getExoPlayer().getBufferedPosition();
            duration = controller.getExoPlayer().getDuration();
        }

        if (!isLive()) {

            mSeekBar.setEnabled(true);
            mCurrentPosition.setVisibility(VISIBLE);
            mDuration.setVisibility(VISIBLE);
            mSeparator.setText("/");

            if (mDuration != null) {
                mDuration.setText(getFormattedTime((int) duration));
            }
            if (mCurrentPosition != null && !scrubbing) {
                mCurrentPosition.setText(getFormattedTime((int) position));
            }
            if (mSeekBar != null) {
                mSeekBar.setPosition(position);
                mSeekBar.setBufferedPosition(bufferedPosition);
                mSeekBar.setDuration(duration);
            }
        }

        // Cancel any pending updates and schedule a new one if necessary.
        removeCallbacks(updateProgressAction);
        int playbackState = controller.getExoPlayer() == null ? ExoPlayer.STATE_IDLE : controller.getExoPlayer().getPlaybackState();
        if (playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
            long delayMs;
            if (controller.getExoPlayer().getPlayWhenReady() && playbackState == ExoPlayer.STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            postDelayed(updateProgressAction, delayMs);
        }
    }

    private void previous() {
        Timeline timeline = controller.getExoPlayer().getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        if (controller.getExoPlayer().getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                || (window.isDynamic && !window.isSeekable)) {
            seekTo(C.TIME_UNSET);
        } else {
            seekTo(0);
        }
    }

    private void next() {
        Timeline timeline = controller.getExoPlayer().getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        seekTo(C.TIME_UNSET);
    }

    private void rewind() {
        if (rewindMs <= 0) {
            return;
        }
        seekTo(Math.max(controller.getExoPlayer().getCurrentPosition() - rewindMs, 0));
    }

    private void fastForward() {
        if (fastForwardMs <= 0) {
            return;
        }
        seekTo(Math.min(controller.getExoPlayer().getCurrentPosition() + fastForwardMs, controller.getExoPlayer().getDuration()));
    }

    private void seekTo(long positionMs) {
        controller.getExoPlayer().seekTo(positionMs);
        updateProgress();
    }

    private void requestPlayPauseFocus() {
        boolean playing = controller != null && controller.getExoPlayer() != null && controller.getExoPlayer().getPlayWhenReady();
        if (!playing && mPlay != null) {
            mPlay.requestFocus();
        } else if (playing && mPause != null) {
            mPause.requestFocus();
        }
    }

    private void setButtonEnabled(boolean enabled, View view) {
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
        if (Util.SDK_INT >= 11) {
            setViewAlphaV11(view, enabled ? 1f : 0.3f);
            view.setVisibility(VISIBLE);
        } else {
            view.setVisibility(enabled ? VISIBLE : INVISIBLE);
        }
    }

    @TargetApi(11)
    private void setViewAlphaV11(View view, float alpha) {
        view.setAlpha(alpha);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        if (hideAtMs != C.TIME_UNSET) {
            long delayMs = hideAtMs - SystemClock.uptimeMillis();
            if (delayMs <= 0) {
                hideInmediatly();
            } else {
                postDelayed(hideAction, delayMs);
            }
        }
        updateAll();
    }
}