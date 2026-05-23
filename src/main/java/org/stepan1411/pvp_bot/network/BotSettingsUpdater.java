package org.stepan1411.pvp_bot.network;

import org.stepan1411.pvp_bot.bot.BotSettings;

import java.lang.reflect.Method;

public class BotSettingsUpdater {
    public static boolean update(String key, String value) {
        BotSettings s = BotSettings.get();
        try {
            Method method = BotSettings.class.getMethod("set" + key, boolean.class);
            method.invoke(s, Boolean.parseBoolean(value));
            return true;
        } catch (ReflectiveOperationException ignored) {}

        try {
            Method method = BotSettings.class.getMethod("set" + key, int.class);
            method.invoke(s, Integer.parseInt(value));
            return true;
        } catch (ReflectiveOperationException | NumberFormatException ignored) {}

        try {
            Method method = BotSettings.class.getMethod("set" + key, double.class);
            method.invoke(s, Double.parseDouble(value));
            return true;
        } catch (ReflectiveOperationException | NumberFormatException ignored) {}

        return false;
    }
}
