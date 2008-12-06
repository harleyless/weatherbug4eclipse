package com.abso.weatherbug.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import com.abso.weatherbug.ui.IWeatherBugConstants;
import com.abso.weatherbug.ui.WeatherBugImages;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.jobs.RefreshForecastJob;
import com.abso.weatherbug.ui.jobs.RefreshLiveWeatherJob;
import com.abso.weatherbug.ui.views.ForecastView;
import com.abso.weatherbug.ui.views.LiveWeatherView;

/**
 * An action able to refresh the current weather data using a specific unit type (U.S. or metric).
 */
public class SetUnitTypeAction extends Action {

    /**
     * The unit type.
     * 
     * @see IWeatherBugConstants#US_UNIT_TYPE
     * @see IWeatherBugConstants#METRIC_UNIT_TYPE
     */
    private int unitType;

    /**
     * Constructs a new action.
     * 
     * @param usType
     *            <code>true</code> for U.S. unit type; <code>false</code> for metric unit type.
     */
    public SetUnitTypeAction(boolean usType) {
        super(usType ? "U.S. Unit Type" : "Metric Unit Type", IAction.AS_RADIO_BUTTON);
        setImageDescriptor(WeatherBugImages.getDescriptor(usType ? WeatherBugImages.US : WeatherBugImages.EU));
        this.unitType = usType ? IWeatherBugConstants.US_UNIT_TYPE : IWeatherBugConstants.METRIC_UNIT_TYPE;
        setChecked(unitType == WeatherBugUIPlugin.getDefault().getUnitType());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        if (unitType != WeatherBugUIPlugin.getDefault().getUnitType()) {
            WeatherBugUIPlugin.getDefault().setUnitType(unitType);
            LiveWeatherView liveWeatherView = LiveWeatherView.getActiveLiveWeatherView();
            ForecastView forecastView = ForecastView.getActiveForecastView();
            if (liveWeatherView != null) {
                liveWeatherView.setUnitType(unitType);
            }
            if (forecastView != null) {
                forecastView.setUnitType(unitType);
            }
            if (liveWeatherView != null) {
                new RefreshLiveWeatherJob(liveWeatherView, true).schedule();
            }
            if (forecastView != null) {
                new RefreshForecastJob(forecastView, true).schedule();
            }
        }
    }

}
