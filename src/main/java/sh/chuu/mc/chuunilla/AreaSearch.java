package sh.chuu.mc.chuunilla;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.BlockState;

public class AreaSearch {
    public static boolean isNearBeacon(Location loc) {
        Chunk center = loc.getChunk();
        World w = loc.getWorld();
        int x = center.getX();
        int z = center.getZ();

        // Chunks in radius of 4
        @SuppressWarnings("ConstantConditions") // world always exists
        Chunk[] chunks = {center,
                w.getChunkAt(x, z+1),
                w.getChunkAt(x, z-1),
                w.getChunkAt(x, z+2),
                w.getChunkAt(x, z-2),
                w.getChunkAt(x, z+3),
                w.getChunkAt(x, z-3),
                w.getChunkAt(x, z+4),
                w.getChunkAt(x, z+4),
                w.getChunkAt(x+1, z),
                w.getChunkAt(x+1, z+1),
                w.getChunkAt(x+1, z-1),
                w.getChunkAt(x+1, z+2),
                w.getChunkAt(x+1, z-2),
                w.getChunkAt(x+1, z+3),
                w.getChunkAt(x+1, z-3),
                w.getChunkAt(x+1, z+4),
                w.getChunkAt(x+1, z+4),
                w.getChunkAt(x-1, z+1),
                w.getChunkAt(x-1, z-1),
                w.getChunkAt(x-1, z+2),
                w.getChunkAt(x-1, z-2),
                w.getChunkAt(x-1, z+3),
                w.getChunkAt(x-1, z-3),
                w.getChunkAt(x-1, z+4),
                w.getChunkAt(x-1, z+4),
                w.getChunkAt(x+2, z+1),
                w.getChunkAt(x+2, z),
                w.getChunkAt(x+2, z-1),
                w.getChunkAt(x+2, z+2),
                w.getChunkAt(x+2, z-2),
                w.getChunkAt(x+2, z+3),
                w.getChunkAt(x+2, z-3),
                w.getChunkAt(x+2, z+4),
                w.getChunkAt(x+2, z+4),
                w.getChunkAt(x-2, z+1),
                w.getChunkAt(x-2, z-1),
                w.getChunkAt(x-2, z+2),
                w.getChunkAt(x-2, z-2),
                w.getChunkAt(x-2, z+3),
                w.getChunkAt(x-2, z-3),
                w.getChunkAt(x-2, z+4),
                w.getChunkAt(x-2, z+4),
                w.getChunkAt(x+3, z),
                w.getChunkAt(x+3, z+1),
                w.getChunkAt(x+3, z-1),
                w.getChunkAt(x+3, z+2),
                w.getChunkAt(x+3, z-2),
                w.getChunkAt(x+3, z+3),
                w.getChunkAt(x+3, z-3),
                w.getChunkAt(x+3, z+4),
                w.getChunkAt(x+3, z+4),
                w.getChunkAt(x-3, z+1),
                w.getChunkAt(x-3, z-1),
                w.getChunkAt(x-3, z+2),
                w.getChunkAt(x-3, z-2),
                w.getChunkAt(x-3, z+3),
                w.getChunkAt(x-3, z-3),
                w.getChunkAt(x-3, z+4),
                w.getChunkAt(x-3, z+4),
                w.getChunkAt(x+4, z),
                w.getChunkAt(x+4, z+1),
                w.getChunkAt(x+4, z-1),
                w.getChunkAt(x+4, z+2),
                w.getChunkAt(x+4, z-2),
                w.getChunkAt(x+4, z+3),
                w.getChunkAt(x+4, z-3),
                w.getChunkAt(x+4, z+4),
                w.getChunkAt(x+4, z+4),
                w.getChunkAt(x-4, z+1),
                w.getChunkAt(x-4, z-1),
                w.getChunkAt(x-4, z+2),
                w.getChunkAt(x-4, z-2),
                w.getChunkAt(x-4, z+3),
                w.getChunkAt(x-4, z-3),
                w.getChunkAt(x-4, z+4),
                w.getChunkAt(x-4, z+4),
        };

        for (Chunk c : chunks) {
            if (!c.isLoaded()) continue;
            for (BlockState state : c.getTileEntities()) {
                if (state instanceof Beacon) {
                    Beacon b = (Beacon) state;
                    int tier = b.getTier();
                    if (tier == 0) continue;
                    int maxd = 10 + tier * 10;
                    int dx = b.getLocation().getBlockX() - loc.getBlockX();
                    int dz = b.getLocation().getBlockZ() - loc.getBlockZ();
                    if (Math.abs(dx) <= maxd && Math.abs(dz) <= maxd)
                        return true;
                }
            }
        }
        return false;
    }
}
