package com.lufax.task.repository;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.MalformedCookieException;
import org.apache.commons.httpclient.cookie.RFC2109Spec;

public class LooseRFC2109Spec extends RFC2109Spec {

    public void validate(String host, int port, String path, boolean secure, Cookie cookie) throws MalformedCookieException {
        try {
            super.validate(host, port, path, secure, cookie);
        } catch (MalformedCookieException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

}
