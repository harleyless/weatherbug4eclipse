package com.abso.weatherbug.ui.views;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
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
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import com.abso.weatherbug.core.data.Forecast;
import com.abso.weatherbug.core.data.Forecasts;
import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.ui.IWeatherBugConstants;
import com.abso.weatherbug.ui.WeatherBugImages;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;
import com.abso.weatherbug.ui.actions.OpenBrowserAction;
import com.abso.weatherbug.ui.actions.RefreshForecastAction;
import com.abso.weatherbug.ui.jobs.RefreshForecastJob;
import com.abso.weatherbug.ui.jobs.RefreshForecastTask;
import com.abso.weatherbug.ui.jobs.RefreshLiveWeatherJob;
import com.abso.weatherbug.ui.viewers.LocationLabelProvider;

/**
 * The forecast view displays weather forecast for a specific location.
 */
public class ForecastView extends WeatherBugView {

    /** The label showing the location name. */
    private Label locationLabel;

    /** The input field for searching a location. */
    private Text locationText;

    /** The composite control rebuilt each time forecast data is refreshed. */
    private Composite forecastsComp;

    /** The WeatherBug forecast URL for the current location. */
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
        mainComp.setLayout(new GridLayout(1, true));
        mainComp.setBackground(IWeatherBugConstants.VIEW_BG_COLOR);
        mainComp.setBackgroundMode(SWT.INHERIT_DEFAULT);
        createLocationControl(mainComp);
        createForecastsControl(mainComp);

