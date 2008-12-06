package com.abso.weatherbug.ui.viewers;

import org.eclipse.jface.viewers.LabelProvider;

import com.abso.weatherbug.core.data.Station;

/**
 * A specialized label provider for showing weather stations.
 */
public class StationLabelProvider extends LabelProvider {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        Station station = (Station) element;
        if (station.getZipCode() != -1) {
            return station.getName() + ", " + station.getState();
        } else {
            return station.getName() + " - " + station.getCity() + ", " + station.getCountry();
        }
    }
}
