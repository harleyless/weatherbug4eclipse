package com.abso.weatherbug.ui.jobs;

import java.util.TimerTask;

import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.views.ForecastView;

/**
 * A timer task able to repeatedly refresh the forecast view.
 */
public class RefreshForecastTask extends TimerTask {

    /** The forecast view this task is attached to. */
    private ForecastView view;

    /**
     * Constructs a new task.
     * 
     * @param view
     *            the forecast view this task is attached to.
     */
    public RefreshForecastTask(ForecastView view) {
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
            new RefreshForecastJob(location, view, false).schedule();
        }
    }

}
