package com.abso.weatherbug.ui.views;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import com.abso.weatherbug.core.WeatherBugServiceException;
import com.abso.weatherbug.core.data.LiveWeather;
import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.core.data.Station;
import com.abso.weatherbug.core.data.WeatherBugDataUtils;
import com.abso.weatherbug.ui.IWeatherBugConstants;
import com.abso.weatherbug.ui.WeatherBugImages;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.actions.OpenBrowserAction;
import com.abso.weatherbug.ui.actions.RefreshLiveWeatherAction;
import com.abso.weatherbug.ui.jobs.RefreshForecastJob;
import com.abso.weatherbug.ui.jobs.RefreshLiveWeatherJob;
import com.abso.weatherbug.ui.jobs.RefreshLiveWeatherTask;
import com.abso.weatherbug.ui.viewers.LocationLabelProvider;
import com.abso.weatherbug.ui.viewers.StationLabelProvider;

/**
 * The live weather view displays live weather for a specific location.
 */
public class LiveWeatherView extends WeatherBugView {

    /** The label showing the location name. */
    private Label locationLabel;

    /** The input field for searching a location. */
    private Text locationText;

    /** The hyperling showing the current station and allowing the selection of a new one. */
    private Hyperlink stationLink;

    /** The label showing the current temperature. */
    private Label tempLabel;

    /** The label showing the current wind speed. */
    private Label windLabel;

    /** The label showing the lowest temperature measured today. */
    private Label todayLowTempLabel;

    /** The label showing the amount of rainfall so far today. */
    private Label todayRainLabel;

    /** The label showing the highest temperature measured today. */
    private Label todayHighTempLabel;

    /** The label showing the speed of strongest wind gust recently recorded. */
    private Label todayGustLabel;

    /** The label showing the Wind Chill (cold temps) or Heat Index (hot temps) temperature. */
    private Label heatIndexLabel;

    /** The label showing the time of last/next sunrise. */
    private Label sunriseLabel;

    /** The label showing the current relative humidity. */
    private Label humidityLabel;

    /** The label showing the time of last/next sunset. */
    private Label sunsetLabel;

    /** The label showing the temperature to which the air must be cooled to condense. */
    private Label dewPointLabel;

    /** The label showing the amount of rainfall so far this month. */
    private Label rainMonthLabel;

    /** The label showing the average speed of the wind so far today. */
    private Label avgWindLabel;

    /** The label showing the current barometric pressure. */
    private Label pressureLabel;

    /** The WeatherBug live weather URL for the current location. */
    private URL siteURL;

    /** The action to open the WeatherBug Web site. */
    private OpenBrowserAction openSiteAction;
    
    /** The refresh timer. */
    private Timer refreshTimer;

    /** The active refresh task. */
    private TimerTask refreshTask;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        Composite mainComp = new Composite(parent, SWT.NONE);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 3;
        gridLayout.marginWidth = 3;
        gridLayout.horizontalSpacing = 2;
        gridLayout.verticalSpacing = 2;

        mainComp.setLayout(gridLayout);
        mainComp.setBackground(IWeatherBugConstants.VIEW_BG_COLOR);
        mainComp.setBackgroundMode(SWT.INHERIT_DEFAULT);
        createTopComposite(mainComp);
        createBottomComposite(mainComp);

        /* sets the toolbar */
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        toolBarManager.add(new RefreshLiveWeatherAction(this));
        openSiteAction = new OpenBrowserAction(this);
        openSiteAction.setEnabled(false);
        toolBarManager.add(openSiteAction);
        toolBarManager.update(false);
        addFavoritesActions();
        addLocationHistoryAction();
        addUnitTypeActions();

