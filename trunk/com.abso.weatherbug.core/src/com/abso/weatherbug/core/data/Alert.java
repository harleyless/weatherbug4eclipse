package com.abso.weatherbug.core.data;

import java.sql.Timestamp;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.dom4j.Element;

/**
 * A severe weather alert.
 */
public class Alert {

    /** The expiration time of the alert. */
    private Timestamp expiresTime;

    /** The unique identifier. */
    private String id;

    /** The message summary. */
    private String messageSummary;

    /** The timestamp when the alert has been posted. */
    private Timestamp postedTime;

    /** The title of the alert. */
    private String title;

    /** The type of the alert. */
    private String type;

    /**
     * Constructs a new alert.
     * 
     * @param alert
     *            the &lt;aws:alert&gt; XML element.
     */
    public Alert(Element alert) {
        this.id = WeatherBugDataUtils.getString(alert, "aws:id");
        this.type = WeatherBugDataUtils.getString(alert, "aws:type");
        this.title = WeatherBugDataUtils.getString(alert, "aws:title");
        this.postedTime = WeatherBugDataUtils.getTimestamp(alert, "aws:posted-date");
        this.expiresTime = WeatherBugDataUtils.getTimestamp(alert, "aws:expires-date");
        this.messageSummary = WeatherBugDataUtils.getString(alert, "aws:msg-summary");
    }

    /**
     * Returns the expiration time.
     * 
     * @return the expiration time.
     */
    public Timestamp getExpiresTime() {
        return expiresTime;
    }

    /**
     * Returns the unique identifier
     * 
     * @return the unique identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the message summary.
     * 
     * @return the message summary
     */
    public String getMessageSummary() {
        return messageSummary;
    }

    /**
     * Returns the timestamp when the alert has been posted.
     * 
     * @return the posting timestamp.
     */
    public Timestamp getPostedTime() {
        return postedTime;
    }

    /**
     * Returns the title of the alert.
     * 
     * @return the title of the alert.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the type of the alert.
     * 
     * @return the type of the alert.
     */
    public String getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
