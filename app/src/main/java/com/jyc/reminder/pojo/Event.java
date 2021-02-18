package com.jyc.reminder.pojo;

public class Event {

    private String uuid;
    private String title;
    private String datetime;
    private Integer remindMinute;
    private Boolean overdue;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Integer getRemindMinute() {
        return remindMinute;
    }

    public void setRemindMinute(Integer remindMinute) {
        this.remindMinute = remindMinute;
    }

    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }

    public String toString() {
        return String.format("%s - %s", this.datetime, this.title);
    }
}