        /* finally refreshes the weather data */
        updateHistory();
        refreshTimer = new Timer(true);
        new RefreshLiveWeatherJob(this, false).schedule();
    }

    /**
     * Creates the top composite.
     * 
     * @param parent
     *            the parent composite.
     */
    private void createTopComposite(Composite parent) {
        Composite topComp = new Composite(parent, SWT.NONE);
        topComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginHeight = 3;
        gridLayout.marginWidth = 3;
        gridLayout.horizontalSpacing = 2;
        gridLayout.verticalSpacing = 2;
        topComp.setLayout(gridLayout);

        /* adds the location label */
        locationLabel = createLabel(topComp, "", JFaceResources.getBannerFont(), IWeatherBugConstants.DARK_YELLOW_COLOR);
        locationLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        locationLabel.setText("Location: N/A");

        /* adds the location text field */
        locationText = new Text(topComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        locationText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        locationText.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                if (e.character == SWT.CR) {
                    if (!StringUtils.isBlank(locationText.getText())) {
                        search(locationText.getText());
                    }
                }
            }
        });

        /* adds the search button */
        ImageHyperlink link = new ImageHyperlink(topComp, SWT.TOP);
        link.setImage(WeatherBugImages.get(WeatherBugImages.SEARCH));
        link.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                if (!StringUtils.isBlank(locationText.getText())) {
                    search(locationText.getText());
                }
            }
        });
        link.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));

        /* adds the station label */
        stationLink = new Hyperlink(topComp, SWT.TOP);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 3;
        stationLink.setLayoutData(gridData);
        stationLink.setFont(JFaceResources.getDefaultFont());
        stationLink.setForeground(IWeatherBugConstants.WHITE_COLOR);
        stationLink.setText("Station: N/A");
        stationLink.setEnabled(false);
        stationLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                if (stationLink.isEnabled()) {
                    changeStation();
                }
            }
        });

        /* adds the temperature control */
        Composite tempComp = new Composite(topComp, SWT.NONE);
        tempComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        tempComp.setLayout(new GridLayout(1, true));
        createLabel(tempComp, "Temperature", JFaceResources.getDefaultFont(), IWeatherBugConstants.DARK_YELLOW_COLOR);
        tempLabel = createLabel(tempComp, "N/A", JFaceResources.getHeaderFont(), IWeatherBugConstants.WHITE_COLOR);
        tempLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

