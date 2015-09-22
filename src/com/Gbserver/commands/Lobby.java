package com.Gbserver.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.Gbserver.Main;
import com.Gbserver.Utilities;
import com.Gbserver.variables.ChatWriter;
import com.Gbserver.variables.ChatWriterType;
import com.Gbserver.variables.GameType;
import com.Gbserver.variables.GameType.SwapPacket;
import com.Gbserver.variables.HelpTable;
import com.Gbserver.variables.LT;
import com.Gbserver.variables.ScoreDisplay;

public class Lobby implements CommandExecutor {
	private int beginTaskID;
	private int countdown;

	private HelpTable ht = new HelpTable("/lobby [bl/tf/swap] [leave/accept/deny]", "To quit a lobby or accept/deny a swap. To join, click on a sheep.", "", "lobby");

	public boolean onCommand(CommandSender sender, Command comm, String label, String[] args) {
		if (label.equalsIgnoreCase("lobby") && Utilities.validateSender(sender)) {
			if (args.length < 2) {
				ht.show(sender);
				return true;
			}
			Player p = (Player) sender;
			switch (args[0]) {
			case "bl":
				switch (args[1]) {
				case "leave":
					// Leave BL.
					GameType.BL.leave(p);
					break;
				case "start":
					GameType.BL.start(20);
				default:
					sender.sendMessage(ChatWriter.getMessage(ChatWriterType.COMMAND, "Invalid Options."));
					ht.show(sender);
					return true;
				}
				break;
			case "tf":
				switch (args[1]) {
				case "leave":
					// Leave TF.
					GameType.TF.leave(p);
					break;
				case "start":
					GameType.TF.start(15);
					break;
				default:
					sender.sendMessage(ChatWriter.getMessage(ChatWriterType.COMMAND, "Invalid Options."));
					ht.show(sender);
					return true;
				}
				break;
			case "swap":
				switch (args[1]) {
				case "accept":
					for(SwapPacket sp : GameType.packets){
						if(sp.target == p){
							//sp is the packet to solve.
							if(sp.type == LT.BL){
								GameType.BL.rawJoin(sp.targetColor, sp.origin);
								GameType.BL.rawJoin(GameType.opposite(sp.targetColor), sp.target);
							}else{
								GameType.TF.rawJoin(sp.targetColor, sp.origin);
								GameType.TF.rawJoin(GameType.opposite(sp.targetColor), sp.target);
								
							}
							try {
								sp.close();
							} catch (Throwable e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
					}
					ChatWriter.writeTo(p, ChatWriterType.GAME, "You do not have any swap requests pending.");
					break;
				case "deny":
					for(SwapPacket sp : GameType.packets){
						if(sp.target == p){
							//sp is the packet to solve.
							ChatWriter.writeTo(p, ChatWriterType.GAME, "You have denied the swap request.");
							ChatWriter.writeTo(sp.origin, ChatWriterType.GAME, "Your opponent has denied the swap request. You may try again to find another player to swap.");
							try {
								sp.close();
							} catch (Throwable e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
					}
					ChatWriter.writeTo(p, ChatWriterType.GAME, "You do not have any swap requests pending.");
					break;
				}
				break;
			default:
				sender.sendMessage(ChatWriter.getMessage(ChatWriterType.COMMAND, "Invalid Options."));
				ht.show(sender);
				return true;
			}
			return true;
		}else{
			return false;
		}
	}
}
