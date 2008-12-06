package com.abso.weatherbug.ui.jobs;

import java.util.TimerTask;

import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.views.LiveWeatherView;

/**
 * A timer task able to repeatedly refresh the live weather view.
 */
public class RefreshLiveWeatherTask extends TimerTask {

    /** The live weather view this task is attached to. */
    private LiveWeatherView view;

    /**
     * Constructs a new task.
     * 
     * @param view
     *            the live weather view this task is attached to.
     */
    public RefreshLiveWeatherTask(LiveWeatherView view) {
        this.view = view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.TimerTask#run()
     */
    public void run() {
        Location location = WeatherBugUIPlugin.getDefault().getCurrentLocation();
        if (location != null) {
            new RefreshLiveWeatherJob(location, view, false).schedule();
        }
    }

}
