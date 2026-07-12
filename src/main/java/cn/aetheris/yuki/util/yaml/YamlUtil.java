package cn.aetheris.yuki.util.yaml;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.config.MemoryConfiguration;
import cn.aetheris.yuki.config.file.YamlConfiguration;
import cn.aetheris.yuki.config.file.YamlConfigurationOptions;
import cn.aetheris.yuki.protocol.nms.ReflectionUtils;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class YamlUtil {

    
    @SneakyThrows
    public static void updateConfig(File configFile, String jarConfigFileName) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        String currentVersion = PluginLoader.INSTANCE.getVersion();
        if (currentVersion.equals(config.getString("config-version"))) {
            return;
        }

        URL url = Yuki.class.getClassLoader().getResource(jarConfigFileName);
        if (url == null) return;

        try (InputStream in = url.openStream()) {
            YamlConfiguration jarConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8)
            );

            Set<String> jarConfigKeys = new HashSet<>(jarConfig.getKeys(true));
            Set<String> userConfigKeys = new HashSet<>(config.getKeys(true));

            jarConfigKeys.removeAll(userConfigKeys);

            Iterator<String> keyIterator = jarConfigKeys.iterator();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                int lastDot = key.lastIndexOf('.');

                if (lastDot != -1) {
                    String parentKey = key.substring(0, lastDot);
                    if (!userConfigKeys.contains(parentKey)) {
                        keyIterator.remove();
                    }
                }
            }

            forKey:
            for (String key : jarConfigKeys) {
                String[] keyParts = key.split("\\.");
                StringBuilder prefixBuilder = new StringBuilder();

                for (int i = 0; i < keyParts.length; i++) {
                    String part = keyParts[i];

                    if (i > 0) {
                        prefixBuilder.append('.');
                    }
                    prefixBuilder.append(part);

                    List<String> prefixComments = jarConfig.getComments(prefixBuilder.toString());
                    if (prefixComments.contains("!no-update")) {
                        continue forKey;
                    }
                }

                config.set(key, jarConfig.get(key));

                List<String> comments = jarConfig.getComments(key);
                if (!comments.isEmpty()) {
                    config.setComments(key, comments);
                }
            }

            config.set("config-version", currentVersion);

            YamlConfigurationOptions newOptions = config.options();
            newOptions.width(Integer.MAX_VALUE);
            ReflectionUtils.setFieldValue(
                    ReflectionUtils.getField(MemoryConfiguration.class, "options", true),
                    jarConfig,
                    newOptions
            );

            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}