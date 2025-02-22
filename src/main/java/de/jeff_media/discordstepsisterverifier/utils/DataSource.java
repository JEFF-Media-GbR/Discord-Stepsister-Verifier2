package de.jeff_media.discordstepsisterverifier.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.jeff_media.discordstepsisterverifier.DiscordStepsisterVerifier;
import de.jeff_media.discordstepsisterverifier.data.Plugin;
import de.jeff_media.discordstepsisterverifier.data.Shop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DataSource {

    private static final DiscordStepsisterVerifier main = DiscordStepsisterVerifier.getInstance();

    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource dataSource;

    static {

        config.setJdbcUrl( main.getConfig().getSQLURL());
        config.setUsername( main.getConfig().getSQLUser() );
        config.setPassword( main.getConfig().getSQLPass() );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        dataSource = new HikariDataSource( config );
    }

    public static void createTables() {
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS purchase_verifications (shop varchar(128), shop_user varchar(128), plugin_name varchar(128), shop_resource varchar(128), discord_user varchar(128))")) {
            int changed = ps.executeUpdate();
            if(changed == 0) {
                System.out.println("Connected.");
            } else {
                System.out.println("Connected and create tables.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveVerification(String pluginName, String resourceId, String shopUserId, String discordUserId, String shopName) {
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO purchase_verifications (shop, shop_user, plugin_name, shop_resource, discord_user) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, shopName);
            ps.setString(2, shopUserId);
            ps.setString(3, pluginName);
            ps.setString(4, resourceId);
            ps.setString(5, discordUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    Returns the DiscordID of the already verified user
     */
    public static String alreadyVerified(String resourceId, String shopUserId, String shopId) {
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT discord_user FROM purchase_verifications WHERE shop = ? AND shop_user = ? AND shop_resource = ?")) {
            ps.setString(1,shopId);
            ps.setString(2,shopUserId);
            ps.setString(3,resourceId);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getString("discord_user");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DataSource() {}

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
