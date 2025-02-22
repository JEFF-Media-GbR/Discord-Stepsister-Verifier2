package de.jeff_media.discordstepsisterverifier.data;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

public enum Shop {

    SPIGOT("spigot","https://www.spigotmc.org/resources/{name}.{id}/","SpigotMC"),
    POLYMART("polymart","https://polymart.org/resource/{name}.{id}","Polymart");

    @Getter private final String urlScheme;
    @Getter private final String name;
    @Getter private final String displayName;

    Shop(String name, String urlScheme, String displayName) {
        this.name = name;
        this.urlScheme = urlScheme;
        this.displayName = displayName;
    }

    public static Shop getFromMap(@NotNull Map<String,String> map) {
        return getFromName(map.getOrDefault("Shop","spigot"));
    }

    public static Shop getFromName(@Nullable String name) {
        if(name == null) return SPIGOT;
        return Arrays.stream(values())
                .filter(shop -> shop.name.equals(name))
                .findAny()
                .orElse(null);
    }
}
