/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import java.util.ArrayList;
import java.util.List;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptCommand;
import me.xhawk87.CreateYourOwnMenus.utils.ItemStackRef;
import static me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils.commandStart;
import static me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils.hiddenCommand;
import static me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils.hiddenPlayerCommand;
import static me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils.packHiddenText;
import static me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils.playerCommand;
import static me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils.unpackHiddenLines;
import static me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils.unpackHiddenText;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptShowCommand extends IMenuScriptCommand {

    public MenuScriptShowCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) show - Shows all hidden commands for the menu item in your hand, and hides all comments";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.show";
    }

    @Override
    public boolean onCommand(CommandSender sender, ItemStackRef itemStackRef, Command command, String label, String[] args) {
        // Check the player is holding the item
        ItemStack held = itemStackRef.get();
        if (held == null || held.getType().getId() == 0) {
            sender.sendMessage(plugin.translate(sender, "error-no-item-in-hand", "You must be holding a menu item"));
            return true;
        }

        // Get or create the lore
        ItemMeta meta = held.getItemMeta();
        List<String> loreStrings;
        if (meta.hasLore()) {
            loreStrings = meta.getLore();
        } else {
            loreStrings = new ArrayList<>();
        }

        // Show the hidden commands
        StringBuilder comments = new StringBuilder();
        List<String> commands = new ArrayList<>();

        // Expand all hidden commands from the first line
        if (!loreStrings.isEmpty()) {
            String firstLine = loreStrings.get(0);
            List<String> lines = unpackHiddenLines(firstLine);
            loreStrings.set(0, lines.get(lines.size() - 1));
            loreStrings.addAll(lines.subList(0, lines.size() - 1));
        }

        for (String loreString : loreStrings) {
            if (loreString.startsWith(commandStart)
                    || loreString.startsWith(playerCommand)) {
                commands.add(loreString);
            } else if (loreString.startsWith(hiddenCommand)
                    || loreString.startsWith(hiddenPlayerCommand)) {
                // Legacy command hiding
                commands.add(unpackHiddenText(loreString));
            } else {
                comments.append(packHiddenText(loreString)).append(ChatColor.COLOR_CHAR).append('\r');
            }
        }

        // and condense them into the first line
        loreStrings = commands;
        if (loreStrings.isEmpty()) {
            loreStrings.add(comments.toString());
        } else {
            loreStrings.set(0, comments.toString() + loreStrings.get(0));
        }

        sender.sendMessage(plugin.translate(sender, "script-commands-shown", "All commands on this menu item should now be visible"));

        // Update the item
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
