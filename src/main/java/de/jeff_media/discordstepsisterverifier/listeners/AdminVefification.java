package de.jeff_media.discordstepsisterverifier.listeners;

import de.jeff_media.discordstepsisterverifier.Config;
import de.jeff_media.discordstepsisterverifier.DiscordStepsisterVerifier;
import de.jeff_media.discordstepsisterverifier.utils.StepsisterUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminVefification extends ListenerAdapter {

    private static final DiscordStepsisterVerifier main = DiscordStepsisterVerifier.getInstance();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {


        Message message = event.getMessage();

        if(message.getAuthor().isBot()) return;

        if(!event.getChannelType().isMessage()) return;

        if(!event.getTextChannel().getId().equals(main.getConfig().getAdminVerificationChannelId())) {
            return;
        }

        TextChannel channel = event.getTextChannel();


        StepsisterUtils.VerificationResult result = StepsisterUtils.getVerificationResult(event);
        if(result == StepsisterUtils.VerificationResult.NO_VERIFICATION_CODE) {
            channel.sendMessage("No verification code found").queue();
            return;
        }
        if(result == StepsisterUtils.VerificationResult.INVALID_VERIFICATION_CODE) {
            channel.sendMessage("Invalid verification code.").queue();
            return;
        }
        if(result == StepsisterUtils.VerificationResult.COULD_NOT_DOWNLOAD) {
            channel.sendMessage("Could not download file.").queue();
            return;
        }
        LinkedHashMap<String,String> map = result.getVerificationEntries();
        String toSend = map.entrySet().stream().map(entry -> "**" + entry.getKey() + "**\n" + entry.getValue() + "\n\n").collect(Collectors.joining());
        channel.sendMessage(toSend).queue();
    }
}
