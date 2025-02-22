package de.jeff_media.discordstepsisterverifier.listeners;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildReadyListener extends ListenerAdapter {

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        //System.out.println("Guild ready: " + event.getGuild().getIdLong());
        if(event.getGuild().getIdLong() == 730812691068747817L) {
            System.out.println("Sending messages");
            String message =  "Stepsister is starting...\n"
                    + "OS: " + System.getProperty("os.name") + "\n"
                    + "Available CPU: " +   Runtime.getRuntime().availableProcessors() + " cores\n"
                    + "Available RAM: " + Runtime.getRuntime().freeMemory()/1048576 + " MB \n"
                    + "\n"
                    + ":clown: Stepsister has started and is ready to get stuck somewhere.";

                try {
                    event.getGuild().getTextChannelsByName("bot-testing",true).get(0).sendMessage(message).queue();
                } catch (Throwable t) {
                    System.out.println("Could not send message to bot-testing channel: " + message);
                    t.printStackTrace();
                }
        }
    }
}
