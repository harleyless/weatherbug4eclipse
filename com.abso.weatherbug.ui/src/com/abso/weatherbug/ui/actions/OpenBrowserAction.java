package com.abso.weatherbug.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.abso.weatherbug.ui.IWeatherBugConstants;
import com.abso.weatherbug.ui.WeatherBugImages;
import com.abso.weatherbug.ui.views.WeatherBugView;

/**
 * Action able to open a specific browser URL.
 */
public class OpenBrowserAction extends Action {

    /** The live weather or forecast view this action is associated with. */
    private WeatherBugView view;

    /**
     * Constructs a new action.
     * 
     * @param label
     *            the action's text.
     * @param imageDescriptor
     *            the action's image.
     * @param view
     *            the live weather or forecast view.
     */
    public OpenBrowserAction(WeatherBugView view) {
        super("Open WeatherBug Web site", WeatherBugImages.getDescriptor(WeatherBugImages.OPEN_WEB_SITE));
        this.view = view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        BusyIndicator.showWhile(null, new Runnable() {
            public void run() {
                IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
                try {
                    IWebBrowser browser = support.createBrowser(IWorkbenchBrowserSupport.AS_EDITOR
                            | IWorkbenchBrowserSupport.NAVIGATION_BAR | IWorkbenchBrowserSupport.STATUS
                            | IWorkbenchBrowserSupport.LOCATION_BAR,

                    IWeatherBugConstants.BROWSER_ID, null, null);
                    browser.openURL(view.getWeatherBugSiteURL());
                } catch (PartInitException e) {
                }
            }
        });

    }

}
