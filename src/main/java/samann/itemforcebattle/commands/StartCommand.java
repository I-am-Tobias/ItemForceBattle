package samann.itemforcebattle.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import samann.itemforcebattle.ItemForceBattle;

public class StartCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int minutes = 60;
        if(args.length >= 1){
            minutes = Integer.parseInt(args[0]);
        }
        ItemForceBattle.instance.start(minutes);
        return true;
    }
}
