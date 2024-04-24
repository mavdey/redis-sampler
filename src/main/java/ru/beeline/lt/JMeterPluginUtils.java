package ru.beeline.lt;

import java.util.regex.Pattern;

public class JMeterPluginUtils {

    private static final Pattern DETECT_JMETER_VAR_REGEX = Pattern.compile("\\$\\{[^}]+\\}");

    public static String stripJMeterVariables(String data) {
        return DETECT_JMETER_VAR_REGEX.matcher(data).replaceAll("");
    }

    public static String prefixLabel(String label) {
        String PLUGINS_PREFIX = "@beeload - ";
        return PLUGINS_PREFIX + label;
    }
}
