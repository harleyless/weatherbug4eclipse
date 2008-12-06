package com.abso.weatherbug.ui.actions;

import org.eclipse.jface.action.Action;

import com.abso.weatherbug.ui.WeatherBugImages;
import com.abso.weatherbug.ui.jobs.RefreshLiveWeatherJob;
import com.abso.weatherbug.ui.views.LiveWeatherView;

/**
 * An action able to refresh the live weather view.
 */
public class RefreshLiveWeatherAction extends Action {

    /** The live weather view. */
    private LiveWeatherView view;

    /**
     * Constructs a new action.
     * 
     * @param view
     *            the live weather view.
     */
    public RefreshLiveWeatherAction(LiveWeatherView view) {
        super("", WeatherBugImages.getDescriptor(WeatherBugImages.REFRESH));
        setToolTipText("Refresh");
        this.view = view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        new RefreshLiveWeatherJob(view, true).schedule();
    }

}
