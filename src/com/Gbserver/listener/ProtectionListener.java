package com.Gbserver.listener;

import com.Gbserver.Utilities;
import com.Gbserver.commands.TF;
import com.Gbserver.variables.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
//Much more robust one needed!
public class ProtectionListener implements Listener {
    public static boolean isDisabled = false;
    final int[][][] DATA = {
            {
                    {-156, 78, 228},
                    {-130, 68, 208},
            },
            {
                    {72, 65, 365},
                    {-26, 254, 277},
            },
            {
                    {165, 101, 433},
                    {124, 145, 392},
            },
            {
                    {-162, 71, 185},
                    {-163, 75, 175}
            }
    };

    final String[][] TRUSTED_PLAYERS = {
            {"GoBroadwell"},
            {"_Broadwell", "Yin_of_the_Yang", "Latios_"},
            {""},
            {"_Broadwell"}
    };

    final int RIGHT_UP = 0;
    final int LEFT_DOWN = 1;

    final int AXIS_X = 0;
    final int AXIS_Y = 1;
    final int AXIS_Z = 2;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent bbe) {
        int x = bbe.getBlock().getX();
        int y = bbe.getBlock().getY();
        int z = bbe.getBlock().getZ();

        //bbe.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "This area is protected. Sorry!");
        //bbe.setCancelled(true);
        for (int i = 0; i < DATA.length; i++) {
            if (isInSelection(i, x, y, z) && !isTrusted(i, bbe.getPlayer())) {
                if (i == 0 && bbe.getBlock().getType() == Material.SNOW_BLOCK) {

                } else {
                    bbe.getPlayer().sendMessage(ChatWriter.getMessage(ChatWriterType.CONDITION, ChatColor.RED + "" + ChatColor.BOLD + "This area is protected. Sorry!"));
                    bbe.setCancelled(true);
                }
            }
            if (bbe.getBlock().getWorld().equals(Bukkit.getServer().getWorld("Bomb_Lobbers1")) && bbe.getBlock().getType() == Material.BEDROCK) {
                bbe.getPlayer().sendMessage(ChatWriter.getMessage(ChatWriterType.CONDITION, ChatColor.RED + "" + ChatColor.BOLD + "This area is protected. Sorry!"));
                bbe.setCancelled(true);
            }
        }

        if (!bbe.getBlock().getWorld().getName().equals("world") &&
                !bbe.getPlayer().getName().equals(Utilities.OWNER)) {
            //Only exception: During turf, building.
            bbe.setCancelled(!(bbe.getBlock().getWorld().getName().equals("Turf_Wars1") && TF.isBuildtime) && bbe.getBlock().getType() == Material.STAINED_CLAY);
        }
            try {
                if (EnhancedPlayer.getEnhanced(bbe.getPlayer()) == null) {
                    if (!bbe.getBlock().getType().equals(Material.SNOW_BLOCK)) {
                        bbe.setCancelled(true);
                        bbe.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to break blocks. Required permission above GUEST");
                        return;
                    }
                }
                if (EnhancedPlayer.getEnhanced(bbe.getPlayer()).getPermission() == null) {
                    if (!bbe.getBlock().getType().equals(Material.SNOW_BLOCK)) {
                        bbe.setCancelled(true);
                        bbe.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to break blocks. Required permission above GUEST");
                    }
                } else {
                    if (EnhancedPlayer.getEnhanced(bbe.getPlayer()).getPermission().getLevel() <
                            PermissionManager.Permissions.PRIVILEGED.getLevel()) {
                        if (!bbe.getBlock().getType().equals(Material.SNOW_BLOCK)) {
                            bbe.setCancelled(true);
                            bbe.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to break blocks. Required permission above GUEST");
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        for(Territory t : Territory.activeTerritories){
            if(t.isInside(bbe.getBlock().getLocation())
                    && !bbe.getPlayer().getUniqueId().equals(t.getOwner())
                    && !t.hasCollaborator(bbe.getPlayer().getUniqueId())){
                bbe.setCancelled(true);
                bbe.getPlayer().sendMessage("You may not break blocks here. Property belongs to " + ChatColor.YELLOW +
                        Bukkit.getOfflinePlayer(t.getOwner()).getName() + ChatColor.RESET +
                        ". If you wish to obtain permission, ask the owner to include you as a collaborator.");
                break;
            }
        }

    }

    private boolean isInRangeOf(int testant, int min, int max) {
        if (min < max) {
            if (testant >= min && testant <= max) {
                return true;
            } else {
                return false;
            }
        } else if (min != max) {
            if (testant <= min && testant >= max) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isInSelection(int selectionNumber, int x, int y, int z) {

        if (isInRangeOf(x, DATA[selectionNumber][RIGHT_UP][AXIS_X], DATA[selectionNumber][LEFT_DOWN][AXIS_X])
                && isInRangeOf(y, DATA[selectionNumber][RIGHT_UP][AXIS_Y], DATA[selectionNumber][LEFT_DOWN][AXIS_Y])
                && isInRangeOf(z, DATA[selectionNumber][RIGHT_UP][AXIS_Z], DATA[selectionNumber][LEFT_DOWN][AXIS_Z])
            //&& !isInRangeOf(x,DATA[selectionNumber][EXCLUDE_RIGHT_UP][AXIS_X],DATA[selectionNumber][EXCLUDE_LEFT_DOWN][AXIS_X])
            //&& !isInRangeOf(y,DATA[selectionNumber][EXCLUDE_RIGHT_UP][AXIS_Y],DATA[selectionNumber][EXCLUDE_LEFT_DOWN][AXIS_Y])
            //&& !isInRangeOf(z,DATA[selectionNumber][EXCLUDE_RIGHT_UP][AXIS_Z],DATA[selectionNumber][EXCLUDE_LEFT_DOWN][AXIS_Z])
                ) {

            return true;
        } else {
            return false;
        }
    }

    private boolean isTrusted(int selectionNumber, Player contestant) {
        String name = contestant.getName();
        if (Arrays.asList(TRUSTED_PLAYERS[selectionNumber]).contains(name)) {
            return true;
        } else {
            return false;
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent bbe) {
        int x = bbe.getBlock().getX();
        int y = bbe.getBlock().getY();
        int z = bbe.getBlock().getZ();

        //bbe.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "This area is protected. Sorry!");
        //bbe.setCancelled(true);
        for (int i = 0; i < DATA.length; i++) {
            if (isInSelection(i, x, y, z) && !isTrusted(i, bbe.getPlayer())) {
                if (i == 0 && bbe.getBlock().getType() == Material.SNOW_BLOCK) {

                } else {
                    bbe.getPlayer().sendMessage(ChatWriter.getMessage(ChatWriterType.CONDITION, ChatColor.RED + "" + ChatColor.BOLD + "This area is protected. Sorry!"));
                    bbe.setCancelled(true);
                }
            }
        }
        if ((!bbe.getBlock().getWorld().getName().equals("world") &&
                !bbe.getPlayer().getName().equals(Utilities.OWNER)) || ((bbe.getBlock().getType() == Material.TNT ||
                bbe.getBlock().getType() == Material.STATIONARY_LAVA ||
                bbe.getBlock().getType() == Material.LAVA ||
                bbe.getBlock().getType() == Material.LAVA_BUCKET) &&
                bbe.getBlock().getWorld().getName().equals("world"))) {
            bbe.setCancelled(!(bbe.getBlock().getWorld().getName().equals("Turf_Wars1") && TF.isBuildtime) && bbe.getBlock().getType() == Material.STAINED_CLAY);
        }
        if(!isDisabled) {
            try {
                if (EnhancedPlayer.getEnhanced(bbe.getPlayer()) == null) {
                    if (!bbe.getBlock().getType().equals(Material.SNOW_BLOCK)) {
                        bbe.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to place blocks. Required permission above GUEST");
                        bbe.setCancelled(true);
                        return;
                    }
                }
                if (EnhancedPlayer.getEnhanced(bbe.getPlayer()).getPermission() == null) {
                    if (!bbe.getBlock().getType().equals(Material.SNOW_BLOCK)) {
                        bbe.setCancelled(true);
                        bbe.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to place blocks. Required permission above GUEST");
                    }
                } else {
                    if (EnhancedPlayer.getEnhanced(bbe.getPlayer()).getPermission().getLevel() <
                            PermissionManager.Permissions.PRIVILEGED.getLevel()) {
                        if (!bbe.getBlock().getType().equals(Material.SNOW_BLOCK)) {
                            bbe.setCancelled(true);
                            bbe.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to place blocks. Required permission above GUEST");
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        for(Territory t : Territory.activeTerritories){
            if(t.isInside(bbe.getBlock().getLocation())
                    && !bbe.getPlayer().getUniqueId().equals(t.getOwner())
                    && !t.hasCollaborator(bbe.getPlayer().getUniqueId())){
                bbe.setCancelled(true);
                bbe.getPlayer().sendMessage("You may not place blocks here. Property belongs to " + ChatColor.YELLOW +
                        Bukkit.getOfflinePlayer(t.getOwner()).getName() + ChatColor.RESET +
                        ". If you wish to obtain permission, ask the owner to include you as a collaborator.");
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent pbe) {
        int x = pbe.getBlockClicked().getX();
        int y = pbe.getBlockClicked().getY() + 1;
        int z = pbe.getBlockClicked().getZ();

        //pbe.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "This area is protected. Sorry!");
        //pbe.setCancelled(true);
        for (int i = 0; i < DATA.length; i++) {
            if (isInSelection(i, x, y, z) && !isTrusted(i, pbe.getPlayer())) {
                if (i != 0) {
                    pbe.getPlayer().sendMessage(ChatWriter.getMessage(ChatWriterType.CONDITION, ChatColor.RED + "" + ChatColor.BOLD + "This area is protected. Sorry!"));
                    pbe.setCancelled(true);
                }
            }
        }


        try {

            if (EnhancedPlayer.getEnhanced(pbe.getPlayer()).getPermission() == null) {
                pbe.setCancelled(true);
                pbe.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to place liquid. Required permission above GUEST");
            } else {
                if (EnhancedPlayer.getEnhanced(pbe.getPlayer()).getPermission().getLevel() <
                        PermissionManager.Permissions.PRIVILEGED.getLevel()) {

                    pbe.setCancelled(true);
                    pbe.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to place liquid. Required permission above GUEST");

                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for(Territory t : Territory.activeTerritories){
            if(t.isInside(pbe.getBlockClicked().getLocation())
                    && !pbe.getPlayer().getUniqueId().equals(t.getOwner())
                    && !t.hasCollaborator(pbe.getPlayer().getUniqueId())){
                pbe.setCancelled(true);
                pbe.getPlayer().sendMessage("You may not place liquids here. Property belongs to " + ChatColor.YELLOW +
                        Bukkit.getOfflinePlayer(t.getOwner()).getName() + ChatColor.RESET +
                        ". If you wish to obtain permission, ask the owner to include you as a collaborator.");
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent pie){
        if(pie.getPlayer().getItemInHand().getType() == Material.ARMOR_STAND ||
                pie.getPlayer().getItemInHand().getType() == Material.MOB_SPAWNER){

                if(EnhancedPlayer.isPlayerIneligible(pie.getPlayer(), PermissionManager.Permissions.PRIVILEGED)){
                    pie.setCancelled(true);
                    pie.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to spawn creatures. Permission not above GUEST");
                }

            for(Territory t : Territory.activeTerritories){
                if(t.isInside(pie.getClickedBlock().getLocation())
                        && !pie.getPlayer().getUniqueId().equals(t.getOwner())
                        && !t.hasCollaborator(pie.getPlayer().getUniqueId())){
                    pie.setCancelled(true);
                    pie.getPlayer().sendMessage("You may not spawn entities here. Property belongs to " + ChatColor.YELLOW +
                            Bukkit.getOfflinePlayer(t.getOwner()).getName() + ChatColor.RESET +
                            ". If you wish to obtain permission, ask the owner to include you as a collaborator.");
                    break;
                }
            }
        }


    }
}
