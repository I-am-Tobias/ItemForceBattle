package samann.itemforcebattle;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CollectedItem {
    public final UUID playerUUID;
    public final Material material;

    public CollectedItem(Player player, Material material) {
        this.playerUUID = player.getUniqueId();
        this.material = material;
    }
}
