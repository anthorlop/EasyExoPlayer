package es.lombrinus.projects.mods.videos;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import es.lombrinus.projects.mods.videos.controller.EasyControllerView;
import es.lombrinus.projects.mods.videos.views.EasyPreRollVideoView;
import es.lombrinus.projects.mods.videos.views.EasyVideoView;

/**
 * Fragment para la reprodución de vídeos, puede venir algúna url de los vídeos a nulo
 * y no se reproducirá
 *
 * @author anthorlop
 */
public class EasyVideoFragment extends Fragment
{
    //---------------------
    //PUBLIC ARGUMENTS
    //---------------------
    public final static String ARG_ADS_URL = "arg_ads_url";
    public final static String ARG_VIDEO_URL = "arg_video_url";
    public final static String ARG_BACKGROUND_COLOR = "arg_background_color";
    public final static String ARG_AUTOPLAY_VIDEO = "arg_autoplay_video";
    public final static String ARG_FORCE_MEDIAPLAYER = "arg_force_mediaplayer";

    //---------------------
    //KEY FOR SAVEDINSTANCE
    //---------------------
    protected String KEY_IS_PLAYING_ANY_VIDEO = "key_is_playing_any_video";
    protected String KEY_ADS_COMPLETED = "key_ads_completed";
    protected String KEY_SAVED_INSTANCE_STATE = "key_saved_instance_state";

    private static final int SURFACE_TYPE_NONE = 0;
    private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
    private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;

    //---------------------
    //RESULT
    //---------------------
    public final static int URL_VIDEO_NULL = 0;

    //---------------------
    //VIDEOVIEWS
    //---------------------
    protected EasyVideoView eVideoView = null;
    protected EasyPreRollVideoView ePreRollVideoView = null;

    protected RelativeLayout videoHolder = null;
    protected ProgressBar progressBar = null;
    protected String backgroundColor;
    protected boolean adsCompleted = false;
    protected Bundle savedInstanceState = null; //bundle necesario para pasarselo a onResume

    protected EasyVideoFragmentListener mListener; //Listener para el Fragment
    private boolean isPlaying = false;

    protected boolean autoplayVideo = true;
    protected boolean forceMediaPlayer = false;


    //---------------------
    //URL
    //---------------------
    private String videoURL;
    private String adsURL;

    //---------------------
    //NewInstance
    //---------------------

    /**
     * Crea el fragment
     *
     * @param urlVideo url del vídeo a reproducir
     * @param urlAds   url del prerroll que se va a reproducir
     * @return el fragment
     */
    public static EasyVideoFragment newInstance(@Nullable String urlVideo, @Nullable String urlAds)
    {
        return newInstance(urlVideo, urlAds, true, false, null);
    }

    /**
     * Crea el fragment
     *
     * @param urlVideo url del vídeo a reproducir
     * @param urlAds   url del prerroll que se va a reproducir
     * @return el fragment
     */
    public static EasyVideoFragment newInstance(@Nullable String urlVideo, @Nullable String urlAds, @Nullable String backgroundColor)
    {
        return newInstance(urlVideo, urlAds, true, false, backgroundColor);
    }

