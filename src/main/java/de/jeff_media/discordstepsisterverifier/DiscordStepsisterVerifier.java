package de.jeff_media.discordstepsisterverifier;

import de.jeff_media.discordstepsisterverifier.listeners.AdminVefification;
import de.jeff_media.discordstepsisterverifier.listeners.GuildReadyListener;
import de.jeff_media.discordstepsisterverifier.listeners.PurchaseVerificationListener;
import de.jeff_media.discordstepsisterverifier.utils.DataSource;
import de.jeff_media.discordstepsisterverifier.utils.MigrationUtils;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.yaml.snakeyaml.Yaml;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordStepsisterVerifier {

    @Getter private final Config config;
    @Getter private static DiscordStepsisterVerifier instance;
    //@Getter private Map<String,Object> finishedVerifications;
    //private static final File finishedVerificationsFile = new File(System.getProperty("user.home"),"verified.yml");

    {
        instance = this;
        final InputStream inputStream = instance.getClass().getClassLoader().getResourceAsStream("config.yml");
        config = Config.fromMap(new Yaml().load(inputStream));

        DataSource.createTables();
        MigrationUtils.migrateYamlToSql();
    }

    public void init() throws LoginException {
        JDA jda = JDABuilder.createDefault(config.getBotToken()).build();
        jda.addEventListener(new PurchaseVerificationListener());
        jda.addEventListener(new GuildReadyListener());
        jda.addEventListener(new AdminVefification());
    }

    public static void main(String[] args) {
        try {
            new DiscordStepsisterVerifier().init();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

}
