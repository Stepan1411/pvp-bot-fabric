package org.stepan1411.pvp_bot.network;

import org.stepan1411.pvp_bot.bot.BotSettings;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class BotSettingsReader {
    public static Map<String, String> readAll() {
        BotSettings s = BotSettings.get();
        Map<String, String> result = new TreeMap<>();

        for (Method method : BotSettings.class.getDeclaredMethods()) {
            String name = method.getName();
            if (method.getParameterCount() != 0) continue;

            String key = null;
            if (name.startsWith("is") && name.length() > 2) {
                key = name.substring(2);
            } else if (name.startsWith("get") && name.length() > 3) {
                key = name.substring(3);
            }
            if (key == null) continue;

            if (key.equals("")) continue;

            try {
                Object value = method.invoke(s);
                result.put(key, String.valueOf(value));
            } catch (Exception ignored) {
            }
        }

        return result;
    }
}
