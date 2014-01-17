package de.gemo.smartlauncher.internet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GETProperty {

    private final String name;
    private final String value;

    public GETProperty(String name, String value) {
        this.name = name;
        this.value = value.trim();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        try {
            return this.name + '=' + URLEncoder.encode(this.value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return this.name + '=' + this.value;
        }
    }
}
