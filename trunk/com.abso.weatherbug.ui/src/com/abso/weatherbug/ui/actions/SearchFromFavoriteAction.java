package com.abso.weatherbug.ui.actions;

import org.eclipse.jface.action.Action;

import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.ui.jobs.RefreshForecastJob;
import com.abso.weatherbug.ui.jobs.RefreshLiveWeatherJob;
import com.abso.weatherbug.ui.viewers.LocationLabelProvider;
import com.abso.weatherbug.ui.views.ForecastView;
import com.abso.weatherbug.ui.views.LiveWeatherView;

/**
 * An action able to refresh the live weather and forecast view data using a location stored in the list of favorite locations.
 */
public class SearchFromFavoriteAction extends Action {

    /** A favorite location. */
    private Location favLocation;

    /**
     * Constructs a new action.
     * 
     * @param favLocation
     *            a favorite location.
     */
    public SearchFromFavoriteAction(Location favLocation) {
        super(new LocationLabelProvider().getText(favLocation), AS_RADIO_BUTTON);
        this.favLocation = favLocation;
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
            new RefreshForecastJob(favLocation, forecastView, true).schedule();
        }
        LiveWeatherView liveWeatherView = LiveWeatherView.getActiveLiveWeatherView();
        if (liveWeatherView != null) {
            new RefreshLiveWeatherJob(favLocation, liveWeatherView, true).schedule();
        }
    }

}
