package samann.itemforcebattle.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import samann.itemforcebattle.ItemForceBattle;

public class SkipCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ItemForceBattle.instance.voteSkip((Player)sender);
        return true;
    }
}
