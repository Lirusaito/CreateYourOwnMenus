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
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptHideCommand extends IMenuScriptCommand {

    public MenuScriptHideCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) hide - Hides all commands for the menu item in your hand, and shows all comments";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.hide";
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

        // Hide the commands
        StringBuilder commands = new StringBuilder();
        List<String> comments = new ArrayList<>();

        // Expand all hidden lines from the first line
        if (!loreStrings.isEmpty()) {
            String firstLine = loreStrings.get(0);
            List<String> lines = unpackHiddenLines(firstLine);
            loreStrings.set(0, lines.get(lines.size() - 1));
            loreStrings.addAll(lines.subList(0, lines.size() - 1));
        }

        for (int i = 0; i < loreStrings.size(); i++) {
            String loreString = loreStrings.get(i);
            if (loreString.startsWith(commandStart)
                    || loreString.startsWith(playerCommand)) {
                commands.append(packHiddenText(loreString)).append(ChatColor.COLOR_CHAR).append('\r');
            } else if (loreString.startsWith(hiddenCommand)
                    || loreString.startsWith(hiddenPlayerCommand)) {
                // Legacy command hiding
                commands.append(loreString).append(ChatColor.COLOR_CHAR).append('\r');
            } else {
                comments.add(loreString);
            }
        }

        // and condense them into the first line
        loreStrings = comments;
        if (loreStrings.isEmpty()) {
            loreStrings.add(commands.toString());
        } else {
            loreStrings.set(0, commands.toString() + loreStrings.get(0));
        }

        sender.sendMessage(plugin.translate(sender, "script-commands-hidden", "All commands on this menu item should now be hidden"));

        // Update the item
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
