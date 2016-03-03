package alsterradio2.com.example.mtracz.alsterradio_corrected;

/**
 * Created by MTRACZ on 02.Mrz.2016.
 */
public class PlayerProperties {
    private static PlayerProperties instance;

    private boolean isPlaying;
    private int numberOfClicks = 0;

    public static PlayerProperties getInstance()
    {
        if (instance == null) {
            instance = new PlayerProperties();
            return instance;
        }
        else
            return instance;
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

    public void setNumberOfClicks(int numberOfClicks)
    {
        this.numberOfClicks = numberOfClicks;
    }
}
