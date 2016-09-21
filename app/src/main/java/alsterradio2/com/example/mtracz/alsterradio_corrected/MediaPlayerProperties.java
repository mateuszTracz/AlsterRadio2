package alsterradio2.com.example.mtracz.alsterradio_corrected;

/**
 * Created by MTRACZ on 02.Mrz.2016.
 */
public class MediaPlayerProperties {
    private static MediaPlayerProperties instance;

    private boolean isPlaying;
    private boolean shouldButtonPlayBeDisabled = false;

    public boolean isTimerStarted() {
        return isTimerStarted;
    }

    public void setTimerStarted(boolean isTimerStarted) {
        this.isTimerStarted = isTimerStarted;
    }

    private boolean isTimerStarted=false;
    private int numberOfClicks = 0;

    private MediaPlayerProperties() {

    }

    public static MediaPlayerProperties getInstance(){
        if (instance == null) {
            instance = new MediaPlayerProperties();
            return instance;
        }
        else
            return instance;
    }

    public boolean shouldButtonPlayBeDisabled() {
        return shouldButtonPlayBeDisabled;
    }

    public void shouldButtonPlayBeDisabled(boolean shouldButtonPlayBeDisabled) {
        this.shouldButtonPlayBeDisabled = shouldButtonPlayBeDisabled;
    }

    public boolean isPlaying()
    {
        return isPlaying;
    }

    public int getNumberOfClicks()
    {
        return numberOfClicks;
    }

    public void setIsPlaying(boolean isPlaying)
    {
        this.isPlaying = isPlaying;
    }

    public void setNumberOfClicks(int numberOfClicks){
        this.numberOfClicks = numberOfClicks;
    }

}
