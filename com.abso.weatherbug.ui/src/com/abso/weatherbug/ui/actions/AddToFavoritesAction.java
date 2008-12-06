package com.abso.weatherbug.ui.actions;

import org.eclipse.jface.action.Action;

import com.abso.weatherbug.ui.WeatherBugImages;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.views.ForecastView;
import com.abso.weatherbug.ui.views.LiveWeatherView;

/**
 * An action to add the current location to the list of favorites.
 */
public class AddToFavoritesAction extends Action {

    /** Constructs a new action. */
    public AddToFavoritesAction() {
        super("", WeatherBugImages.getDescriptor(WeatherBugImages.ADD_TO_FAVORITES));
        setToolTipText("Add to Favorites");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        if (WeatherBugUIPlugin.getDefault().addCurrentLocationToFavorites()) {
            ForecastView forecastView = ForecastView.getActiveForecastView();
            if (forecastView != null) {
                forecastView.updateFavorites();
            }
            LiveWeatherView liveWeatherView = LiveWeatherView.getActiveLiveWeatherView();
            if (liveWeatherView != null) {
                liveWeatherView.updateFavorites();
            }
        }
    }

}
