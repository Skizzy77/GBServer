package com.Gbserver.listener;

import com.Gbserver.variables.ConfigLoader;
import com.Gbserver.variables.ConfigManager;
import com.Gbserver.variables.Identity;
import com.Gbserver.variables.SwiftDumpOptions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class BlockData implements Listener{
    public static File file = ConfigManager.getPathInsidePluginFolder("blockData.dat").toFile();
    public static HashMap<String, List<HashMap<String, String>>> data = new HashMap<>();
    public static Yaml helper = SwiftDumpOptions.BLOCK_STYLE();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent bpe){
        if(ConfigManager.smartGet("Preferences").get("BlockChangeLogging").equalsIgnoreCase("true") || ConfigManager.smartGet("Newbies").keySet().contains(
                Identity.serializeIdentity(bpe.getPlayer()))) {
            HashMap<String, String> properties = new HashMap<>();
            properties.put("Initiator", Identity.serializeIdentity(bpe.getPlayer()));
            properties.put("Timestamp", new Date().toString());
            properties.put("BlockType", bpe.getPlayer().getItemInHand().getType().toString());
            properties.put("Action", "Place");
            smartGet(bpe.getBlock().getLocation().toString()).add(properties);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent bbe){
        if(ConfigManager.smartGet("Preferences").get("BlockChangeLogging").equalsIgnoreCase("true") || ConfigManager.smartGet("Newbies").keySet().contains(
                Identity.serializeIdentity(bbe.getPlayer()))) {
            HashMap<String, String> properties = new HashMap<>();
            properties.put("Initiator", Identity.serializeIdentity(bbe.getPlayer()));
            properties.put("Timestamp", new Date().toString());
            properties.put("BlockType", bbe.getBlock().getType().toString());
            properties.put("Action", "Break");
            smartGet(bbe.getBlock().getLocation().toString()).add(properties);
        }
    }

    public static final ConfigLoader.ConfigUser configUser = new ConfigLoader.ConfigUser() {
        public void unload() throws IOException {
            FileWriter fw = new FileWriter(file);
            helper.dump(data, fw);
            fw.flush();
            fw.close();

        }

        public void load() throws IOException {
            FileReader fr = new FileReader(file);
            Object read = helper.load(fr);
            fr.close();
            if (read instanceof HashMap)
                data = (HashMap<String, List<HashMap<String, String>>>) read;
        }
    };


    private static List<HashMap<String, String>> smartGet(String loc){
        List<HashMap<String, String>> mnp = data.get(loc);
        if(mnp == null){
            data.put(loc, new LinkedList<HashMap<String, String>>());
            return data.get(loc);
        }else return mnp;
    }
}
