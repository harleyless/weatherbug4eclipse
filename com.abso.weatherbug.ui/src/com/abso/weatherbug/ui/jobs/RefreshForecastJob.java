package com.abso.weatherbug.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.abso.weatherbug.core.WeatherBugServiceException;
import com.abso.weatherbug.core.data.Forecasts;
import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.views.ForecastView;

/**
 * The asynchronous job able to refresh the forecast view.
 */
public class RefreshForecastJob extends Job {

    /** The location whose forecast must be refreshed. */
    private Location location;

    /** The forecast view. */
    private ForecastView view;

    /** Indicates if in case of error a dialog must be displayed. */
    private boolean showErrorDialog;

    /**
     * Constructs a new job, able to recompute the forecast for the current location.
     * 
     * @param view
     *            the forecast view.
     * @param showErrorDialog
     *            indicates whether in case of error a dialog must be displayed.
     */
    public RefreshForecastJob(ForecastView view, boolean showErrorDialog) {
        this(null, view, showErrorDialog);
    }

    /**
     * Constructs a new job, able to recompute the forecast for a specific location.
     * 
     * @param location
     *            the location whose forecast must be recomputed.
     * @param view
     *            the forecast view.
     * @param showErrorDialog
     *            indicates whether in case of error a dialog must be displayed.
     */
    public RefreshForecastJob(Location location, ForecastView view, boolean showErrorDialog) {
        super("Refreshing forecast");
        this.location = location;
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
            }
            final Forecasts forecasts = WeatherBugUIPlugin.getDefault().getForecasts();
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            Control control = view.getViewSite().getShell();
            if ((control != null) && !control.isDisposed()) {
                Display display = control.getDisplay();
                if (display != null) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            view.refreshData(forecasts);
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
