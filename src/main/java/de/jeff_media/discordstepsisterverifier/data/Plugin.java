package de.jeff_media.discordstepsisterverifier.data;

import de.jeff_media.discordstepsisterverifier.Config;
import de.jeff_media.discordstepsisterverifier.DiscordStepsisterVerifier;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class Plugin {
    private final String displayName;
    private final String realName;
    private final List<String> roles;
    private final List<String> channels;

    private final Map<Shop, String> resourceIds;

    public String getResourceId(Shop shop) {
        return resourceIds.get(shop);
    }

    public String getShopLink(Shop shop) {
        return shop.getUrlScheme().replace("{id}",getResourceId(shop)).replace("{name}",getDisplayName());
    }

    public static Plugin get(String realName) {
        Config config = DiscordStepsisterVerifier.getInstance().getConfig();
        try {
            Map<String, Object> plugins = (Map<String, Object>) config.get("plugins");
            Map<String, Object> plugin = (Map<String, Object>) plugins.get(realName);

            String displayName = (String) plugin.getOrDefault("name",realName);
            List<Long> roles = new ArrayList<>((List<Long>) plugin.get("roles"));
            List<Long> channels = new ArrayList<>((List<Long>) plugin.get("channels"));
            roles.add(Long.parseLong(config.getBuyerRoleId()));
            //channels.add(Long.parseLong(config.getPremiumSupportChannelId()));
            roles.sort(Comparator.comparing(Object::toString));
            channels.sort(Comparator.comparing(Object::toString));


            for(Long role : roles) {
                System.out.println(" "+role);
            }
            for(Long channel : channels) {
                System.out.println(" " + channel);
            }

            Map<Shop,String> resourceIds = new HashMap<>();
            for(Shop shop : Shop.values()) {
                resourceIds.put(shop, String.valueOf((int) ((Map<String,Object>)plugin.get("shops")).getOrDefault(shop.getName(),-1)));
            }

            return new Plugin(displayName, realName, roles, channels, resourceIds);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getRoleMentions() {
        List<String> roles = new ArrayList<>(this.roles);
        for(int i = 0; i < roles.size(); i++) {
            roles.set(i, "<@&" + roles.get(i) + ">");
        }
        return roles;
    }

    public List<String> getChannelMentions() {
        List<String> channels = new ArrayList<>(this.channels);
        for(int i = 0; i < channels.size(); i++) {
            channels.set(i,String.format("<#" + channels.get(i)+">"));
        }
        return channels;
    }

    private Plugin(String displayName, String realName, List<Long> roles, List<Long> channels, Map<Shop,String> resourceIds) {
        this.displayName = displayName;
        this.realName = realName;
        List<String> rolesAsString = new ArrayList<>();
        List<String> channelsAsString = new ArrayList<>();
        for(long role : roles) {
            rolesAsString.add(String.valueOf(role));
        }
        for(long channel : channels) {
            channelsAsString.add(String.valueOf(channel));
        }
        this.roles = rolesAsString;
        this.channels = channelsAsString;
        this.resourceIds = resourceIds;
    }
}
