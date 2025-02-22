package de.jeff_media.discordstepsisterverifier.listeners;

import de.jeff_media.daddy.Stepsister;
import de.jeff_media.discordstepsisterverifier.data.Shop;
import de.jeff_media.discordstepsisterverifier.utils.DCUtils;
import de.jeff_media.discordstepsisterverifier.DiscordStepsisterVerifier;
import de.jeff_media.discordstepsisterverifier.utils.DataSource;
import de.jeff_media.discordstepsisterverifier.utils.FileUtils;
import de.jeff_media.discordstepsisterverifier.data.OpenPurchaseVerificationRequest;
import de.jeff_media.discordstepsisterverifier.data.Plugin;
import de.jeff_media.discordstepsisterverifier.utils.StepsisterUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.jeff_media.daddy.Stepsister.PREFIX;
import static de.jeff_media.daddy.Stepsister.SUFFIX;

public class PurchaseVerificationListener extends ListenerAdapter {

    private static final DiscordStepsisterVerifier main = DiscordStepsisterVerifier.getInstance();
    private static final Map<String, OpenPurchaseVerificationRequest> openRequests = new HashMap<>();
    private static final String SPIGOT_USER_URL_PREFIX = "https://www.spigotmc.org/members/";
    private static final Pattern pattern = Pattern.compile(".*https://www\\.spigotmc\\.org/members/.+\\.([0-9]+).*");
    private static final String ERROR_COULD_NOT_DOWNLOAD = "Could not download discord-verification.html file. Maybe Discord or my server currently has connection problems. Please copy/paste the code from inside the file manually into this chat.";
    private static final String ERROR_COULD_NOT_FIND_CODE_IN_FILE = "Could not find verification code inside discord-verification.html file.";


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if(message.getAuthor().isBot()) {
            return;
        }

        if (!event.getChannelType().isMessage()) return;

        if(event.getTextChannel().getParent()==null || !event.getTextChannel().getParent().getId().equals(main.getConfig().getTicketsCategoryId())) {
            return;
        }

        String rawMessage = message.getContentRaw().replace("\n","");

        if (message.getContentRaw().contains(SPIGOT_USER_URL_PREFIX)) {
            extractSpigotUserUrl(event);
        }

        if (!message.getAttachments().isEmpty()) {
            extractVerificationFile(event);
            return;
        } else if(rawMessage.contains(PREFIX) && rawMessage.contains(SUFFIX)) {
            String[] split = rawMessage.split(PREFIX);
            if(split.length>1) {
                String code = split[1].split(SUFFIX)[0];
                verifyCode(event, code.replace("\n", ""), false);
            }
        } else if(rawMessage.startsWith("Verification Code: ")) {
            verifyCode(event, rawMessage.replace("Verification Code: ","").replace("\n",""),false);
        }