//        /* adds the wind label */
//        Label windHeaderLabel = createLabel(topComp, "Wind", JFaceResources.getDefaultFont(), IWeatherBugConstants.DARK_YELLOW_COLOR);
//        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
//        gridData.horizontalSpan = 2;
//        windHeaderLabel.setLayoutData(gridData);

        /* adds the wind control */
        Composite windComp = new Composite(topComp, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gridData.horizontalSpan = 2;
        windComp.setLayoutData(gridData);
        windComp.setLayout(new GridLayout(1, true));
        createLabel(windComp, "Wind", JFaceResources.getDefaultFont(), IWeatherBugConstants.DARK_YELLOW_COLOR);
        windLabel = createLabel(windComp, "N/A", JFaceResources.getHeaderFont(), IWeatherBugConstants.WHITE_COLOR);
        windLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    }

    /**
     * Creates the bottom composite area.
     * 
     * @param parent
     *            the parent composite.
     */
    private void createBottomComposite(Composite parent) {
        Composite bottomComp = new Composite(parent, SWT.NONE);
        bottomComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout(3, true);
        gridLayout.marginHeight = 3;
        gridLayout.marginWidth = 3;
        gridLayout.horizontalSpacing = 2;
        gridLayout.verticalSpacing = 2;
        bottomComp.setLayout(gridLayout);
        Label todayLabel = createLabel(bottomComp, "So Far Today", JFaceResources.getDefaultFont(),
                IWeatherBugConstants.DARK_YELLOW_COLOR);
        todayLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        Label detailedObsLabel = createLabel(bottomComp, "Detailed Observations", JFaceResources.getDefaultFont(),
                IWeatherBugConstants.DARK_YELLOW_COLOR);
        GridData detailedObsGridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        detailedObsGridData.horizontalSpan = 2;
        detailedObsLabel.setLayoutData(detailedObsGridData);
        todayLowTempLabel = createLabel(bottomComp, "Lo: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        todayLowTempLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        heatIndexLabel = createLabel(bottomComp, "Heat Index: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        heatIndexLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        sunriseLabel = createLabel(bottomComp, "Sunrise: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        sunriseLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        todayRainLabel = createLabel(bottomComp, "Rain: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        todayRainLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        humidityLabel = createLabel(bottomComp, "Humidity: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        humidityLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        sunsetLabel = createLabel(bottomComp, "Sunset: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        sunsetLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        todayHighTempLabel = createLabel(bottomComp, "Hi: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        todayHighTempLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        dewPointLabel = createLabel(bottomComp, "Dew Point: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        dewPointLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        rainMonthLabel = createLabel(bottomComp, "Rain/Month: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        rainMonthLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        todayGustLabel = createLabel(bottomComp, "Gust: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        todayGustLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        avgWindLabel = createLabel(bottomComp, "Avg Wind: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        avgWindLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        pressureLabel = createLabel(bottomComp, "Pressure: N/A", JFaceResources.getDefaultFont(), IWeatherBugConstants.WHITE_COLOR);
        pressureLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus() {
        locationText.setFocus();
    }

    /**
     * Refreshes the view with new live weather data.
     * 
     * @param liveWeather
     *            the new live weather.
     */
    public synchronized void refreshData(LiveWeather liveWeather) {
        if (liveWeather == null) {
            return;
        }
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        refreshTask = new RefreshLiveWeatherTask(this);
        int refreshRate = WeatherBugUIPlugin.getDefault().getLiveWeatherRefreshRate() * 1000;
        if (refreshRate > 0) {
            refreshTimer.scheduleAtFixedRate(refreshTask, refreshRate, refreshRate);
        }
        this.siteURL = liveWeather.getWeatherBugSiteURL();
        openSiteAction.setEnabled(siteURL != null);
        locationLabel.setText(WeatherBugUIPlugin.getDefault().getCurrentLocationLabel() + " "
                + WeatherBugDataUtils.formatTimestamp(liveWeather.getObservationTime(), "h:mm a"));
        stationLink.setText("Station: " + liveWeather.getStationName());
        stationLink.setEnabled(true);
        stationLink.setUnderlined(true);
        refreshDecimal(tempLabel, "", liveWeather.getTemperature(), "", liveWeather.getTemperatureUnits());
        refreshDecimal(windLabel, "", liveWeather.getWindSpeed(), " ", liveWeather.getWindSpeedUnits());
        refreshDecimal(todayLowTempLabel, "Lo: ", liveWeather.getLowestTemperature(), "", liveWeather.getLowestTemperatureUnits());
        refreshDecimal(todayRainLabel, "Rain: ", liveWeather.getRainToday(), " ", liveWeather.getRainTodayUnits());
        refreshDecimal(todayHighTempLabel, "Hi: ", liveWeather.getHighestTemperature(), "", liveWeather.getHighestTemperatureUnits());
        refreshGust(todayGustLabel, "Gust: ", liveWeather.getGustSpeed(), liveWeather.getGustDirection());
        refreshDecimal(heatIndexLabel, "Heat Index: ", liveWeather.getFeelsLike(), "", liveWeather.getFeelsLikeUnits());
        refreshTimestamp(sunriseLabel, "Sunrise: ", liveWeather.getSunriseTime());
        refreshDecimal(humidityLabel, "Humidity: ", liveWeather.getHumidity(), " ", liveWeather.getHumidityUnits());
        refreshTimestamp(sunsetLabel, "Sunset: ", liveWeather.getSunsetTime());
        refreshDecimal(dewPointLabel, "Dew Point: ", liveWeather.getDewPoint(), "", liveWeather.getDewPointUnits());
        refreshDecimal(rainMonthLabel, "Rain/Month: ", liveWeather.getRainMonth(), "", liveWeather.getRainMonthUnits());
        refreshDecimal(avgWindLabel, "Avg Wind: ", liveWeather.getAvgWindSpeed(), " ", liveWeather.getAvgWindDirection());
        refreshDecimal(pressureLabel, "Pressure: ", liveWeather.getPressure(), " ", liveWeather.getPressureUnits());
        if (true) {
            return;
        }
    }

    /**
     * Searches a new location.
     * 
     * @param searchString
     *            the string to match.
     */
    private void search(final String searchString) {
        try {
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(WeatherBugUIPlugin.getActiveWorkbenchShell()) {
                protected void configureShell(final Shell shell) {
                    super.configureShell(shell);
                    shell.setText("Location Search");
                }
            };
            SearchRunnable searchRunnable = new SearchRunnable(searchString);
            dialog.run(true, true, searchRunnable);
            if (searchRunnable.getServiceException() != null) {
                WeatherBugUIPlugin.getDefault().showErrorDialog("Error", "Unable to search location", "Error searching location",
                        searchRunnable.getServiceException());
            } else {
                Location[] locations = searchRunnable.getLocations();
                if (locations != null) {
                    if (locations.length == 0) {
                        WeatherBugUIPlugin.getDefault().showWarningDialog("No Result",
                                "No matches found for your request. Please try another location.");
                    } else if (locations.length == 1) {
                        LiveWeatherView liveWeatherView = LiveWeatherView.getActiveLiveWeatherView();
                        if (liveWeatherView != null) {
                            new RefreshLiveWeatherJob(locations[0], liveWeatherView, true).schedule();
                        }
                        ForecastView forecastView = ForecastView.getActiveForecastView();
                        if (forecastView != null) {
                            new RefreshForecastJob(locations[0], forecastView, true).schedule();
                        }
                    } else {

                        /* opens the selection dialog */
                        ListDialog listDialog = new ListDialog(WeatherBugUIPlugin.getActiveWorkbenchShell()) {

                            protected Control createDialogArea(Composite container) {
                                Control dialogArea = super.createDialogArea(container);
                                getTableViewer().setComparator(new ViewerComparator());
                                return dialogArea;
                            }

                        };
                        listDialog.setTitle("" + locations.length + " Results Found");
                        listDialog.setMessage("Please select your location:");
                        listDialog.setContentProvider(new ArrayContentProvider());
                        listDialog.setLabelProvider(new LocationLabelProvider());
                        listDialog.setInput(locations);
                        listDialog.setInitialSelections(new Object[] { locations[0] });
                        if (listDialog.open() == Window.OK) {
                            Object[] result = listDialog.getResult();
                            if ((result != null) && (result.length > 0)) {
                                Location location = (Location) result[0];
                                LiveWeatherView liveWeatherView = LiveWeatherView.getActiveLiveWeatherView();
                                if (liveWeatherView != null) {
                                    new RefreshLiveWeatherJob(location, liveWeatherView, true).schedule();
                                }
                                ForecastView forecastView = ForecastView.getActiveForecastView();
                                if (forecastView != null) {
                                    new RefreshForecastJob(location, forecastView, true).schedule();
                                }
                            }
                        }
                    }

                }
            }
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }
    }

    /**
     * Changes the weather station, by searching the stations near the current location and asking the user to select one of the found
     * stations.
     */
    private void changeStation() {
        try {
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(WeatherBugUIPlugin.getActiveWorkbenchShell()) {
                protected void configureShell(final Shell shell) {
                    super.configureShell(shell);
                    shell.setText("Station Selection");
                }
            };
            Location currentLocation = WeatherBugUIPlugin.getDefault().getCurrentLocation();
            GetStationsRunnable getStationsRunnable = new GetStationsRunnable(currentLocation.getZipCode() != -1, (currentLocation
                    .getZipCode() != -1) ? currentLocation.getZipCode() : currentLocation.getCityCode());
            dialog.run(true, true, getStationsRunnable);
            if (getStationsRunnable.getServiceException() != null) {
                WeatherBugUIPlugin.getDefault().showErrorDialog("Error", "Unable to retrieve stations", "Error retrieving stations",
                        getStationsRunnable.getServiceException());
            } else {
                Station[] stations = getStationsRunnable.getStations();
                if (stations != null) {
                    if (stations.length == 0) {
                        WeatherBugUIPlugin.getDefault().showWarningDialog("No Result", "No stations available.");
                    } else {

                        /* opens the selection dialog */
                        ListDialog listDialog = new ListDialog(WeatherBugUIPlugin.getActiveWorkbenchShell()) {

                            protected Control createDialogArea(Composite container) {
                                Control dialogArea = super.createDialogArea(container);
                                getTableViewer().setComparator(new ViewerComparator());
                                return dialogArea;
                            }

                        };
                        listDialog.setTitle("" + stations.length + " Results Found");
                        listDialog.setMessage("Please select the station:");
                        listDialog.setContentProvider(new ArrayContentProvider());
                        listDialog.setLabelProvider(new StationLabelProvider());
                        listDialog.setInput(stations);
                        listDialog.setInitialSelections(new Object[] { stations[0] });
                        if (listDialog.open() == Window.OK) {
                            Object[] result = listDialog.getResult();
                            if ((result != null) && (result.length > 0)) {
                                Station station = (Station) result[0];
                                LiveWeatherView liveWeatherView = LiveWeatherView.getActiveLiveWeatherView();
                                if (liveWeatherView != null) {
                                    new RefreshLiveWeatherJob(station, liveWeatherView, true).schedule();
                                }
                            }
                        }
                    }

                }
            }
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }
    }

    /**
     * Returns the live weather view active in the active workbench window.
     * 
     * @return the active live weather view, or <code>null</code> if no view is active in the active workbench window.
     */
    public static LiveWeatherView getActiveLiveWeatherView() {
        IWorkbenchWindow window = WeatherBugUIPlugin.getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return null;
        }
        return (LiveWeatherView) page.findView(IWeatherBugConstants.ID_LIVE_WEATHER_VIEW);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.abso.weatherbug.ui.views.WeatherBugView#getWeatherBugSiteURL()
     */
    public URL getWeatherBugSiteURL() {
        return siteURL;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
        super.dispose();
    }
    
    /**
     * A runnable able to search stations near the current location.
     */
    private static final class GetStationsRunnable implements IRunnableWithProgress {

        /** If <code>true</code> the current location is a U.S. location. */
        private boolean usLocation;

        /** The U.S. ZIP code or the city code for a city located outside the U.S. */
        private int locationCode;

        /** The list of found stations. */
        private Station[] stations;

        /** The exception thrown by the service execution. */
        private WeatherBugServiceException serviceException;

        /**
         * Constructs a new runnable.
         * 
         * @param usLocation
         *            indicates if the current location is a U.S. location.
         * @param locationCode
         *            the U.S. ZIP code or the city code for a city located outside the U.S.
         */
        public GetStationsRunnable(boolean usLocation, int locationCode) {
            this.usLocation = usLocation;
            this.locationCode = locationCode;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            try {
                this.stations = WeatherBugUIPlugin.getDefault().searchStations(usLocation, locationCode);
            } catch (WeatherBugServiceException e) {
                this.serviceException = e;
            }
            if (monitor.isCanceled()) {
                this.stations = null;
                this.serviceException = null;
            }
        }

        /**
         * Returns the found stations.
         * 
         * @return the stations near the current location.
         */
        public Station[] getStations() {
            return stations;
        }

        /**
         * Returns the exception (if any) thrown by the service execution.
         * 
         * @return the service exception.
         */
        public WeatherBugServiceException getServiceException() {
            return serviceException;
        }

    }

}
