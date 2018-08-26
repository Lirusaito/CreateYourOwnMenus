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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptDeleteCommand extends IMenuScriptCommand {

    public MenuScriptDeleteCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) delete [index] - Deletes the line with the given index (0 for first) in the held item's lore";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.delete";
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

        if (args.length != 1) {
            return false;
        }

        String indexString = args[0];
        int index = getIndex(indexString, loreStrings.size(), sender);
        if (index == -1) {
            return true;
        }

        // Remove the deleted line
        String removedText;
        if (index == 0) {
            // Handle first-line special case
            String replacedWith;
            if (loreStrings.size() >= 2) {
                replacedWith = loreStrings.get(1);
                loreStrings.remove(1);
            } else {
                replacedWith = "";
            }
            String firstLine = loreStrings.get(0);
            int lastPartIndex = firstLine.lastIndexOf('\r') + 1;
            removedText = firstLine.substring(lastPartIndex);
            loreStrings.set(0, firstLine.substring(0, lastPartIndex) + replacedWith);
        } else {
            removedText = loreStrings.remove(index);
        }
        sender.sendMessage(plugin.translate(sender, "script-line-removed", "Removed {0} from line {1} in the command list of this menu item", removedText, index));

        // Update the item
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