    /**
     * Crea el fragment y se puede poner un color de fondo para el fragment
     *
     * @param urlVideo        url del vídeo a reproducir
     * @param urlAds          url del prerroll que se va a reproducir
     * @param autoplayVideo   si se desea que se autoinicie el video
     * @param backgroundColor color de fondo para el fragment
     * @return el fragment
     */
    public static EasyVideoFragment newInstance(@Nullable String urlVideo, @Nullable String urlAds, boolean autoplayVideo, boolean forceMediaPlayer, @Nullable String backgroundColor)
    {
        EasyVideoFragment fragment = new EasyVideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ADS_URL, urlAds);
        args.putString(ARG_VIDEO_URL, urlVideo);
        args.putString(ARG_BACKGROUND_COLOR, backgroundColor);
        args.putBoolean(ARG_AUTOPLAY_VIDEO, autoplayVideo);
        args.putBoolean(ARG_FORCE_MEDIAPLAYER, forceMediaPlayer);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Contructor por defecto
     */
    public EasyVideoFragment()
    {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Argumentos
        Bundle args = getArguments();
        if (args != null)
        {
            videoURL = args.getString(ARG_VIDEO_URL);
            adsURL = args.getString(ARG_ADS_URL);
            backgroundColor = args.getString(ARG_BACKGROUND_COLOR);
            autoplayVideo = args.getBoolean(ARG_AUTOPLAY_VIDEO, true);
            forceMediaPlayer = args.getBoolean(ARG_FORCE_MEDIAPLAYER, false);
        }
        if (savedInstanceState != null)
        {
            adsCompleted = savedInstanceState.getBoolean(KEY_ADS_COMPLETED);
            this.savedInstanceState = savedInstanceState.getParcelable(KEY_SAVED_INSTANCE_STATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_video, container, false);

        //Configuración del color de background
        if (!TextUtils.isEmpty(backgroundColor))
        {
            try
            {
                v.setBackgroundColor(Color.parseColor(backgroundColor));
            } catch (Exception e)
            {
                e.printStackTrace();
//                v.setBackgroundColor(Color.parseColor(DEFAUL_BACKGROUND_COLOR));
            }
        }
        videoHolder = (RelativeLayout) v.findViewById(R.id.videoHolder);
        //inicializamos el progresbar y el bloqueo de pantalla
        initProgresBar();

        //El funcionamiento y la carga de vídeos se realiza mediante el onResume y onPause
        return v;
    }

    @Override
    public void onPause()
    {
        Bundle outState = new Bundle();
        if (ePreRollVideoView != null && !ePreRollVideoView.getUeVideoView().canUseExoPlayer())
        {
            isPlaying = ePreRollVideoView.getUeVideoView().isPlaying();

            ePreRollVideoView.getUeVideoView().saveInstanceState(outState);
            ePreRollVideoView.destroy();
        }

        if (eVideoView != null && !eVideoView.canUseExoPlayer())
        {
            isPlaying = isPlaying | eVideoView.isPlaying();
            eVideoView.saveInstanceState(outState);
            eVideoView.destroy();
        }
        this.savedInstanceState = outState;

        // exoplayer actions
        if (eVideoView != null)
            eVideoView.pausePlayer();
        if (ePreRollVideoView != null && ePreRollVideoView.getUeVideoView() != null)
            ePreRollVideoView.getUeVideoView().pausePlayer();

        super.onPause();
    }

    @Override
    public void onResume()
    {
        View v = getView();
        if (v != null)
        {   //si no se consigue lanzar el vídeo de Ads o ya se ha reproducido la Ads, se lanza el video normal
            if (!lauchVideoAds(savedInstanceState) || adsCompleted)
                launchVideo(savedInstanceState);

            savedInstanceState = null;
        }

        if (eVideoView != null)
            eVideoView.resumePlayer();
        if (ePreRollVideoView != null && ePreRollVideoView.getUeVideoView() != null)
            ePreRollVideoView.getUeVideoView().resumePlayer();




        super.onResume();
    }

    /**
     * Inicializa y lanza la reprodución del prerroll
     *
     * @param savedInstanceState estado anterior
     * @return si se ha conseguido lanzar el vídeo (devuelve falso si la url está vacía)
     */
    protected boolean lauchVideoAds(@Nullable Bundle savedInstanceState)
    {
        if (!adsCompleted && !TextUtils.isEmpty(adsURL) && getActivity() != null)
        {
            //Se vacía el viewHolder para que solo contenga el vídeo de Ads
            resetViewHolder();

            //inicializamos el view
            ePreRollVideoView = new EasyPreRollVideoView(getActivity());

            //Agregamos las vistas
            if (ePreRollVideoView.getUeVideoView().canUseExoPlayer()) {
                // Añadir AspectRatioFrameLayout
                AspectRatioFrameLayout aspectRatioFrameLayout = new AspectRatioFrameLayout(getContext());
                AspectRatioFrameLayout.LayoutParams paramsAspectRatio = new AspectRatioFrameLayout.LayoutParams(AspectRatioFrameLayout.LayoutParams.MATCH_PARENT, AspectRatioFrameLayout.LayoutParams.MATCH_PARENT);
                ePreRollVideoView.setLayoutParams(paramsAspectRatio);
                aspectRatioFrameLayout.addView(ePreRollVideoView);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                aspectRatioFrameLayout.setLayoutParams(params);
                videoHolder.addView(aspectRatioFrameLayout);

            } else {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                ePreRollVideoView.setLayoutParams(params);

                videoHolder.addView(ePreRollVideoView);
            }

            if (progressBar != null)
            {
                videoHolder.addView(progressBar);
                progressBar.setVisibility(View.VISIBLE);
            }

            EasyVideoView ueVideoViewAds = ePreRollVideoView.getUeVideoView();

            //le asignamos el listener
            ueVideoViewAds.setVideoViewListener(new EasyVideoView.VideoViewListener()
            {
                @Override
                public void onError(int codeError, int extra)
                {
                    adsCompleted = true;
                    if (!ePreRollVideoView.getUeVideoView().isPlaying())
                        launchVideo(null);
                    if (mListener != null)
                        mListener.onAdsError(codeError, extra);
                }

                @Override
                public void onPause()
                {
                    if (mListener != null)
                        mListener.onAdsPause();
                }

                @Override
                public void onStart(int duration)
                {
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                    if (mListener != null)
                        mListener.onAdsStart(duration);

                }

                @Override
                public void onLoading()
                {

                }

                @Override
                public void onVideoClick()
                {
                    if (mListener != null)
                        mListener.onAdsVideoClick();
                }

                @Override
                public void onComplete(boolean isErrorFinish)
                {
                    adsCompleted = true;
                    if (mListener != null)
                        mListener.onAdsCompleted(isErrorFinish);
                    launchVideo(null);

                }

                @Override
                public void onSurfaceCreated()
                {
                    ePreRollVideoView.getUeVideoView().createPreparePlayer();
                }
            });

            ueVideoViewAds.setAutoplay(true);
            if (savedInstanceState != null)
            {
                ueVideoViewAds.restoreInstanceState(savedInstanceState);
//                if (!savedInstanceState.getBoolean(KEY_IS_PLAYING_ANY_VIDEO))
//                    ueVideoViewAds.setAutoplay(false);
            }
            ueVideoViewAds.setVideoUrl(adsURL);

            return true;
        }
        //Si no se ha introducido url para las ads
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (eVideoView != null)
            eVideoView.releasePlayer();
        if (ePreRollVideoView != null && ePreRollVideoView.getUeVideoView() != null)
            ePreRollVideoView.getUeVideoView().releasePlayer();
    }

    /**
     * Inicializa y lanza la reprodución del vídeo
     *
     * @param savedInstanceState estado anterior
     * @return si se ha conseguido lanzar el vídeo (devuelve falso si la url está vacía)
     */
    protected boolean launchVideo(@Nullable Bundle savedInstanceState)
    {

        Activity activity = getActivity();

        if (activity == null)
            return false;

        if (TextUtils.isEmpty(videoURL)) {
            if (mListener != null)
            {
                mListener.onVideoError(URL_VIDEO_NULL, URL_VIDEO_NULL);
                mListener.onVideoCompleted(true);

            }
            activity.finish();

            return false;
        }

        if (eVideoView != null && TextUtils.equals(videoURL, eVideoView.getVideoUrl()))
            return true;

        //Vaciamos el holder de otras vistas
        resetViewHolder();

        eVideoView = new EasyVideoView(activity);
        eVideoView.setForceMediaPlayer(forceMediaPlayer);

        //Agregamos las vistas
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        eVideoView.setLayoutParams(params);

        videoHolder.addView(eVideoView);

        if (eVideoView.canUseExoPlayer()) {

            final EasyControllerView controlView = new EasyControllerView(getContext(), eVideoView);
            //controlView = new UEPlaybackControlView(getContext());

            RelativeLayout.LayoutParams paramsController = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsController.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, eVideoView.getId());
            videoHolder.addView(controlView, paramsController);

            eVideoView.setUEControllerListener(controlView);
        }

        if (progressBar != null)
        {
            videoHolder.addView(progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }
        eVideoView.setAutoplay(autoplayVideo);

        if (savedInstanceState != null)
        {
            eVideoView.restoreInstanceState(savedInstanceState);
            //Si el vídeo estaba detenido
            if (!savedInstanceState.getBoolean(KEY_IS_PLAYING_ANY_VIDEO))
                eVideoView.setAutoplay(false);
        }
        eVideoView.setVideoUrl(videoURL);
        eVideoView.setVideoViewListener(new EasyVideoView.VideoViewListener()
        {

            @Override
            public void onError(int codeError, int extra)
            {
                if (mListener != null)
                    mListener.onVideoError(codeError, extra);
            }

            @Override
            public void onPause()
            {
                if (mListener != null)
                    mListener.onVideoPause();
            }

            @Override
            public void onStart(int duration)
            {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                if (mListener != null)
                    mListener.onVideoPlay(duration);
            }

            @Override
            public void onLoading()
            {
                if (progressBar != null)
                    progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVideoClick()
            {
                if (mListener != null)
                    mListener.onVideoClick();
            }

            @Override
            public void onComplete(boolean isErrorFinish)
            {
                if (isErrorFinish)
                    showVideoError();

                if (mListener != null)
                    mListener.onVideoCompleted(isErrorFinish);
            }

            @Override
            public void onSurfaceCreated()
            {
                eVideoView.createPreparePlayer();
            }
        });

        return true;
    }

    /**
     * Se muestra el error al reproducir el video (no el correspondiente al prerroll)
     */
    private void showVideoError()
    {
        videoHolder.post(new Runnable()
        {
            @Override
            public void run()
            {
                resetViewHolder();
                View errorView = getVideoViewError();
                if (errorView != null)
                    videoHolder.addView(errorView);
            }
        });
    }

    /**
     * Función para obtener la vista, se puede sobreescribir si se hereda el fragment o se puede
     * hacer uso del Listener para definir la vista de error
     */
    public View getVideoViewError()
    {
        //Caso que se defina por el listener
        if (mListener != null)
        {
            View errorView = mListener.getVideoViewError();
            if (errorView != null)
                return errorView;
        }
        //Caso que se usa la vista por defecto
        View view = null;
        Activity activity = getActivity();
        if (activity != null)
        {
            view = View.inflate(activity, R.layout.video_error, null);
            if (view != null)
            {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                view.setLayoutParams(params);
            }
        }
        return view;
    }

    /**
     * Reinicia el viewHolder vaciandolo de las vistas y reseteando los dos VideoViews
     */
    public final void resetViewHolder()
    {
        videoHolder.removeAllViews();
        if (ePreRollVideoView != null)
        {
            ePreRollVideoView.destroy();
            ePreRollVideoView = null;
        }

        if (eVideoView != null)
        {
            eVideoView.destroy();
            eVideoView = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putParcelable(KEY_SAVED_INSTANCE_STATE, savedInstanceState);

        //Guardo si se estaba reproduciendo el vídeo
        outState.putBoolean(KEY_IS_PLAYING_ANY_VIDEO, isPlaying);
        outState.putBoolean(KEY_ADS_COMPLETED, adsCompleted);

        super.onSaveInstanceState(outState);
    }

    public boolean isPlaying()
    {
        return isPlaying;
    }

    /**
     * Inicializa el progresbar por defecto, si se desea usar otro, se debeía utilizar el
     */
    protected void initProgresBar()
    {
        Activity activity = getActivity();
        if (activity != null)
        {
            progressBar = new ProgressBar(activity);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            progressBar.setLayoutParams(params);
        }
    }

    //---------------
    //GETTER & SETTER
    //---------------
    public void setUEVideoFragmentListener(EasyVideoFragmentListener ueVideoFragmentListener)
    {
        this.mListener = ueVideoFragmentListener;
    }

    public EasyVideoView getUEVideoView()
    {
        return eVideoView;
    }

    public EasyPreRollVideoView getUEPreRollVideoView()
    {
        return ePreRollVideoView;
    }

    public boolean isAdsCompleted()
    {
        return adsCompleted;
    }

    public EasyVideoFragmentListener getUEVideoFragmentListener()
    {
        return mListener;
    }

    public ProgressBar getProgressBar()
    {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar)
    {
        this.progressBar = progressBar;
    }

    public void setAdsURL(String adsURL)
    {
        this.adsURL = adsURL;
    }

    public String getAdsURL()
    {
        return adsURL;
    }


    public void setVideoURL(String videoURL)
    {
        this.videoURL = videoURL;
    }

    public String getVideoURL()
    {
        return videoURL;
    }


//---------------
//INTERFACE
//---------------

    public interface EasyVideoFragmentListener
    {
        void onAdsStart(int duration);

        void onAdsPause();

        void onAdsCompleted(boolean isErrorFinish);

        void onAdsVideoClick();

        void onAdsError(int codeError, int extra);

        void onVideoPlay(int duration);

        void onVideoPause();

        void onVideoCompleted(boolean isErrorFinish);

        void onVideoClick();

        void onVideoError(int codeError, int extra);

        View getVideoViewError();
    }

}
