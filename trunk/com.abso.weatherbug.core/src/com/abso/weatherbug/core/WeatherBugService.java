package com.abso.weatherbug.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.ObjectUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import com.abso.weatherbug.core.data.Alert;
import com.abso.weatherbug.core.data.Forecasts;
import com.abso.weatherbug.core.data.LiveCompactWeather;
import com.abso.weatherbug.core.data.LiveWeather;
import com.abso.weatherbug.core.data.Location;
import com.abso.weatherbug.core.data.Station;
import com.abso.weatherbug.core.data.WeatherBugDataUtils;

/**
 * The WeatherBug service.
 */
public class WeatherBugService {

    /** HTTP connection default time-out (in milliseconds). */
    public static final int DEFAULT_TIMEOUT = 10000;

    /**
     * The alphanumeric license key issued by WeatherBug.
     */
    private String aCode;

    /** The current time-out (default = DEFAULT_TIMEOUT). */
    private int timeout = DEFAULT_TIMEOUT;

    /* static initializer */
    static {
        Map nsURIs = DocumentFactory.getInstance().getXPathNamespaceURIs();
        if (nsURIs == null) {
            nsURIs = new HashMap();
        }
        nsURIs.put("aws", "http://www.aws.com/aws");
        DocumentFactory.getInstance().setXPathNamespaceURIs(nsURIs);
    }

    /**
     * Constructs a new WeatherBug service.
     * 
     * @param aCode
     *            the alphanumeric license key issued by WeatherBug.
     */
    public WeatherBugService(String aCode) {
        this.aCode = aCode;
    }

