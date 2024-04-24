package ru.beeline.lt.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class GuiUtils {

    private static final Logger log = LoggerFactory.getLogger(GuiUtils.class);

    /**
     * Color used for error messages and to signal invalid input
     */
    private static final Color DEFAULT_ERROR_COLOR = Color.RED;
    /**
     * Color used for "OK" icon
     */
    private static final Color DEFAULT_OK_ICON_COLOR = Color.GREEN.darker();
    /**
     * Color used for "ERROR" icon
     */
    private static final Color DEFAULT_ERROR_ICON_COLOR = Color.RED;
    /**
     * Color for disabled elements when UIDefaults color is undefined
     */
    private static Color DEFAULT_DISABLED_COLOR = Color.GRAY;
    /**
     * Color for foreground when UIDefaults color is undefined
     */
    private static Color DEFAULT_FOREGROUND_COLOR = Color.BLACK;

    public static Color getLookAndFeelColor(String colorKey) {
        Color color;
        if (colorKey.equals("TextField.errorForeground")) {
            color = DEFAULT_ERROR_COLOR;
            LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
            if (lookAndFeel != null && lookAndFeel.getName().contains("Darcula"))
                color = color.darker();
        } else if (colorKey.equals("Icon.okForeground"))
            color = DEFAULT_OK_ICON_COLOR;
        else if (colorKey.equals("Icon.errorForeground"))
            color = DEFAULT_ERROR_ICON_COLOR;
        else {
            color = UIManager.getDefaults().getColor(colorKey);
            if (color == null) {
                log.error("UIManager does not support color key '" + colorKey + "'");
                if (colorKey.contains("foreground"))
                    color = DEFAULT_FOREGROUND_COLOR;
                else if (colorKey.contains("title"))
                    color = DEFAULT_FOREGROUND_COLOR;
                else if (colorKey.contains("disabled"))
                    color = DEFAULT_DISABLED_COLOR;
                else {
                    log.error("Undefined color key '" + colorKey + "'");
                    color = Color.RED;
                }
            }
        }
        return color;
    }

}
