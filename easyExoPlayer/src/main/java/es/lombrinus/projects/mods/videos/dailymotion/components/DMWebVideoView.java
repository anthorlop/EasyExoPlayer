package es.lombrinus.projects.mods.videos.dailymotion.components;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

public class DMWebVideoView extends WebView {

    public static final String ERROR = "error";
    public static final String APIREADY = "apiready";
    public static final String START = "start";
    public static final String LOADEDMETADATA = "loadedmetadata";
    public static final String TIMEUPDATE = "timeupdate";
    public static final String AD_TIMEUPDATE = "ad_timeupdate";
    public static final String PROGRESS = "progress";
    public static final String DURATIONCHANGE = "durationchange";
    public static final String SEEKING = "seeking";
    public static final String SEEKED = "seeked";
    public static final String FULLSCREENCHANGE = "fullscreenchange";
    public static final String VIDEO_START = "video_start";
    public static final String AD_START = "ad_start";
    public static final String AD_PLAY = "ad_play";
    public static final String PLAYING = "playing";
    public static final String PLAY = "play";
    public static final String END = "end";
    public static final String AD_PAUSE = "ad_pause";
    public static final String AD_END = "ad_end";
    public static final String VIDEO_END = "video_end";
    public static final String PAUSE = "pause";
    public static final String REBUFFER = "rebuffer";
    public static final String QUALITIESAVAILABLE = "qualitiesavailable";
    public static final String QUALITYCHANGE = "qualitychange";
    public static final String SUBTITLESAVAILABLE = "subtitlesavailable";
    public static final String SUBTITLECHANGE = "subtitlechange";
    private Context context;

    private int fullscreenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
    private int defaultOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
    private int margin = 0;
    private int height = 0;

    private boolean fullscreenMode = false;

    public void onFullscreenMode() {
        fullscreenMode = true;
        goToFullscreen();
    }

    public void onFullscreenClicked() {
        if (fullscreen) {
            backFromFullscreen();
        } else {
            goToFullscreen();
        }
    }

    private void backFromFullscreen() {
        // quitamos los margenes actuales
        if (getParent() instanceof RelativeLayout) {
            RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            lay.setMargins(margin, 0, margin, 0);
            lay.addRule(RelativeLayout.CENTER_IN_PARENT);
            this.setLayoutParams(lay);

            if (getParent().getParent() instanceof RelativeLayout) {
                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                layout.addRule(RelativeLayout.CENTER_IN_PARENT);
                ((RelativeLayout) getParent()).setLayoutParams(layout);
            }

            ((Activity) context).setRequestedOrientation(defaultOrientation);

            fullscreen = false;
            mListener.onFullscreenChange(false);
        }
    }

    private void goToFullscreen() {

        // quitamos los margenes actuales
        if (getParent() instanceof RelativeLayout) {
            height = ((RelativeLayout) getParent()).getHeight();
            MarginLayoutParams lp = (MarginLayoutParams) this.getLayoutParams();
            margin = lp.leftMargin;
            RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lay.setMargins(0, 0, 0, 0);
            this.setLayoutParams(lay);

            if (getParent().getParent() instanceof RelativeLayout) {
                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                ((RelativeLayout) getParent()).setLayoutParams(layout);
            }
            defaultOrientation = ((Activity) context).getRequestedOrientation();
            ((Activity) context).setRequestedOrientation(fullscreenOrientation);

            fullscreen = true;
            mListener.onFullscreenChange(true);
        }
    }

    public interface Listener {

        public void onEvent(String event);

    }

    public class Error {

        public String code;
        public String title;
        public String message;

        public Error(String c, String t, String m) {
            code = c;
            title = t;
            message = m;
        }

    }

    private WebSettings mWebSettings;
    private WebChromeClient mChromeClient;
    private VideoView mCustomVideoView;
    private WebChromeClient.CustomViewCallback mViewCallback;
    public static String DEFAULT_PLAYER_URL = "http://www.dailymotion.com/embed/video/";
    private String mBaseUrl = DEFAULT_PLAYER_URL;
    private final String mExtraUA = "; DailymotionEmbedSDK 1.0";
    private FrameLayout mVideoLayout;
    private boolean mIsFullscreen = false;
    private ViewGroup mRootLayout;
    private boolean mAutoPlay = true;
    private String mExtraParameters;
    private String mVideoId;
    private long mStartNanos;
    private DMEventListener mListener = null;

