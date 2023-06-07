package samann.itemforcebattle;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import samann.itemforcebattle.commands.*;

import java.util.*;


public final class ItemForceBattle extends JavaPlugin {
    public static ItemForceBattle instance = null;
    Timer timer;
    List<CollectedItem> collectedItems;
    Material currentItem = Material.AIR;
    List<Material> nonValidItems = new ArrayList<>();
    Map<Player, Item> itemsAbovePlayer = new HashMap<>();
    List<Player> skipVotes = new ArrayList<>();

    public ItemForceBattle() {
        super();
        if(instance == null) instance = this;
    }

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("start")).setExecutor(new StartCommand());
        Objects.requireNonNull(getCommand("end")).setExecutor(new EndCommand());
        Objects.requireNonNull(getCommand("skip")).setExecutor(new SkipCommand());

        Bukkit.getPluginManager().registerEvents(new Listener(){
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event){
                spawnItemsAboveHead();
            }
        }, this);
    }

    @Override
    public void onDisable() {
        end();
    }


    public void start(int minutes) {
        if(timer != null && timer.isRunning()) end();
        collectedItems = new ArrayList<>();
        nonValidItems = new ArrayList<>();
        itemsAbovePlayer = new HashMap<>();
        selectNewItem();
        spawnItemsAboveHead();

        timer = new Timer(minutes * 60 * 20, () -> {
            end();
        }, () -> {
            checkInventory();
            itemsAbovePlayer.forEach((player, item) -> {
                if(player.getLocation().getWorld() != item.getLocation().getWorld()){
                    spawnItemsAboveHead();
                }
                item.teleport(player.getLocation().add(0, 2, 0));
                item.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
            });

            if(timer.getTicksLeft() % 20 == 0){
                sendActionBarText();
            }
        });
    }
    public void end() {
        timer.stop();
        itemsAbovePlayer.forEach((player, item) -> {
            item.remove();
        } );
        //set title for all players saying "Das Spiel ist beendet"
        getServer().getOnlinePlayers().forEach(player -> {
            player.sendTitle(ChatColor.RED + "Das Spiel ist beendet", "", 0, 20, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 1, 1);
        });
        sendResult();
    }
    void selectNewItem() {
        nonValidItems.add(currentItem);
        while(nonValidItems.contains(currentItem) || !currentItem.isItem()) {
            currentItem = Material.values()[new Random().nextInt(Material.values().length)];
        }

        //spawn items
        spawnItemsAboveHead();

        skipVotes.clear();
    }
    //check inventory of all players
    void checkInventory() {
        for(Player player : getServer().getOnlinePlayers()) {
            boolean hasItem = false;
            if(player.getInventory().contains(currentItem)) {
                hasItem = true;
            }
            if(player.getOpenInventory().getCursor() != null && player.getOpenInventory().getCursor().getType() == currentItem) {
                hasItem = true;
            }

            if(hasItem) {
                collectedItems.add(new CollectedItem(player, currentItem));
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
                msgAll(ChatColor.GREEN + player.getName() + " hat das Item " + ChatColor.AQUA + itemName(currentItem) + ChatColor.GREEN + " gefunden!");
                for(Player p : getServer().getOnlinePlayers()) {
                    if(p != player) {
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }
                }
                selectNewItem();
            }
        }
    }

    void sendActionBarText(){
        String text = ChatColor.GOLD.toString() + ChatColor.BOLD.toString();
        //print time left in formal like "1h 30m 30s" or "1m 30s" or "30s"
        int seconds = timer.getTicksLeft() / 20;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        if(hours > 0) {
            text += hours + "h ";
        }
        if(minutes > 0) {
            text += minutes + "m ";
        }
        if(seconds > 0) {
            text += seconds + "s";
        }
        text += ChatColor.RESET + " | " + ChatColor.GREEN + "Gesuchtes Item: " + ChatColor.AQUA + itemName(currentItem);
        for(Player player : getServer().getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
        }
    }

    void spawnItemsAboveHead(){
        itemsAbovePlayer.forEach((player, item) -> {
            item.remove();
        } );
        itemsAbovePlayer.clear();
        for(Player player : getServer().getOnlinePlayers()) {
            Item item = player.getWorld().dropItem(player.getLocation().add(0, 2, 0), new ItemStack(currentItem));
            item.setUnlimitedLifetime(true);
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setGravity(false);
            item.setInvulnerable(true);
            item.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
            itemsAbovePlayer.put(player, item);
        }
    }

    public void voteSkip(Player player){
        if(!skipVotes.contains(player)) {
            skipVotes.add(player);
            msgAll(ChatColor.YELLOW + player.getName() + " hat für das Skippen von " + ChatColor.AQUA + itemName(currentItem) + ChatColor.YELLOW + " gestimmt!");
        }
        if(skipVotes.containsAll(getServer().getOnlinePlayers())) {
            selectNewItem();
            msgAll(ChatColor.LIGHT_PURPLE + "Das Item wurde geskippt!");
        }
    }

    void msgAll(String msg){
        getServer().getOnlinePlayers().forEach(player -> {
            player.sendMessage(msg);
        });
    }

    void sendResult(){
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        players.sort((p1, p2) -> {
            int p1Count = (int) collectedItems.stream().filter(collectedItem -> collectedItem.playerUUID.equals(p1.getUniqueId())).count();
            int p2Count = (int) collectedItems.stream().filter(collectedItem -> collectedItem.playerUUID.equals(p2.getUniqueId())).count();
            return p2Count - p1Count;
        });
        msgAll(ChatColor.BLUE + "Das Spiel ist beendet!");
        msgAll(ChatColor.GOLD + "Ergebnis:");
        for(int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            msgAll(ChatColor.GREEN.toString() + (i+1) + ". " + player.getName() + ": " + collectedItems.stream().filter(collectedItem -> collectedItem.playerUUID.equals(player.getUniqueId())).count() + " gefundene Items");
        }
    }

    String itemName(Material mat){
        return mat.name().replaceAll("_", " ").toLowerCase();
    }

}
