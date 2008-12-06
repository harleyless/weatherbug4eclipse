package com.abso.weatherbug.ui.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.abso.weatherbug.ui.IWeatherBugConstants;
import com.abso.weatherbug.ui.WeatherBugUIPlugin;

/**
 * The WeatherBug preference page.
 */
public class WeatherBugPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/** Constructs a new page. */
	public WeatherBugPreferencePage() {
		super(GRID);
		setPreferenceStore(WeatherBugUIPlugin.getDefault().getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
        FieldEditor chunkSizeField = new IntegerFieldEditor(IWeatherBugConstants.PREF_FORECAST_REFRESH_RATE,
                "Forecast Refresh Rate (seconds):", getFieldEditorParent());
        addField(chunkSizeField);
		FieldEditor timeoutField = new IntegerFieldEditor(IWeatherBugConstants.PREF_LIVE_WEATHER_REFRESH_RATE,
				"Live Weather Refresh Rate (seconds):", getFieldEditorParent());
		addField(timeoutField);
	}

}
