package de.jeff_media.discordstepsisterverifier;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Config extends HashMap<String, Object> {

    private final Map<String,String> cache = new HashMap<>();

    public String getSQLURL() {
        return cache.computeIfAbsent("sql.url", __ -> String.valueOf(((Map<String,Object>) get("sql")).get("url")));
    }

    public String getSQLUser() {
        return cache.computeIfAbsent("sql.user", __ -> String.valueOf(((Map<String,Object>) get("sql")).get("user")));
    }

    public String getSQLPass() {
        return cache.computeIfAbsent("sql.pass", __ -> String.valueOf(((Map<String,Object>) get("sql")).get("pass")));
    }

    public String getSupporterRoleId() {
        return cache.computeIfAbsent("roles.supporter", __ -> String.valueOf(((Map<String,Object>) get("roles")).get("supporter")));
    }

    /*public String getPremiumSupportChannelId() {
        return String.valueOf(((Map<String,Object>) get("channels")).get("premium-support-channel"));
    }*/

    public String getCreateTicketsChannelId() {
        return cache.computeIfAbsent("channels.create-tickets-channel", __ -> String.valueOf(((Map<String,Object>) get("channels")).get("create-tickets-channel")));
    }

    public String getBuyerRoleId() {
        return cache.computeIfAbsent("roles.buyer", __ -> String.valueOf(((Map<String,Object>) get("roles")).get("buyer")));
    }

    public String getTicketsCategoryId() {
        return cache.computeIfAbsent("channels.verifypurchase-category", __ -> String.valueOf(((Map<String,Object>) get("channels")).get("verifypurchase-category")));
    }

    public String getVerificationLogsChannelId() {
        return cache.computeIfAbsent("channels.verification-logs-channel", __ -> String.valueOf(((Map<String,Object>) get("channels")).get("verification-logs-channel")));
    }

    public String getBotToken() {
        return cache.computeIfAbsent("bot-token", __ -> (String) get("bot-token"));
    }

    public String getAdminVerificationChannelId() {
        return cache.computeIfAbsent("channels.admin-verification-channel", __ -> String.valueOf(((Map<String,Object>) get("channels")).get("admin-verification-channel")));
    }

    public static Config fromMap(Map<String, Object> map) {
        Config config = new Config();
        config.putAll(map);
        return config;
    }

}