    public boolean apiReady = false;
    public boolean autoplay = false;
    public double currentTime = 0;
    public double bufferedTime = 0;
    public double duration = 0;
    public boolean seeking = false;
    public Object error = null;
    public boolean ended = false;
    public boolean paused = true;
    public boolean fullscreen = false;
    public boolean rebuffering = false;
    public String qualities = "";
    public String quality = "";
    public String subtitles = "";
    public String subtitle = "";

    public DMWebVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public DMWebVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DMWebVideoView(Context context) {
        super(context);
        init();
    }

    public void setBaseUrl(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    public void setExtraParameters(String extraParameters) {
        mExtraParameters = extraParameters;
    }

    public void setEventListener(DMEventListener listener) {
        mListener = listener;
    }

    private void init() {

        mWebSettings = getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setPluginState(WebSettings.PluginState.ON);
        mWebSettings.setUserAgentString(mWebSettings.getUserAgentString() + mExtraUA);
        if (Build.VERSION.SDK_INT >= 17) {
            mWebSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        mChromeClient = new WebChromeClient() {

            /**
             * The view to be displayed while the fullscreen VideoView is buffering
             * @return the progress view
             */
            @Override
            public View getVideoLoadingProgressView() {
                ProgressBar pb = new ProgressBar(getContext());
                pb.setIndeterminate(true);
                return pb;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                ((Activity) context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
                mIsFullscreen = true;

                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

                if (mListener != null) {
                    mListener.onFullscreenChange(mIsFullscreen);
                }

                mViewCallback = callback;
                if (view instanceof FrameLayout) {
                    FrameLayout frame = (FrameLayout) view;
                    if (frame.getFocusedChild() instanceof VideoView) {//We are in 2.3
                        VideoView video = (VideoView) frame.getFocusedChild();
                        frame.removeView(video);

                        setupVideoLayout(video);

                        mCustomVideoView = video;
                        mCustomVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                hideVideoView();
                            }
                        });


                    } else {//Handle 4.x

                        setupVideoLayout(view);

                    }
                }
            }

            @Override
            public Bitmap getDefaultVideoPoster() {
                int colors[] = new int[1];
                colors[0] = Color.TRANSPARENT;
                Bitmap bm = Bitmap.createBitmap(colors, 0, 1, 1, 1, Bitmap.Config.ARGB_8888);
                return bm;
            }

            @Override
            public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) // Only available in API level 14+
            {
                onShowCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                hideVideoView();
            }

        };


