package com.abso.weatherbug.ui.actions;

import org.eclipse.jface.action.Action;

import com.abso.weatherbug.ui.WeatherBugImages;
import com.abso.weatherbug.ui.jobs.RefreshForecastJob;
import com.abso.weatherbug.ui.views.ForecastView;

/**
 * An action able to refresh the forecast view.
 */
public class RefreshForecastAction extends Action {

    /** The forecast view. */
    private ForecastView view;

    /**
     * Constructs a new action.
     * 
     * @param view
     *            the forecast view.
     */
    public RefreshForecastAction(ForecastView view) {
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
        new RefreshForecastJob(view, true).schedule();
    }

}
