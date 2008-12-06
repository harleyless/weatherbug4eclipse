package com.abso.weatherbug.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.views.ForecastView;
import com.abso.weatherbug.ui.views.LiveWeatherView;

/**
 * Action to clear favorite actions.
 */
public class ClearFavoritesAction extends Action {

    /** Constructs a new action. */
    public ClearFavoritesAction() {
        super("Clear Favorites");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        Shell shell = WeatherBugUIPlugin.getActiveWorkbenchShell();
        if (MessageDialog.openQuestion(shell, "Confirm", "Are you sure you want to remove the favorite locations?")) {
            WeatherBugUIPlugin.getDefault().clearFavoriteLocations();
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
