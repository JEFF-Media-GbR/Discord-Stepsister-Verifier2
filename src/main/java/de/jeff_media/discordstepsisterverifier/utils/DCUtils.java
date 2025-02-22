package de.jeff_media.discordstepsisterverifier.utils;

import de.jeff_media.discordstepsisterverifier.DiscordStepsisterVerifier;

public class DCUtils {
    public static String pingSupportRole() {
        return "<@&" + DiscordStepsisterVerifier.getInstance().getConfig().getSupporterRoleId()+">";
    }

    public static String pingTicketsChannel() {
        return "<#" + DiscordStepsisterVerifier.getInstance().getConfig().getCreateTicketsChannelId()+">";
    }
}