    /**
     * Sets the timeout.
     * 
     * @param timeout
     *            the timeout.
     * @see #DEFAULT_TIMEOUT
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the set of locations matching a specific name. The soundex system allows searching for a city based on phonetics rather
     * than spelling.
     * 
     * @param searchString
     *            the search string.
     * @return the set of matching locations.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public Location[] getLocationList(String searchString) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("SearchString", searchString);
        Document doc = execute("getLocationsXML", params);
        List locations = WeatherBugDataUtils.bind(doc, "aws:locations/aws:location", Location.class);
        return (Location[]) locations.toArray(new Location[0]);
    }

    /**
     * For a given ZIP code returns the weather stations in the area.
     * 
     * @param zipCode
     *            the ZIP code to match.
     * @return the set of weather stations in the area.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public Station[] getStationListByUSZipCode(int zipCode) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("zipCode", Integer.toString(zipCode));
        Document doc = execute("getStationsXML", params);
        List stations = WeatherBugDataUtils.bind(doc, "aws:stations/aws:station", Station.class);
        return (Station[]) stations.toArray(new Station[0]);
    }

    /**
     * For a given city code returns the weather stations in the area.
     * 
     * @param cityCode
     *            the city code to match.
     * @return the set of weather stations in the area.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public Station[] getStationListByCityCode(int cityCode) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("cityCode", Integer.toString(cityCode));
        Document doc = execute("getStationsXML", params);
        List stations = WeatherBugDataUtils.bind(doc, "aws:stations/aws:station", Station.class);
        return (Station[]) stations.toArray(new Station[0]);
    }

    /**
     * For a given latitude and longitude returns the weather stations in the area.
     * 
     * @param latitude
     *            the latitude.
     * @param longitude
     *            the longitude.
     * @return the set of weather stations in the area.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public Station[] getStationListByLatLong(double latitude, double longitude) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("lat", WeatherBugDataUtils.formatNumber(latitude, "0.00"));
        params.put("long", WeatherBugDataUtils.formatNumber(longitude, "0.00"));
        Document doc = execute("getStationsXML", params);
        List stations = WeatherBugDataUtils.bind(doc, "/aws:weather/aws:stations/aws:station", Station.class);
        return (Station[]) stations.toArray(new Station[0]);
    }

    /**
     * Gets the live weather based on a station.
     * 
     * @param stationId
     *            the station identifier.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the live weather.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public LiveWeather getLiveWeatherByStationID(String stationId, int unitType) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("stationid", stationId);
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getLiveWeatherRSS", params);
        return (LiveWeather) WeatherBugDataUtils.bindSingle(doc, "/rss/channel/aws:weather", LiveWeather.class);

    }

    /**
     * Gets the live weather based on a U.S. ZIP code.
     * 
     * @param zipCode
     *            the ZIP code to match.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the live weather.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public LiveWeather getLiveWeatherByUSZipCode(int zipCode, int unitType) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("zipCode", Integer.toString(zipCode));
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getLiveWeatherRSS", params);
        return (LiveWeather) WeatherBugDataUtils.bindSingle(doc, "/rss/channel/aws:weather", LiveWeather.class);
    }

    /**
     * Gets the live weather for a city located outside of the U.S.
     * 
     * @param cityCode
     *            the city code to match.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the live weather.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public LiveWeather getLiveWeatherByCityCode(int cityCode, int unitType) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("cityCode", Integer.toString(cityCode));
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getLiveWeatherRSS", params);
        return (LiveWeather) WeatherBugDataUtils.bindSingle(doc, "/rss/channel/aws:weather", LiveWeather.class);
    }

    /**
     * Gets the live compact weather based on a station.
     * 
     * @param stationId
     *            the station identifier.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the live compact weather.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public LiveCompactWeather getLiveCompactWeatherByStationID(String stationId, int unitType) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("stationid", stationId);
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getLiveCompactWeatherRSS", params);
        return (LiveCompactWeather) WeatherBugDataUtils.bindSingle(doc, "/rss/channel/aws:weather", LiveCompactWeather.class);
    }

    /**
     * Gets the live compact weather based on a U.S. ZIP code.
     * 
     * @param zipCode
     *            the ZIP code to match.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the live compact weather.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public LiveCompactWeather getLiveCompactWeatherByUSZipCode(int zipCode, int unitType) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("zipCode", Integer.toString(zipCode));
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getLiveCompactWeatherRSS", params);
        return (LiveCompactWeather) WeatherBugDataUtils.bindSingle(doc, "/rss/channel/aws:weather", LiveCompactWeather.class);
    }

    /**
     * Gets the live compact weather for a city located outside of the U.S.
     * 
     * @param cityCode
     *            the city code to match.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the live compact weather.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public LiveCompactWeather getLiveCompactWeatherByCityCode(int cityCode, int unitType) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("cityCode", Integer.toString(cityCode));
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getLiveCompactWeatherRSS", params);
        return (LiveCompactWeather) WeatherBugDataUtils.bindSingle(doc, "/rss/channel/aws:weather", LiveCompactWeather.class);
    }

    /**
     * For a given latitude and longitude returns the live compact weather.
     * 
     * @param latitude
     *            the latitude.
     * @param longitude
     *            the longitude.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the live compact weather.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public LiveCompactWeather getLiveCompactWeatherByLatLong(double latitude, double longitude, int unitType)
            throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("lat", WeatherBugDataUtils.formatNumber(latitude, "0.00"));
        params.put("long", WeatherBugDataUtils.formatNumber(longitude, "0.00"));
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getLiveCompactWeatherRSS", params);
        return (LiveCompactWeather) WeatherBugDataUtils.bindSingle(doc, "/rss/channel/aws:weather", LiveCompactWeather.class);
    }

    /**
     * Gets the forecast based on a U.S. ZIP code.
     * 
     * @param zipCode
     *            the ZIP code to match.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the forecast.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public Forecasts getForecastByUSZipCode(int zipCode, int unitType) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("zipCode", Integer.toString(zipCode));
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getForecastRSS", params);
        return (Forecasts) WeatherBugDataUtils.bindSingle(doc, "/rss/channel/aws:weather", Forecasts.class);
    }

    /**
     * Gets the forecast for a city located outside of the U.S.
     * 
     * @param cityCode
     *            the city code to match.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the forecast.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public Forecasts getForecastByCityCode(int cityCode, int unitType) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("cityCode", Integer.toString(cityCode));
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getForecastRSS", params);
        return (Forecasts) WeatherBugDataUtils.bindSingle(doc, "/rss/channel/aws:weather", Forecasts.class);
    }

    /**
     * For a given latitude and longitude returns the forecast.
     * 
     * @param latitude
     *            the latitude.
     * @param longitude
     *            the longitude.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the forecast.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public Forecasts getForecastByLatLong(double latitude, double longitude, int unitType) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("lat", WeatherBugDataUtils.formatNumber(latitude, "0.00"));
        params.put("long", WeatherBugDataUtils.formatNumber(longitude, "0.00"));
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getForecastRSS", params);
        return (Forecasts) WeatherBugDataUtils.bindSingle(doc, "/rss/channel/aws:weather", Forecasts.class);
    }

    /**
     * Gets the weather alerts based on a U.S. ZIP code.
     * 
     * @param zipCode
     *            the ZIP code to match.
     * @param unitType
     *            the unit type: <code>0</code> for U.S. customary units or <code>1</code> for Metric system units
     * @return the weather alerts.
     * @throws WeatherBugServiceException
     *             if an error occurred processing the service response.
     */
    public Alert[] getAlerts(int zipCode, int unitType) throws WeatherBugServiceException {
        Map params = new HashMap();
        params.put("zipCode", Integer.toString(zipCode));
        params.put("UnitType", Integer.toString(unitType));
        Document doc = execute("getAlertsRSS", params);
        List alerts = WeatherBugDataUtils.bind(doc, "/rss/channel/aws:weather/aws:alerts/aws:alert", Alert.class);
        return (Alert[]) alerts.toArray(new Alert[0]);
    }