        checkIfDone(event);

    }

    private OpenPurchaseVerificationRequest createOrGetOpenRequest(String userId) {
        if (openRequests.containsKey(userId)) {
            return openRequests.get(userId);
        } else {
            OpenPurchaseVerificationRequest request = new OpenPurchaseVerificationRequest();
            openRequests.put(userId, request);
            return request;
        }
    }

    private void extractVerificationFile(MessageReceivedEvent event) {
        Message message = event.getMessage();
        for (Message.Attachment attachment : message.getAttachments()) {
            if (attachment.isImage()) {
                continue;
            }
            if (attachment.isVideo()) {
                continue;
            }
            if (!StepsisterUtils.isPossibleVerificationFile(attachment)) {
                continue;
            }
            try {
                CompletableFuture<File> fileCompletableFuture = attachment.downloadToFile(File.createTempFile("stepsister", null));
                fileCompletableFuture.whenComplete((file, action) -> {
                    if (action != null) {
                        sendError(event, ERROR_COULD_NOT_DOWNLOAD);
                        action.printStackTrace();
                        file.delete();
                        return;
                    }
                    String verificationCode = StepsisterUtils.extractVerificationCode(file);
                    if (verificationCode != null && verificationCode.length()!=0) {
                        verifyCode(event, verificationCode,true);
                        checkIfDone(event);
                        file.delete();
                        return;
                    } else {
                        sendError(event, ERROR_COULD_NOT_FIND_CODE_IN_FILE);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                sendError(event, ERROR_COULD_NOT_DOWNLOAD);
            }
            break;
        }
    }

    private void verifyCode(MessageReceivedEvent event, String verificationCode, boolean file) {
        createOrGetOpenRequest(event.getAuthor().getId()).setVerificationCode(verificationCode);
        LinkedHashMap<String,String> map = StepsisterUtils.getEntries(verificationCode);
        if(map==null || map.size()==0) {
            sendError(event,"The given " +(file?"verification file":"verification code")+" is invalid!");
            return;
        }
        createOrGetOpenRequest(event.getAuthor().getId()).setVerificationEntries(map);
    }

    private void sendError(MessageReceivedEvent event, String errorMessage) {
        event.getTextChannel().sendMessageEmbeds(new EmbedBuilder()
                .setTitle("Error while verifying purchase :frowning:")
                .appendDescription("Could not verify your purchase: " + errorMessage)
                .appendDescription("\n\nA " + DCUtils.pingSupportRole() + " will assist you as soon as possible!")
                .setColor(Color.RED)
                .build()).queue();
    }

    private void extractSpigotUserUrl(MessageReceivedEvent event) {
        OpenPurchaseVerificationRequest request = createOrGetOpenRequest(event.getAuthor().getId());
        Matcher matcher = pattern.matcher(event.getMessage().getContentRaw());
        if (matcher.matches()) {
            try {
                request.setSpigotId(Integer.parseInt(matcher.group(1)));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private void checkIfDone(MessageReceivedEvent event) {
        OpenPurchaseVerificationRequest request = createOrGetOpenRequest(event.getAuthor().getId());
        if (request.getVerificationCode() != null && request.getSpigotId() == null) {
            event.getTextChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("50% done | Please send your SpigotMC account link")
                    .setColor(Color.CYAN)
                    .setDescription("Thanks for uploading your discord-verification.html file!\n\n**Please also send a link to your SpigotMC profile.**")
                    .build()).queue();
            event.getTextChannel().sendMessage("valid file");
        } else if(request.getVerificationCode() == null && request.getSpigotId() != null) {
            event.getTextChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("50% done | Please send your discord-verification.html file")
                    .setColor(Color.CYAN)
                    .setDescription("Thanks for sending your Spigot account link!\n\n**Please also send your `discord-verification.html` file** that is found inside the plugin's folder. You can simply drag and dropt the file into this channel.")
                    .build()).queue();
        } else if(request.getVerificationCode()!= null && request.getSpigotId() != null) {
            String alreadyVerifiedDiscordUserId = null;
            if(request.matches()) {
                alreadyVerifiedDiscordUserId = DataSource.alreadyVerified(request.getVerificationEntries().get("RESOURCE"),
                        request.getVerificationEntries().get("UID"),
                        Shop.getFromMap(request.getVerificationEntries()).getName());
            //DataSource.alreadyVerified(request.getVerificationEntries().get("RESOURCE"),request.getVerificationEntries().get("UID"));
                if(alreadyVerifiedDiscordUserId!=null) {
                    sendAlreadyVerifiedEmbed(event, request);
                } else {
                    sendVerificationEmbed(event, request);
                    //main.addPurchase(request.getVerificationEntries().get("RESOURCE"), request.getVerificationEntries().get("UID"), event.getAuthor().getId());
                    String pluginName = request.getVerificationEntries().get("Plugin");
                    String resourceId = request.getVerificationEntries().get("RESOURCE");
                    String userId = request.getVerificationEntries().get("UID");
                    DataSource.saveVerification(pluginName, resourceId, userId, event.getAuthor().getId(),Shop.getFromMap(request.getVerificationEntries()).getName());
                }
            } else {
                sendError(event,"Your verification code is invalid, or does not belong to the SpigotMC account link you sent.");
            }
            openRequests.remove(event.getAuthor().getId());
            sendVerificationLogEmbed(event,request, alreadyVerifiedDiscordUserId);
        }

    }

    private void sendAlreadyVerifiedEmbed(MessageReceivedEvent event, OpenPurchaseVerificationRequest request) {
        Plugin plugin = Plugin.get(request.getVerificationEntries().get("Plugin").split(" ")[0]);
        sendError(event,"This verification code for **"+plugin.getDisplayName()+"** has **already been used**.");
    }

    private void sendVerificationEmbed(MessageReceivedEvent event, OpenPurchaseVerificationRequest request) {
        Plugin plugin = Plugin.get(request.getVerificationEntries().get("Plugin").split(" ")[0]);
        EmbedBuilder builder = new EmbedBuilder().setTitle(String.format("Purchase of __**%s**__ on %s verified!", plugin.getDisplayName(),Shop.getFromMap(request.getVerificationEntries()).getDisplayName()));
        builder.setColor(Color.GREEN);
        builder.setDescription(String.format("Hello %s, thank you very much for buying **%s** :slight_smile:!\n",event.getAuthor().getAsMention(),plugin.getDisplayName()));
        builder.appendDescription("Your purchase has been verified automatically.\n\n");

        builder.appendDescription("You have been given the following role(s):\n");
        builder.appendDescription(String.format("%s\n\n",plugin.getRoleMentions().stream().collect(Collectors.joining(" "))));

        builder.appendDescription("You also have access to the following channel(s) now:\n");
        builder.appendDescription(String.format("%s\n\n",plugin.getChannelMentions().stream().collect(Collectors.joining(" "))));

        builder.appendDescription(String.format("Thank you very much for supporting my work. If you need support, just use one of the mentioned channels, or head back to %S and open a new ticket for individual support!\n\n", DCUtils.pingTicketsChannel()));
        builder.appendDescription("You can close this ticket by typing `-close` or clicking on the button at the top.");

        for(String role : plugin.getRoles()) {
            try {
                event.getGuild().addRoleToMember(event.getAuthor().getIdLong(), event.getGuild().getRoleById(role)).queue();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        event.getTextChannel().sendMessageEmbeds(builder.build()).queue(msg -> {
            msg.addReaction(":x:");
        });
    }

    private void sendVerificationLogEmbed(MessageReceivedEvent event, OpenPurchaseVerificationRequest request, String alreadyVerifiedUserId) {
        Color color = Color.GREEN;
        String title = "Success";

        if(!request.matches()) {
            title = "Denied";
            color = Color.RED;
        }
        if(alreadyVerifiedUserId != null) {
            title = "Already verified";
            color = Color.ORANGE;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(color)
                .setTitle(event.getAuthor().getAsTag() + ": " + title)
                .setTimestamp(Instant.now());
        if(alreadyVerifiedUserId!=null) {
            builder.addField("Already verified Discord User", "<@" + alreadyVerifiedUserId + ">", false);
        }
        builder.addField("Discord User",event.getAuthor().getAsTag() + event.getAuthor().getAsMention(), false);
        builder.addField("Channel",event.getTextChannel().getAsMention() + " [Msg]("+event.getMessage().getJumpUrl()+")",true);
        builder.addField("Given UID","["+request.getSpigotId()+"]("+SPIGOT_USER_URL_PREFIX+request.getSpigotId()+")",false);
        if(request.getVerificationEntries().containsKey("UID")) {
            String UID = request.getVerificationEntries().get("UID");
            builder.addField("Real buyer's UID", "["+UID+"]("+SPIGOT_USER_URL_PREFIX+UID+")", true);
        }
        if(request.getVerificationEntries().containsKey("Plugin")) {
            String plugin = request.getVerificationEntries().get("Plugin");
            builder.addField("Plugin", plugin, true);
        }
        builder.addField("Shop", Shop.getFromMap(request.getVerificationEntries()).getDisplayName(),true);

        for(Map.Entry<String,String> entry : request.getVerificationEntries().entrySet()) {
            if(entry.getKey().equals("UID")
                    ||entry.getKey().equals("Plugin")
                    || entry.getKey().equals("MOTD")
                    || entry.getKey().equals("Shop")) {
                continue;
            } else {
                builder.addField(entry.getKey(), entry.getValue(), true);
            }
        }
        if(request.getVerificationEntries().containsKey("MOTD")) {
            String motd = request.getVerificationEntries().get("MOTD");
            builder.addField("MOTD", motd, false);
        }
        event.getGuild().getTextChannelById(main.getConfig().getVerificationLogsChannelId()).sendMessageEmbeds(builder.build()).queue();
    }




}
