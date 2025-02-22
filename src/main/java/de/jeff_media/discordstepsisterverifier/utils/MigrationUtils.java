package de.jeff_media.discordstepsisterverifier.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

public class MigrationUtils {

    public static void migrateYamlToSql() {
        File file = new File(System.getProperty("user.home"),"verified.yml");
        if(!file.exists()) return;
        System.out.println("Migrating old purchases...");
        try {
            int migrated = 0;
            Map<String, Object> finishedVerifications = (Map<String, Object>) new Yaml().load(new FileReader(file));
            for(String resourceId : finishedVerifications.keySet()) {
                String pluginName = getPluginNameFromSpigotResourceId(resourceId);
                System.out.println("Migrating Resource " + pluginName + "("+resourceId+")");
                List<String> purchases = (List<String>) finishedVerifications.get(resourceId);
                for(String purchase : purchases) {
                    String spigotId = purchase.split(",")[0];
                    String discordId = purchase.split(",")[1];
                    System.out.println("  Spigot " + spigotId + ", Discord " + discordId);
                    DataSource.saveVerification(pluginName, resourceId, spigotId, discordId, "spigot");
                    migrated++;
                }
            }
            System.out.println("Migration complete, added " + migrated + " purchase verifications.");
            file.delete();
            System.out.println("Deleted legacy verifications.yml");
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }
    }

    private static String getPluginNameFromSpigotResourceId(String resourceId) {
        switch (resourceId) {
            case "92668": return "RePlant";
            case "96037": return "FilteredHoppers";
            case "87750": return "JukeBoxPlus";
            case "88214": return "AngelChest";
            case "87784": return "Drop2InventoryPlus";
            case "89807": return "AutoShulker";
            case "95013": return "AutoComposter";
            default: throw new IllegalArgumentException("Unknown Spigot Resource ID: " + resourceId);
        }
    }
}