    /**
     * Executes a REST method.
     * 
     * @param methodName
     *            the name of the method.
     * @param params
     *            the set of parameters being appended to the secure request.
     * @return the resulting XML document.
     * @throws WeatherBugServiceException
     *             if an error occurred executing the method.
     */
    private Document execute(String methodName, Map params) throws WeatherBugServiceException {
        StringBuffer url = new StringBuffer();
        url.append("http://");
        url.append(aCode);
        url.append(".api.wxbug.net/");
        url.append(methodName);
        url.append(".aspx?");
        for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
            Map.Entry param = (Map.Entry) i.next();
            try {
                url.append(encodeURLParameter(ObjectUtils.toString(param.getKey())));
            } catch (UnsupportedEncodingException e) {
                throw new WeatherBugServiceException("Unable to encode parameter: " + param.getKey(), e);
            }
            url.append('=');
            try {
                url.append(encodeURLParameter(ObjectUtils.toString(param.getValue())));
            } catch (UnsupportedEncodingException e) {
                throw new WeatherBugServiceException("Unable to encode parameter: " + param.getValue(), e);
            }
            url.append('&');
        }
        url.append("ACode=");
        url.append(aCode);
        SimpleHttpConnectionManager connMgr = new SimpleHttpConnectionManager();
        connMgr.getParams().setConnectionTimeout(timeout);
        connMgr.getParams().setSoTimeout(timeout);
        connMgr.getParams().setParameter(HttpMethodParams.USER_AGENT, "WeatherBug Eclipse plug-in (http://abso.freehostia.com)");
        HttpMethodBase httpMethod = new GetMethod(url.toString());
        HttpClient httpClient = new HttpClient(connMgr);
        try {
            httpClient.executeMethod(httpMethod);
            SAXReader reader = new SAXReader();
            InputSource source = new InputSource(httpMethod.getResponseBodyAsStream());
            source.setEncoding("UTF-8"/* httpMethod.getResponseCharSet() */);
            return reader.read(source);
        } catch (HttpException e) {
            throw new WeatherBugServiceException("Unable to perform request: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new WeatherBugServiceException("Unable to perform request: " + e.getMessage(), e);
        } catch (DocumentException e) {
            throw new WeatherBugServiceException("Unable to parse response of request: " + e.getMessage(), e);
        } finally {
            httpMethod.releaseConnection();
        }
    }

    /**
     * Encodes a URL parameter using the UTF-8 encoding.
     * 
     * @param param
     *            the parameter being encoded.
     * @return the encoded parameter.
     * @throws UnsupportedEncodingException
     *             if the parameter cannot be encoded.
     */
    private String encodeURLParameter(String param) throws UnsupportedEncodingException {
        String encodedPath = URLEncoder.encode(param, "UTF8");
        encodedPath = encodedPath.replaceAll("\\+", "%20");
        return encodedPath;
    }

}