        /* sets the toolbar */
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        toolBarManager.add(new RefreshForecastAction(this));
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
        new RefreshForecastJob(this, false).schedule();
    }

    /**
     * Creates the control displaying a location and allowing to search locations.
     * 
     * @param parent
     *            the parent composite.
     */
    private void createLocationControl(Composite parent) {
        Composite locationComp = new Composite(parent, SWT.NONE);
        locationComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        locationComp.setLayout(new GridLayout(3, false));
        locationLabel = createLabel(locationComp, "", JFaceResources.getBannerFont(), IWeatherBugConstants.DARK_YELLOW_COLOR);
        locationLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        locationText = new Text(locationComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
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
        ImageHyperlink link = new ImageHyperlink(locationComp, SWT.TOP);
        link.setImage(WeatherBugImages.get(WeatherBugImages.SEARCH));
        link.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                if (!StringUtils.isBlank(locationText.getText())) {
                    search(locationText.getText());
                }
            }
        });
    }

    /**
     * Creates the control displaying the weather forecast.
     * 
     * @param parent
     *            the parent composite.
     */
    private void createForecastsControl(Composite parent) {
        forecastsComp = new Composite(parent, SWT.NONE);
        forecastsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        RowLayout rowLayout = new RowLayout();
        rowLayout.marginLeft = 0;
        rowLayout.marginTop = 0;
        rowLayout.marginRight = 0;
        rowLayout.marginBottom = 0;
        rowLayout.spacing = 1;
        rowLayout.wrap = true;
        rowLayout.pack = false;
        rowLayout.justify = false;
        rowLayout.type = SWT.HORIZONTAL;
        forecastsComp.setLayout(rowLayout);
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
                        ForecastView forecastView = ForecastView.getActiveForecastView();
                        if (forecastView != null) {
                            new RefreshForecastJob(locations[0], forecastView, true).schedule();
                        }
                        LiveWeatherView liveWeatherView = LiveWeatherView.getActiveLiveWeatherView();
                        if (liveWeatherView != null) {
                            new RefreshLiveWeatherJob(locations[0], liveWeatherView, true).schedule();
                        }

                    } else {

                        /* opens the selection dialog */
                        ListDialog listDialog = new ListDialog(WeatherBugUIPlugin.getActiveWorkbenchShell());
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
                    }

                }
            }
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }
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
     * Refreshes the view with new forecast data.
     * 
     * @param forecastsSet
     *            the set of forecast.
     */
    public synchronized void refreshData(Forecasts forecastsSet) {
        if (forecastsSet == null) {
            return;
        }
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        refreshTask = new RefreshForecastTask(this);
        int refreshRate = WeatherBugUIPlugin.getDefault().getForecastRefreshRate() * 1000;
        if (refreshRate > 0) {
            refreshTimer.scheduleAtFixedRate(refreshTask, refreshRate, refreshRate);
        }
        this.siteURL = forecastsSet.getWeatherBugSiteURL();
        openSiteAction.setEnabled(siteURL != null);
        locationLabel.setText(WeatherBugUIPlugin.getDefault().getCurrentLocationLabel());
        Control[] forecastItems = forecastsComp.getChildren();
        for (int i = 0; i < forecastItems.length; i++) {
            forecastItems[i].dispose();
        }
        Forecast[] forecasts = forecastsSet.getForecasts();
        for (int i = 0; i < forecasts.length; i++) {
            ForecastItem item = new ForecastItem(forecasts[i], forecastsComp);
            item.setLayoutData(new RowData(SWT.DEFAULT, SWT.DEFAULT));
        }
        forecastsComp.layout(true);
        forecastsComp.pack(true);
    }

    /**
     * Returns the forecast view active in the active workbench window.
     * 
     * @return the active forecast view, or <code>null</code> if no view is active in the active workbench window.
     */
    public static ForecastView getActiveForecastView() {
        IWorkbenchWindow window = WeatherBugUIPlugin.getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return null;
        }
        return (ForecastView) page.findView(IWeatherBugConstants.ID_FORECAST_VIEW);
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
     * A specialized composite control displaying the forecast for a single day.
     */
    private static final class ForecastItem extends Composite {

        /**
         * Constructs a new item.
         * 
         * @param forecast
         *            the daily forecast.
         * @param parent
         *            the parent composite.
         */
        public ForecastItem(Forecast forecast, Composite parent) {
            super(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.horizontalSpacing = 2;
            gridLayout.verticalSpacing = 2;
            gridLayout.marginWidth = 2;
            gridLayout.marginHeight = 2;
            setLayout(gridLayout);
            setBackground(IWeatherBugConstants.WHITE_COLOR);
            createLabel(forecast.getTitle(), JFaceResources.getBannerFont(), IWeatherBugConstants.BLACK_COLOR);
            Label iconLabel = new Label(this, SWT.CENTER);
            iconLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
            String index = StringUtils.substringAfterLast(StringUtils.substringBefore(forecast.getIconName(), ".gif"), "cond");
            Image image = WeatherBugImages.get(WeatherBugImages.CONDITION + index);
            if (image == null) {
                image = WeatherBugImages.get(WeatherBugImages.CONDITION + "999");
            }
            iconLabel.setImage(image);
            iconLabel.setToolTipText(WordUtils.wrap(forecast.getPrediction(), 50));
            createLabel("Hi: " + formatTemperature(forecast.getHighestTemperature()), JFaceResources.getBannerFont(),
                    IWeatherBugConstants.HIGH_TEMP_COLOR);
            createLabel("Lo: " + formatTemperature(forecast.getLowestTemperature()), JFaceResources.getBannerFont(),
                    IWeatherBugConstants.LOW_TEMP_COLOR);
            createLabel(StringUtils.join(StringUtils.split(forecast.getShortPrediction()), SystemUtils.LINE_SEPARATOR), JFaceResources
                    .getDefaultFont(), IWeatherBugConstants.BLACK_COLOR);
        }

        /**
         * Creates a label.
         * 
         * @param text
         *            the label's text.
         * @param font
         *            the font.
         * @param color
         *            the foreground color.
         */
        private void createLabel(String text, Font font, Color color) {
            Label label = new Label(this, SWT.CENTER);
            label.setFont(font);
            label.setForeground(color);
            label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
            label.setText(text);
        }

        /**
         * Formats a temperature value into a string.
         * 
         * @param temperature
         *            the temperature to format.
         * @return the formatted string.
         */
        private String formatTemperature(BigDecimal temperature) {
            if (temperature == null) {
                return "N/A";
            } else {
                return temperature.toString() + "\u00B0";
            }
        }

    }

}
