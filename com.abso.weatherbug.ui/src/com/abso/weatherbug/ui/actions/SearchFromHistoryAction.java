package com.abso.weatherbug.ui.actions;

import org.eclipse.jface.action.Action;

import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.ui.jobs.RefreshForecastJob;
import com.abso.weatherbug.ui.jobs.RefreshLiveWeatherJob;
import com.abso.weatherbug.ui.viewers.LocationLabelProvider;
import com.abso.weatherbug.ui.views.ForecastView;
import com.abso.weatherbug.ui.views.LiveWeatherView;

/**
 * An action able to refresh the live weather and forecast view data using a location stored in the local history.
 */
public class SearchFromHistoryAction extends Action {

    /** The location stored in the history. */
    private Location location;

    /**
     * Constructs a new action.
     * 
     * @param location
     *            the location stored in the history.
     */
    public SearchFromHistoryAction(Location location) {
        super(new LocationLabelProvider().getText(location), AS_RADIO_BUTTON);
        this.location = location;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        if (!isChecked()) {
            return;
        }
        ForecastView forecastView = ForecastView.getActiveForecastView();
        if (forecastView != null) {
            new RefreshForecastJob(location, forecastView, true).schedule();
        }
        LiveWeatherView liveWeatherView = LiveWeatherView.getActiveLiveWeatherView();
        if (liveWeatherView != null) {
            new RefreshLiveWeatherJob(location, liveWeatherView, true).schedule();
        }
    }

}
