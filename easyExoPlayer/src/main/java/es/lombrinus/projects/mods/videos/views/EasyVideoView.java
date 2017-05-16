package es.lombrinus.projects.mods.videos.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

import es.lombrinus.projects.mods.videos.R;
import es.lombrinus.projects.mods.videos.controller.EasyControllerListener;
import es.lombrinus.projects.mods.videos.controller.EasyMediaPlayerControl;

/**
 * View para la reprodución de vídeo
 *
 * @author anthorlop
 */
public class EasyVideoView extends SurfaceView implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, EasyMediaPlayerControl, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, ExoPlayer.EventListener, AudioRendererEventListener, MetadataRenderer.Output, TextRenderer.Output,
        VideoRendererEventListener {

    private Context mContext;

    private EasyControllerListener ueControllerListener;

    private boolean forceMediaPlayer = false;

    //---------------------
    //FORMATOS PREDEFINIDOS
    //---------------------
    public static float FORMAT_16_9 = 16f / 9f;
    public static float FORMAT_4_3 = 4f / 3f;

    //---------------------
    //EXOPLAYER INFO
    //---------------------
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    //---------------------
    //KEY FOR SAVEDINSTANCE
    //---------------------
    private static final String KEY_POSITION = "key_current_video_position";
    private static final String KEY_URL = "key_video_url";
    private static final String KEY_CAN_SEEK_FORWARD = "key_can_seek_forward";
    private static final String KEY_STATE = "instanceState";

    //    private static String TAG = "VideoView";
    private static final String DEFAULT_SCHEMA = "http://schemas.android.com/apk/res-auto";
    private static final int ID_ERROR_LOADING = -10000; // ERROR al crear, normalmente por URL malformada

    //---------------------
    //MEDIAPLAYER Y MEDIACONTROLLER
    //---------------------
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;

    private DataSource.Factory mediaDataSourceFactory;
    private Handler mainHandler;
    private ExoPlayer exoPlayer;
    private DefaultTrackSelector trackSelector;
    private boolean needRetrySource;
    private TrackGroupArray lastSeenTrackGroupArray;

    //---------------------
    //URL
    //---------------------
    private String mVideoUrl;
    private SurfaceHolder holder;
    private Surface surface;

    //---------------------
    //LISTENER
    //---------------------
    private VideoViewListener videoViewListener = null;

    //---------------------
    //VARIABLES
    //---------------------
    private boolean autoplay;
    private int resumeWindow;
    private long resumePosition;
    private boolean show_controls = true;
    private static String TAG = "UEVideoView";

    //---------------------
    //ESTADO DEL VÍDEO
    //---------------------
    private EasyVideoState eVideoState = EasyVideoState.NO_LOADED;
    private boolean surfaceDestroyed = true;
    private float videoRatio = FORMAT_16_9;
    private boolean canSeekForward = true;
    private int savedPosition = 0;

    //---------------------------------------------------------------------------------------------
    //------------------------------------INIT: NO_LOADED STATE------------------------------------
    //---------------------------------------------------------------------------------------------
    public EasyVideoView(Context context)
    {
        super(context);
        init(context, null);
    }

    public EasyVideoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public EasyVideoView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EasyVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs)
    {
//        Log.d(TAG, "[" + TAG + "][init]init");

        mContext = context;

        setKeepScreenOn(true);

        clearResumePosition();
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        holder = getHolder();
        holder.addCallback(this);
        autoplay = true;

        if (attrs != null)
        {
            autoplay = attrs.getAttributeBooleanValue(DEFAULT_SCHEMA, "autoplay", true);
            show_controls = attrs.getAttributeBooleanValue(DEFAULT_SCHEMA, "show_controls", true);
        }
    }

    //------------------------------------END: NO_LOADED STATE-------------------------------------

    //---------------------------------------------------------------------------------------------
    //-----------------------------------------INIT: LOADING---------------------------------------
    //---------------------------------------------------------------------------------------------
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
//        Log.d(TAG, "[" + TAG + "][surfaceCreated]Init");
        surfaceDestroyed = false;
        eVideoState = EasyVideoState.LOADING;
        this.holder = holder;

        if (videoViewListener != null)
            videoViewListener.onSurfaceCreated();
    }

    public void createPreparePlayer()
    {
        if (canUseExoPlayer()) {
            createPrepareExoPlayer();
        } else {
            createPrepareMediaPlayer();
        }
    }

    public void createPrepareMediaPlayer()
    {
        try
        {
            mediaController = new MediaController(mContext);
            mediaController.setAnchorView(this);
            if (mediaPlayer != null)
            {
                mediaPlayer.release();
            }
            // creo el media player cuando el surface ya esta creado
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDisplay(holder);
            if (!TextUtils.isEmpty(mVideoUrl))
                mediaPlayer.setDataSource(mVideoUrl);
            mediaPlayer.setOnPreparedListener(this);
            // listener para hacer que se salga del video cuando termine
            mediaPlayer.setOnCompletionListener(this);

            // listener para tratar los errores del video
            mediaPlayer.setOnErrorListener(this);

            // al controller que he creado anteriormente le asigno este media player
            mediaController.setMediaPlayer(this);

            // lanzo para preparar el video para su reproduccion
            mediaPlayer.prepareAsync();

            if (videoViewListener != null)
                videoViewListener.onLoading();

        } catch (Exception e)
        {
            e.printStackTrace();
            //Se reproduce un error normalmente de URL se lanza onError
            onError(mediaPlayer, ID_ERROR_LOADING, 0);
        }
    }

    public void createPrepareExoPlayer()
    {

        if (!canUseExoPlayer()) return;

        try
        {
            needRetrySource = false;
            initializePlayer();

            if (videoViewListener != null)
                videoViewListener.onLoading();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //-----------------------------------------END: LOADING----------------------------------------

    //---------------------------------------------------------------------------------------------
    //------------------------------------------INIT: READY----------------------------------------
    //---------------------------------------------------------------------------------------------

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
//        Log.d(TAG, "[" + TAG + "][onPrepared]Init");
        if (!surfaceDestroyed)
        {
            eVideoState = EasyVideoState.PREPARED;
            // muestro el mcontroller del vídeo
            if (mediaController != null)
                mediaController.setEnabled(true);

            if (mediaPlayer.getDuration() > 0)
            {
                seekTo(savedPosition);
            } else
            {
                if (mediaController != null)
                {
                    final int topContainerId1 = getResources().getIdentifier("mediacontroller_progress", "id", "android");
                    final int ffw = getResources().getIdentifier("ffwd", "id", "android");
                    final int rw = getResources().getIdentifier("rew", "id", "android");
                    final SeekBar seekbar = (SeekBar) mediaController.findViewById(topContainerId1);
                    final View ffView = mediaController.findViewById(ffw);
                    final View rwView = mediaController.findViewById(rw);

                    if (seekbar != null)
                        seekbar.setEnabled(false);

                    if (ffView != null)
                        ffView.setVisibility(View.INVISIBLE);
//                        ffView.setEnabled(false);

                    if (rwView != null)
                        rwView.setVisibility(View.INVISIBLE);
//                        rwView.setEnabled(false);

                }
            }

            //Hacemos un play y un pause para que el MediaController refleje bien el progress
            start(mediaPlayer);
            if (!autoplay)
            {
                pause();
                //Si el vídeo se ha iniciado detenido, muestro el controller indefinidamente
                mediaController.show(0);
            }
        }
    }

    //-------------------------------------------END: READY----------------------------------------

    //---------------------------------------------------------------------------------------------
    //----------------------------------------INIT: PLAYING----------------------------------------
    //---------------------------------------------------------------------------------------------

    public void start(MediaPlayer mediaPlayer)
    {
        if (!surfaceDestroyed)
        {
            try
            {
                if (mediaPlayer != null)
                {
                    mediaPlayer.start();
                    if (videoViewListener != null)
                        videoViewListener.onStart(mediaPlayer.getDuration());
                    eVideoState = EasyVideoState.PLAYING;
                } else if (exoPlayer != null) {
                    exoPlayer.setPlayWhenReady(true);
                    if (videoViewListener != null)
                        videoViewListener.onStart((int) exoPlayer.getDuration());
                    eVideoState = EasyVideoState.PLAYING;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start()
    {
        if (!surfaceDestroyed)
        {
            try
            {
                if (mediaPlayer != null)
                {
                    mediaPlayer.start();
                    if (videoViewListener != null)
                        videoViewListener.onStart(getDuration());
                    eVideoState = EasyVideoState.PLAYING;
                } else if (exoPlayer != null) {
                    exoPlayer.setPlayWhenReady(true);
                    if (ueControllerListener != null)
                        ueControllerListener.updatePlayPauseButton();
                    if (videoViewListener != null)
                        videoViewListener.onStart(getDuration());
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    //----------------------------------------END: PLAYING-----------------------------------------

    //---------------------------------------------------------------------------------------------
    //----------------------------------------INIT: PAUSE------------------------------------------
    //---------------------------------------------------------------------------------------------
    @Override
    public void pause()
    {
        eVideoState = EasyVideoState.PAUSE;
        try
        {
            if (mediaPlayer != null)
            {
                savedPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();

                if (videoViewListener != null)
                    videoViewListener.onPause();
            } else if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
                if (ueControllerListener != null)
                    ueControllerListener.updatePlayPauseButton();

                if (videoViewListener != null)
                    videoViewListener.onPause();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //----------------------------------------END: PAUSE-------------------------------------------

    //---------------------------------------------------------------------------------------------
    //----------------------------------------INIT: ERROR------------------------------------------
    //---------------------------------------------------------------------------------------------

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra)
    {
        eVideoState = EasyVideoState.ERROR;

        if (videoViewListener != null)
                videoViewListener.onError(what, extra);

        //si el error se produce en el loading, es bloqueante
        // y hay que notificar que se ha finalizado la reproducción
        if (what == ID_ERROR_LOADING)
            onCompletion(mediaPlayer);

        return false;
    }

    //----------------------------------------END: ERROR-----------------------------------------

    //---------------------------------------------------------------------------------------------
    //----------------------------------------INIT: FINISHED---------------------------------------
    //---------------------------------------------------------------------------------------------

    @Override
    public void onCompletion(MediaPlayer mediaPlayer)
    {
//        Log.d(TAG, "[" + TAG + "][onCompletion]init");

        //muestro el mediacontroller si se ha finalizado el vídeo y se permite mostrar los controles
        if (show_controls && mediaController != null && isGoodState())
            mediaController.show(0);
        if (videoViewListener != null)
            videoViewListener.onComplete(eVideoState.equals(EasyVideoState.ERROR));
        eVideoState = EasyVideoState.FINISHED;
    }

    //----------------------------------------END: FINISHED---------------------------------------

    //---------------------------------------------------------------------------------------------
    //---------------------------------INIT: InstaceState------------------------------------------
    //---------------------------------------------------------------------------------------------
    public void saveInstanceState(Bundle outState)
    {
//        Log.d(TAG, "[" + TAG + "][saveInstanceState]init");
        if (outState == null)
            outState = new Bundle();
        outState.putParcelable(KEY_STATE, super.onSaveInstanceState());
        //Se guarda la posición actual o la savedPosition si no se ha podido recuperar
        outState.putInt(KEY_POSITION, getCurrentPosition() == 0 ? savedPosition : getCurrentPosition());
        outState.putString(KEY_URL, mVideoUrl);
        outState.putBoolean(KEY_CAN_SEEK_FORWARD, canSeekForward);

    }

    public void restoreInstanceState(Parcelable state)
    {
//        Log.d(TAG, "[" + TAG + "][restoreInstanceState]init");
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable(KEY_STATE);
            this.savedPosition = bundle.getInt(KEY_POSITION);
            this.mVideoUrl = bundle.getString(KEY_URL);
            this.canSeekForward = bundle.getBoolean(KEY_CAN_SEEK_FORWARD);
        }
        super.onRestoreInstanceState(state);
    }

    public void setUEControllerListener(EasyControllerListener ueController) {

        ueControllerListener = ueController;

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ueControllerListener.showControl();
            }
        });

    }

    @Override
    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    //---------------------------------END: InstaceState-----------------------------------------

    public interface VideoViewListener
    {
        void onError(int codeError, int extra);

        void onPause();

        void onStart(int duration);

        void onLoading();

        void onVideoClick();

        void onComplete(boolean errorFinish);

        void onSurfaceCreated();

    }

    /**
     * Configura si está habilitada la vista
     *
     * @param enabled si está habilitado o no
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        mediaController.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    /**
     * Seekto a la posición del vídeo
     *
     * @param position posición final a la que se desea ir
     */
    @Override
    public void seekTo(int position)
    {
        //No se puede navegar por el vídeo si canSeekForward==false o canSeekBackward==false
        if (canSeekForward() && mediaPlayer != null && isGoodState() && canSeekBackward())
            try
            {
                if (mediaPlayer.getDuration() > 0)
                    mediaPlayer.seekTo(position);
                if (position == 0) savedPosition = 0; //reseteo la posición salvada
            }
            //se trata el error de IllegalStateExcption que puede dar al hacer seek y haber finalizado el vídeo
            catch (IllegalStateException ise)
            {
                mediaController.setVisibility(GONE);
                destroy();
            }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder)
    {
        destroy();
    }

    /**
     * elimina la vista y sus componentes
     */
    public void destroy()
    {
        if (mediaController != null)
        {
            mediaController.hide();
            mediaController.setVisibility(GONE);
            mediaController.setAnchorView(null);
            mediaController.destroyDrawingCache();
        }
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        releasePlayer(); // exoPlayer

        mediaPlayer = null;
        mediaController = null;
        surfaceDestroyed = true;
    }

    /**
     * Se sobreescribe el onMeasure para mantener un ratio del vídeo.
     * Por defecto se toma 16/9 pero puede ser configurado mediante el setVideoRatio
     *
     * @param widthMeasureSpec  width
     * @param heightMeasureSpec height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        float width = MeasureSpec.getSize(widthMeasureSpec);
        float height = MeasureSpec.getSize(heightMeasureSpec);

        float initialRatio = width / height;
        float targetRatio = videoRatio;

        if (targetRatio > initialRatio)
        {
            // ceil not round - avoid thin vertical gaps along the left/right edges
            height = (int) Math.ceil(width * (1 / videoRatio));

        } else
        {
            width = (int) Math.ceil(height * (videoRatio));
        }
        setMeasuredDimension((int) width, (int) height);
    }

    /**
     * Control del evento de tocar el vídeo
     *
     * @param event evento que se recibe
     * @return si se ha manejado completamente el evento
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
//        Log.d(TAG, "[" + TAG + "][onTouchEvent]MotionEvent: " + event.toString());
        if (getContext() != null && event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if (videoViewListener != null)
                videoViewListener.onVideoClick();

            if (show_controls)
            {
                try
                {
                    if (mediaController != null)
                    {
                        if (mediaController.isShowing())
                        {
                            mediaController.hide();
                        }
                        //Solo muestro los controles en un estado bueno del video
                        else if (isGoodState()) // && mediaPlayer!= null) //isGoodState ya comprueba el null
                        {
                            if (mediaPlayer.isPlaying())
                            {
                                mediaController.show();
                            } else
                            {
                                mediaController.show(0);
                            }
                        }
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    //----------------------
    //GETTER AND SETTER
    //----------------------
    public VideoViewListener getVideoViewListener()
    {
        return videoViewListener;
    }

    public void setVideoViewListener(VideoViewListener videoViewListener)
    {
        this.videoViewListener = videoViewListener;
    }

    public void setVideoUrl(String videoUrl)
    {
        this.mVideoUrl = videoUrl;
    }

    public String getVideoUrl()
    {
        return this.mVideoUrl;
    }

    public MediaController getMediaController()
    {
        return mediaController;
    }

    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }

    public void setAutoplay(boolean autoplay)
    {
        this.autoplay = autoplay;
    }

    public boolean isAutoplay()
    {
        return autoplay;
    }

    public void setMediaController(MediaController mediaController)
    {
        this.mediaController = mediaController;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer)
    {
        this.mediaPlayer = mediaPlayer;
    }

    public boolean isShow_controls()
    {
        return show_controls;
    }

    public void setShow_controls(boolean show_controls)
    {
        this.show_controls = show_controls;
    }

    public void setVideoRatio(float videoRatio)
    {
        this.videoRatio = videoRatio;
    }

    public void setFormat_4_3()
    {
        this.videoRatio = FORMAT_4_3;
    }

    public void setFormat_16_9()
    {
        this.videoRatio = FORMAT_16_9;
    }

    public EasyVideoState getState()
    {
        return eVideoState;
    }

    public boolean isForceMediaPlayer() {
        return forceMediaPlayer;
    }

    public void setForceMediaPlayer(boolean forceMediaPlayer) {
        this.forceMediaPlayer = forceMediaPlayer;
    }

    @Override
    public int getDuration()
    {
        try
        {
            return (mediaPlayer != null) ? mediaPlayer.getDuration() : 0;
        } catch (Exception e)
        {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition()
    {
        if (mediaPlayer != null)
            return mediaPlayer.getCurrentPosition();
        if (exoPlayer != null)
            return (int) exoPlayer.getCurrentPosition();

        return 0;
    }

    @Override
    public boolean isPlaying()
    {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.isPlaying();
            } catch (Exception e) {
//                e.printStackTrace();
                return false;
            }
        } else if (exoPlayer != null) {
            return exoPlayer.getPlayWhenReady();
        } else
        {
            return false;
        }
    }

    @Override
    public int getBufferPercentage()
    {
        return 0;
    }

    @Override
    public boolean canPause()
    {
        return true;
    }

    @Override
    public boolean canSeekBackward()
    {
        return true;
    }

    public void setCanSeekForward(boolean canSeekForward)
    {
        this.canSeekForward = canSeekForward;
    }

    @Override
    public boolean canSeekForward()
    {
        return canSeekForward;
    }

    @Override
    public int getAudioSessionId()
    {
        return 0;
    }

    public boolean isGoodState()
    {
        return mediaPlayer != null && eVideoState.isGoodState();
    }

    public void setCallback(SurfaceHolder.Callback callback)
    {
        holder.addCallback(callback);
    }

    public boolean canUseExoPlayer() {
        return !forceMediaPlayer && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    // exoplayer methods
    private void initializePlayer() {

        boolean needNewPlayer = exoPlayer == null;
        if (needNewPlayer) {

            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(mContext);
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            lastSeenTrackGroupArray = null;

            if (exoPlayer != null) {
                exoPlayer.removeListener(this);
                //clearVideoSurfaceInternal(holder.getSurface(), true);
            }

            Renderer[] renderers = renderersFactory.createRenderers(mainHandler, this, this, this, this);

            exoPlayer = ExoPlayerFactory.newInstance(renderers, trackSelector);

            if (exoPlayer != null) {
                setVideoSurfaceInternal(renderers);
                exoPlayer.addListener(this);
            }

            exoPlayer.setPlayWhenReady(autoplay);

        }

        if (needNewPlayer || needRetrySource) {
            Uri[] uris;
            if (mVideoUrl != null) {
                String[] uriStrings = new String[]{mVideoUrl};
                uris = new Uri[uriStrings.length];
                for (int i = 0; i < uriStrings.length; i++) {
                    uris[i] = Uri.parse(uriStrings[i]);
                }

            } else {
                showToast(mContext.getString(R.string.error_uri));
                return;
            }
            if (Util.maybeRequestReadExternalStoragePermission((Activity) mContext, uris)) {
                // The player will be reinitialized if the permission is granted.
                return;
            }
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], null);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                exoPlayer.seekTo(resumeWindow, resumePosition);
            }
            exoPlayer.prepare(mediaSource, !haveResumePosition, false);
            //updateButtonVisibilities();
        }
    }

    private void setVideoSurfaceInternal(Renderer[] renderers) {
        // Note: We don't turn this method into a no-op if the surface is being replaced with itself
        // so as to ensure onRenderedFirstFrame callbacks are still called in this case.
        ExoPlayer.ExoPlayerMessage[] messages = new ExoPlayer.ExoPlayerMessage[1];
        int count = 0;
        for (Renderer renderer : renderers) {
            if (renderer.getTrackType() == C.TRACK_TYPE_VIDEO) {
                messages[count++] = new ExoPlayer.ExoPlayerMessage(renderer, C.MSG_SET_SURFACE, getHolder().getSurface());
            }
        }
        if (this.surface != null) {
            // We're replacing a surface. Block to ensure that it's not accessed after the method returns.
            exoPlayer.blockingSendMessages(messages);
        }

        this.surface = getHolder().getSurface();

        if (this.surface != null) {
            exoPlayer.sendMessages(messages);
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    public void releasePlayer() {
        if (canUseExoPlayer() && exoPlayer != null) {
            autoplay = exoPlayer.getPlayWhenReady();
            updateResumePosition();
            exoPlayer.release();
            exoPlayer = null;
            trackSelector = null;
        }
    }

    public void pausePlayer() {
        if (canUseExoPlayer() && exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    public void resumePlayer() {
        if (canUseExoPlayer() && exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    private void updateResumePosition() {
        resumeWindow = exoPlayer.getCurrentWindowIndex();
        resumePosition = exoPlayer.isCurrentWindowSeekable() ? Math.max(0, exoPlayer.getCurrentPosition())
                : C.TIME_UNSET;

    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *     DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *     DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    // ExoPlayer.EventListener implementation

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_READY) {
            if (videoViewListener != null)
                videoViewListener.onStart((int) exoPlayer.getDuration());
            eVideoState = EasyVideoState.PLAYING;
            //showControls();
        } else if (playbackState == ExoPlayer.STATE_ENDED) {
            if (videoViewListener != null)
                videoViewListener.onComplete(false);
            eVideoState = EasyVideoState.FINISHED;
            //showControls();
        }
    }

    @Override
    public void onPositionDiscontinuity() {
        if (needRetrySource) {
            // This will only occur if the user has performed a seek whilst in the error state. Update the
            // resume position so that if the user then retries, playback will resume from the position to
            // which they seeked.
            updateResumePosition();
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        // Do nothing.
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {

        if (!needRetrySource)
            needRetrySource = true;
        else
            needRetrySource = false;

        if (!needRetrySource) {
            eVideoState = EasyVideoState.ERROR;

            if (videoViewListener != null)
                videoViewListener.onError(30, 30);


            if (videoViewListener != null)
                videoViewListener.onComplete(true);

        } else {
            initializePlayer();
        }

    }

    @Override
    @SuppressWarnings("ReferenceEquality")
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        //updateButtonVisibilities();
        if (trackGroups != lastSeenTrackGroupArray) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                        == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    showToast(R.string.error_unsupported_video);
                }
                if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                        == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    showToast(R.string.error_unsupported_audio);
                }
            }
            lastSeenTrackGroupArray = trackGroups;
        }
    }

    private void showToast(int messageId) {
        showToast(mContext.getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(mContext, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(mContext, "ExoPlayerDemo"), bandwidthMeter);
    }

    // ExoPlayer methods implementations
    @Override
    public void onAudioEnabled(DecoderCounters counters) {

    }

    @Override
    public void onAudioSessionId(int audioSessionId) {

    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onAudioInputFormatChanged(Format format) {

    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {

    }

    @Override
    public void onMetadata(Metadata metadata) {

    }

    @Override
    public void onCues(List<Cue> cues) {

    }

    @Override
    public void onVideoEnabled(DecoderCounters counters) {
        Log.d(TAG, "onVideoEnabled() called with: counters = [" + counters + "]");
    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        Log.d(TAG, "onVideoDecoderInitialized() called with: decoderName = [" + decoderName + "], initializedTimestampMs = [" + initializedTimestampMs + "], initializationDurationMs = [" + initializationDurationMs + "]");
    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        Log.d(TAG, "onVideoSizeChanged() called with: width = [" + width + "], height = [" + height + "], unappliedRotationDegrees = [" + unappliedRotationDegrees + "], pixelWidthHeightRatio = [" + pixelWidthHeightRatio + "]");
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {
        Log.d(TAG, "onVideoDisabled() called with: counters = [" + counters + "]");
    }

}
