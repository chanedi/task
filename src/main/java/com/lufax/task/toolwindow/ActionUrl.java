package com.lufax.task.toolwindow;

import com.intellij.util.net.HTTPMethod;

public class ActionUrl {

    private String url;
    private HTTPMethod method = HTTPMethod.GET;

    public ActionUrl() {
        this("", HTTPMethod.GET);
    }

    public ActionUrl(String url, HTTPMethod method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public void setMethod(HTTPMethod method) {
        this.method = method;
    }
}
