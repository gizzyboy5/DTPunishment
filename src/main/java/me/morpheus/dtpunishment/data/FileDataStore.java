package me.morpheus.dtpunishment.data;

import me.morpheus.dtpunishment.DTPunishment;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

public class FileDataStore extends DataStore {

    private DTPunishment main;

    public FileDataStore(DTPunishment main) {
        this.main = main;
    }

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationNode node;

    private void save() {
        try {
            loader.save(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initPlayerConfig(UUID player){
        Path playerData = Paths.get(main.getConfigPath() + "/data/" + player + ".conf");
        loader = HoconConfigurationLoader.builder().setPath(playerData).build();
        try {
            node = loader.load();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        Path dataFolder = Paths.get(main.getConfigPath() + "/data");
        if (Files.exists(dataFolder)) {
            main.getLogger().info("Data folder found");
        } else {
            try {
                Files.createDirectories(dataFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getBanpoints(UUID player) {
        initPlayerConfig(player);
        return node.getNode("points", "banpoints").getInt();
    }

    @Override
    public int getMutepoints(UUID player) {
        initPlayerConfig(player);
        return node.getNode("points", "mutepoints").getInt();
    }

    @Override
    public boolean isMuted(UUID player) {
        initPlayerConfig(player);
        return node.getNode("mute", "isMuted").getBoolean();
    }

    @Override
    public Instant getExpiration(UUID player) {
        initPlayerConfig(player);
        String exp = node.getNode("mute", "until").getString();
        return Instant.parse(exp);
    }

    @Override
    public boolean hasReceivedBonus(UUID player) {
        initPlayerConfig(player);
        return node.getNode("points", "bonus_received").getBoolean();
    }

    @Override
    public void giveBonus(UUID player) {

    }

    @Override
    public void addBanpoints(UUID player, int amount) {
        initPlayerConfig(player);
        int actual = getBanpoints(player);
        node.getNode("points", "banpoints").setValue(actual + amount);
        save();
    }

    @Override
    public void removeBanpoints(UUID player, int amount) {
        initPlayerConfig(player);
        int actual = getBanpoints(player);
        node.getNode("points", "banpoints").setValue(actual - amount);
        save();
    }

    @Override
    public void addMutepoints(UUID player, int amount) {
        initPlayerConfig(player);
        int actual = getMutepoints(player);
        node.getNode("points", "mutepoints").setValue(actual + amount);
        save();
    }

    @Override
    public void removeMutepoints(UUID player, int amount) {
        initPlayerConfig(player);
        int actual = getMutepoints(player);
        node.getNode("points", "mutepoints").setValue(actual - amount);
        save();
    }

    @Override
    public void mute(UUID player, Instant expiration) {
        initPlayerConfig(player);
        node.getNode("mute", "isMuted").setValue(true);
        String exp = String.valueOf(expiration);
        node.getNode("mute", "until").setValue(exp);
        save();
    }

    @Override
    public void unmute(UUID player) {
        initPlayerConfig(player);
        node.getNode("mute", "isMuted").setValue(false);
        node.getNode("mute").removeChild("until");
        save();
    }

    @Override
    public void createUser(UUID player) {
        Path playerData = Paths.get(main.getConfigPath() + "/data/" + player + ".conf");
        try {
            Files.createFile(playerData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initPlayerConfig(player);
        node.getNode("points", "banpoints").setValue(0);
        node.getNode("points", "mutepoints").setValue(0);
        node.getNode("mute", "isMuted").setValue(false);
        save();
    }

    @Override
    public boolean userExists(UUID player) {
        Path playerData = Paths.get(main.getConfigPath() + "/data/" + player + ".conf");
        return Files.exists(playerData);
    }


}