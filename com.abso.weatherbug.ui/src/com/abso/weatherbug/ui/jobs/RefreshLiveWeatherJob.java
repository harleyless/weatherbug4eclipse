package com.abso.weatherbug.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.abso.weatherbug.core.WeatherBugServiceException;
import com.abso.weatherbug.core.data.LiveWeather;
import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.core.data.Station;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.views.LiveWeatherView;

/**
 * The asynchronous job able to refresh the live weather.
 */
public class RefreshLiveWeatherJob extends Job {

    /** The location whose live weather must be refreshed. */
    private Location location;

    /** The weather station. */
    private Station station;

    /** The live weather view. */
    private LiveWeatherView view;

    /** Indicates if in case of error a dialog must be displayed. */
    private boolean showErrorDialog;

    /**
     * Constructs a new job, able to recompute the live weather for the current location.
     * 
     * @param view
     *            the live weather view.
     * @param showErrorDialog
     *            indicates whether in case of error a dialog must be displayed.
     */
    public RefreshLiveWeatherJob(LiveWeatherView view, boolean showErrorDialog) {
        this((Location) null, view, showErrorDialog);
    }

    /**
     * Constructs a new job, able to recompute the live weather for a specific location.
     * 
     * @param location
     *            the location whose live weather must be recomputed.
     * @param view
     *            the live weather view.
     * @param showErrorDialog
     *            indicates whether in case of error a dialog must be displayed.
     */
    public RefreshLiveWeatherJob(Location location, LiveWeatherView view, boolean showErrorDialog) {
        super("Refreshing live weather");
        this.location = location;
        this.view = view;
        this.showErrorDialog = showErrorDialog;
    }

    /**
     * Constructs a new job, able to recompute the live weather for a specific weather station.
     * 
     * @param station
     *            a weather station.
     * @param view
     *            the live weather view.
     * @param showErrorDialog
     *            indicates whether in case of error a dialog must be displayed.
     */
    public RefreshLiveWeatherJob(Station station, LiveWeatherView view, boolean showErrorDialog) {
        super("Refreshing live weather");
        this.station = station;
        this.view = view;
        this.showErrorDialog = showErrorDialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected IStatus run(IProgressMonitor monitor) {
        try {
            if (location != null) {
                WeatherBugUIPlugin.getDefault().saveCurrentLocation(location);
                view.updateHistory();
            } else if (station != null) {
                WeatherBugUIPlugin.getDefault().saveCurrentStation(station);
            }
            final LiveWeather liveWeather = WeatherBugUIPlugin.getDefault().getLiveWeather();
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            Control control = view.getViewSite().getShell();
            if ((control != null) && !control.isDisposed()) {
                Display display = control.getDisplay();
                if (display != null) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            view.refreshData(liveWeather);
                        }
                    });
                }
            }
        } catch (WeatherBugServiceException e) {
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            if (showErrorDialog) {
                WeatherBugUIPlugin.getDefault().showErrorDialog("Error", "Unable to refresh live weather",
                        "Error refreshing live weather", e);
            }
        }
        return Status.OK_STATUS;
    }

}
