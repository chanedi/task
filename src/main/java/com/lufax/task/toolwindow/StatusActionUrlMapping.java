package com.lufax.task.toolwindow;

public class StatusActionUrlMapping implements Cloneable {

    public static final String DEFAULT_STATUS = "default";
    private String status;
    private String url;
    private ActionUrl.HTTPMethod method;
    private boolean isDefault = false;

    public StatusActionUrlMapping() {
        this.status = "";
        this.url = "";
        this.method = ActionUrl.HTTPMethod.GET;
    }

    public StatusActionUrlMapping(String status, String url, ActionUrl.HTTPMethod method) {
        this.status = status;
        this.url = url;
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ActionUrl.HTTPMethod getMethod() {
        return method;
    }

    public void setMethod(ActionUrl.HTTPMethod method) {
        this.method = method;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public StatusActionUrlMapping clone() {
        try {
            return (StatusActionUrlMapping) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
