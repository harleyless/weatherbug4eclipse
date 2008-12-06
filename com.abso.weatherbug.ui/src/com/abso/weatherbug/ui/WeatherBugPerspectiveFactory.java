package com.abso.weatherbug.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Defines the layout of the WeatherBug UI plug-in perspective.
 */
public class WeatherBugPerspectiveFactory implements IPerspectiveFactory {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        IFolderLayout bottomFolder = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.65, editorArea);
        bottomFolder.addView(IWeatherBugConstants.ID_FORECAST_VIEW);
        bottomFolder.addView(IPageLayout.ID_PROP_SHEET);
        bottomFolder.addView("org.eclipse.pde.runtime.LogView");
        IFolderLayout bottomLeftFolder = layout.createFolder("bottomLeft", IPageLayout.LEFT, (float) 0.40, "bottom");
        bottomLeftFolder.addView(IWeatherBugConstants.ID_LIVE_WEATHER_VIEW);
        layout.addShowViewShortcut(IWeatherBugConstants.ID_LIVE_WEATHER_VIEW);
        layout.addShowViewShortcut(IWeatherBugConstants.ID_FORECAST_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
    }

}
