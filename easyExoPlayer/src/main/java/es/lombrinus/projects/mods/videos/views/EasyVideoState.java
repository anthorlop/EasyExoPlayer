package es.lombrinus.projects.mods.videos.views;

/**
 * Estados posibles para los VideoViews, se corresponde con los del listener que se le puede asignar
 * a los VideoViews
 *
 * @author anthorlop
 */
public enum EasyVideoState
{
    NO_LOADED(-2), ERROR(-1), LOADING(0), PREPARED(1), PAUSE(2), PLAYING(3), FINISHED(4);
    private int value;

    EasyVideoState(int value)
    {
        this.value = value;
    }

    public boolean isGoodState()
    {
        return value > 0;
    }

}