        setWebChromeClient(mChromeClient);
        setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                if (uri.getScheme().equals("dmevent")) {
                    String event = uri.getQueryParameter("event");
                    if (event.equals("apiready")) {
                        if (mAutoPlay) {
                            callPlayerMethod("play");
                        }
                        // DMLog.d(DMLog.STUFF, "apiready took " + ((double) (System.nanoTime() - mStartNanos)) / 1000000000.0);
                    }

                    switch (event)
                    {
                        case APIREADY: apiReady = true; break;
                        case START: ended = false; break;
                        case LOADEDMETADATA: error = null; break;
                        case TIMEUPDATE:
                        case AD_TIMEUPDATE: currentTime = Double.parseDouble(uri.getQueryParameter("time")); break;
                        case PROGRESS: bufferedTime = Double.parseDouble(uri.getQueryParameter("time")); break;
                        case DURATIONCHANGE: duration = Double.parseDouble(uri.getQueryParameter("duration")); break;
                        case SEEKING: seeking = true; currentTime = Double.parseDouble(uri.getQueryParameter("time")); break;
                        case SEEKED: seeking = false; currentTime = Double.parseDouble(uri.getQueryParameter("time")); break;
                        case FULLSCREENCHANGE: setFullscreen(parseBooleanFromAPI(uri.getQueryParameter("fullscreen"))); break;
                        case VIDEO_START:
                        case AD_START:
                        case AD_PLAY:
                        case PLAYING:
                        case PLAY: paused = false; break;
                        case END: ended = true; break;
                        case AD_PAUSE:
                        case AD_END:
                        case VIDEO_END:
                        case PAUSE: paused = true; break;
                        case ERROR:
                            error = new DMWebVideoView.Error(uri.getQueryParameter("code"), uri.getQueryParameter("title"), uri.getQueryParameter("message"));
                            break;
                        case REBUFFER: rebuffering = parseBooleanFromAPI(uri.getQueryParameter("rebuffering")); break;
                        case QUALITIESAVAILABLE: qualities = uri.getQueryParameter("qualities"); break;
                        case QUALITYCHANGE: quality = uri.getQueryParameter("quality"); break;
                        case SUBTITLESAVAILABLE: subtitles = uri.getQueryParameter("subtitles"); break;
                        case SUBTITLECHANGE: subtitle = uri.getQueryParameter("subtitle"); break;
                    }

                    callback(event);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }
        });
    }

    private void callback(String event) {
        if (mListener != null) {
            switch (event)
            {
                case START: mListener.onStart(); break;
                case PROGRESS: mListener.onBuffered(bufferedTime); break;
                case TIMEUPDATE:
                case AD_TIMEUPDATE:
                case SEEKING:
                case SEEKED: mListener.onProgress((int) currentTime); break;
                case PAUSE: mListener.onPause(); break;
                case PLAY: mListener.onPlay(); break;
                case ERROR: mListener.onError(error.toString()); break;
                case REBUFFER: mListener.onRebuffering(rebuffering); break;
                case VIDEO_END: mListener.onFinished();break;
            }
        }
    }

    private boolean parseBooleanFromAPI(String value) {
        if (value.equals("true") || value.equals("1")) {
            return true;
        }
        return false;
    }

    private void callPlayerMethod(String method) {
        loadUrl("javascript:player.api(\"" + method + "\")");
    }

    private void callPlayerMethod(String method, String param) {
        loadUrl("javascript:player.api(\"" + method + "\", \"" + param + "\")");
    }

    public void setVideoId(String videoId) {
        mVideoId = videoId;
    }

    public void load(Context context) {

        this.context = context;

        if (mRootLayout == null) {
            //The topmost layout of the window where the actual VideoView will be added to
            mRootLayout = (FrameLayout) ((Activity) context).getWindow().getDecorView();
        }

        String url = mBaseUrl + mVideoId + "?app=" + getContext().getPackageName() + "&api=location";
        if (mExtraParameters != null && !mExtraParameters.equals("")) {
            url += "&" + mExtraParameters;
        }

        // DMLog.d(DMLog.STUFF, "loading " + url);

        mStartNanos = System.nanoTime();
        loadUrl(url);
    }

    public void hideVideoView() {
        if (isFullscreen()) {
            if (mCustomVideoView != null) {
                mCustomVideoView.stopPlayback();
            }
            mRootLayout.removeView(mVideoLayout);
            mViewCallback.onCustomViewHidden();
            ((Activity) context).setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            mIsFullscreen = false;
            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            if (mListener != null) {
                mListener.onFullscreenChange(mIsFullscreen);
            }
        }
    }

    private void setupVideoLayout(View video) {

        /**
         * As we don't want the touch events to be processed by the underlying WebView, we do not set the WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE flag
         * But then we have to handle directly back press in our View to exit fullscreen.
         * Otherwise the back button will be handled by the topmost Window, id-est the player controller
         */
        mVideoLayout = new FrameLayout(getContext()) {

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    hideVideoView();
                    return true;
                }

                return super.dispatchKeyEvent(event);
            }
        };

        mVideoLayout.setBackgroundResource(android.R.color.black);
        if (video != null)
            mVideoLayout.addView(video);
        ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mRootLayout.addView(mVideoLayout, lp);
        mIsFullscreen = true;
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        if (mListener != null) {
            mListener.onFullscreenChange(mIsFullscreen);
        }
    }

    public boolean isFullscreen() {
        return mIsFullscreen;
    }

    public void handleBackPress() {
        if (isFullscreen()) {
            hideVideoView();
        } else if (fullscreen && !fullscreenMode) {
            backFromFullscreen();
        } else {
            loadUrl("");//Hack to stop video
            ((Activity) context).finish();
        }
    }

    public boolean isAutoPlaying() {
        return mAutoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        mAutoPlay = autoPlay;
    }

    public void play() {
        callPlayerMethod("play");
    }

    public void togglePlay() {
        callPlayerMethod("toggle-play");
    }

    public void pause() {
        callPlayerMethod("pause");
    }

    public void seek(double time) {
        callPlayerMethod("seek", Double.toString(time));
    }

    public void setQuality(String quality) {
        callPlayerMethod("quality", quality);
    }

    public void setSubtitle(String language_code) {
        callPlayerMethod("subtitle", language_code);
    }

    public void setControls(boolean visible) {
        callPlayerMethod("controls", (visible ? "true" : "false"));
    }

    public void toggleControls() {
        callPlayerMethod("toggle-controls");
    }

    public void setRootViewGroup(ViewGroup rootView) {
        mRootLayout = rootView;
    }

    /**
     * Set a value from fullscreenOrientation variable
     */
    public void setFullscreenOrientation(int fullscreenOrientation) {
        this.fullscreenOrientation = fullscreenOrientation;
    }

    /**
     * Set a value from fullscreen variable
     */
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }
}