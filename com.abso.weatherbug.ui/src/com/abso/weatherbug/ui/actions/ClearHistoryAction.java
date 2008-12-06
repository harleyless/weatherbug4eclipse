package com.abso.weatherbug.ui.actions;

import org.eclipse.jface.action.Action;

import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.views.ForecastView;
import com.abso.weatherbug.ui.views.LiveWeatherView;

/**
 * Action to clear the current location history.
 */
public class ClearHistoryAction extends Action {

    /** Constructs a new action. */
    public ClearHistoryAction() {
        super("Clear History");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        WeatherBugUIPlugin.getDefault().clearLocationHistory();
        ForecastView forecastView = ForecastView.getActiveForecastView();
        if (forecastView != null) {
            forecastView.updateHistory();
        }
        LiveWeatherView liveWeatherView = LiveWeatherView.getActiveLiveWeatherView();
        if (liveWeatherView != null) {
            liveWeatherView.updateHistory();
        }
    }

}
