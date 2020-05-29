package sh.chuu.mc.chuunilla.listeners;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import sh.chuu.mc.chuunilla.Chuunilla;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Usage: Strip log first, then break the stripped log to active timber
public class Timber implements Listener {
    private static final String PERMISSION_NODE = "chuunilla.timber";
    private enum Specie {
        OAK, SPRUCE, BIRCH, JUNGLE, DARK_OAK, ACACIA
    }
    private enum CheckResult {
        LOG, NO_LEAVES, NONE
    }

    private final Chuunilla plugin = Chuunilla.getInstance();

    private static final BlockFace[] adjacent = {
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH_WEST,
            BlockFace.NORTH_WEST,
            BlockFace.NORTH_EAST,
            BlockFace.SOUTH_EAST
    };

    @EventHandler
    private void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission(PERMISSION_NODE) || p.isSneaking()) return;
        Block b = e.getBlock();
        if (!isInitialLog(b)) return;

        p.sendMessage("Timber Listen");
        ItemStack axe = e.getPlayer().getInventory().getItemInMainHand();
        long interval = axeTicks(axe.getType());
        if (interval == -1)
            return;

        p.sendMessage("Timber Activated");
        List<Block> blocks = new ArrayList<>();

        if (populateLogList(blocks, 0, getSpecie(b.getType()), b, BlockFace.SELF)) {
            p.sendMessage("Timber Passed");
            unstripLog(b);

            Iterator<Block> it = blocks.iterator();

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (it.hasNext()) it.next().breakNaturally(axe);
                    else this.cancel();
                }
            }.runTaskTimer(plugin, 0L, interval);
        }
        p.sendMessage("Timber Blocks: " + blocks.size());
    }

    /*
    Psuedobode: (itself is a log)
    1. traverse up and check
    2. if not log, check blocks surrounding up
    3. check blocks surrounding itself
     */
    // TODO This is so broken
    /**
     Psuedobode: (itself is a log)
     1. traverse up and check
     2. if up is not same specie, check surrounding of up
     3.   if nothing surrounds it, exit.
     4. if log, check surrounding then recursive call to up
     5. else if leaves, check surrounding then
     6. else just return false because idk wtf happened
     3. check blocks surrounding itself
     * @param list List of blocks
     * @param side How many blocks search went sideways
     * @param specie Specie
     * @param block Block to center the search around
     * @param face Direction of block to check
     * @return true if at least one branch was detected
     */
    private boolean populateLogList(List<Block> list, int side, Specie specie, Block block, BlockFace face) {
        if (side >= 5) return false;

        Block bl = block.getRelative(face);
        list.add(bl);

        // Get the above block to check
        // if it's a log
        // then recursive call
        // else check its surroundings
        Block up = bl.getRelative(BlockFace.UP);

        if (specie != getSpecie(up.getType())) {
            return checkSurrounding(list, side + 1, specie, bl, face) == CheckResult.LOG
                    || checkSurrounding(list, 0, specie, up, BlockFace.UP) == CheckResult.LOG;
        }

        if (isLog(up)) {
            if (list.contains(up))
                return true;

            return checkSurrounding(list, side+ + 1, specie, bl, face) != CheckResult.NO_LEAVES && populateLogList(list, 0, specie, bl, BlockFace.UP);
        }

        if (isNaturalLeaves(up)) {
            return checkSurrounding(list, side + 1, specie, bl, face) != CheckResult.NO_LEAVES
                    || checkSurrounding(list, 0, specie, up, BlockFace.UP) != CheckResult.NO_LEAVES;
        }

        return false;
    }

    /**
     *
     * @param list List of blocks
     * @param side how many blocks search went sideways
     * @param specie Specie
     * @param bl Block to center the search around
     * @param face Direction of block relative to previous block
     * @return true if at least one branch was detected, false if no branch was detected, null if branch ends without leaves
     */
    private CheckResult checkSurrounding(List<Block> list, int side, Specie specie, Block bl, BlockFace face) {
        CheckResult ret = CheckResult.NONE;
        for (BlockFace f : adjacent) {
            if (facePass(face, f)) continue;

            Block b = bl.getRelative(f);
            if (specie == getSpecie(b.getType()) && isLog(b) && !list.contains(b)) {
                if (!populateLogList(list, side, specie, bl, f))
                    return CheckResult.NO_LEAVES;
                ret = CheckResult.LOG;
            }
        }
        return ret;
    }

    private boolean facePass(BlockFace face, BlockFace check) {
        int fx = face.getModX();
        int fz = face.getModZ();
        int x = check.getModX();
        int z = check.getModZ();

        return !((fx == 0 && fz == 0) || (fx != 0 && fx == x) || (fz != 0 && fz == z));
    }

    private int axeTicks(Material m) {
        switch (m) {
            case WOODEN_AXE:
                return 10;
            case STONE_AXE:
                return 5;
            case IRON_AXE:
                return 2;
            case GOLDEN_AXE:
                return 2;
            case DIAMOND_AXE:
                return 1;
            default:
                return -1;
        }
    }

    private boolean isLog(Block b) {
        switch (b.getType()) {
            case OAK_LOG:
            case SPRUCE_LOG:
            case BIRCH_LOG:
            case JUNGLE_LOG:
            case DARK_OAK_LOG:
            case ACACIA_LOG:
                return true;
            default:
                return false;
        }
    }

    private boolean isInitialLog(Block b) {
        BlockData data = b.getBlockData();
        // Check if initial log is orientated in Y (natural logs are usually like this)
        if (!(data instanceof Orientable) || ((Orientable) data).getAxis() != Axis.Y)
            return false;

        switch (b.getType()) {
            case STRIPPED_OAK_LOG:
            case STRIPPED_SPRUCE_LOG:
            case STRIPPED_BIRCH_LOG:
            case STRIPPED_JUNGLE_LOG:
            case STRIPPED_DARK_OAK_LOG:
            case STRIPPED_ACACIA_LOG:
                return true;
            default:
                return false;
        }
    }

    private boolean isNaturalLeaves(Block b) {
        BlockData data = b.getBlockData();
        return data instanceof Leaves && !((Leaves) data).isPersistent();
    }

    private Specie getSpecie(Material tree) {
        switch (tree) {
            case OAK_LOG:
            case STRIPPED_OAK_LOG:
            case OAK_LEAVES:
                return Specie.OAK;
            case SPRUCE_LOG:
            case STRIPPED_SPRUCE_LOG:
            case SPRUCE_LEAVES:
                return Specie.SPRUCE;
            case BIRCH_LOG:
            case BIRCH_LEAVES:
            case STRIPPED_BIRCH_LOG:
                return Specie.BIRCH;
            case JUNGLE_LOG:
            case STRIPPED_JUNGLE_LOG:
            case JUNGLE_LEAVES:
                return Specie.JUNGLE;
            case DARK_OAK_LOG:
            case STRIPPED_DARK_OAK_LOG:
            case DARK_OAK_LEAVES:
                return Specie.DARK_OAK;
            case ACACIA_LOG:
            case STRIPPED_ACACIA_LOG:
            case ACACIA_LEAVES:
                return Specie.ACACIA;
            default:
                return null;
        }
    }

    private void unstripLog(Block b) {
        switch (b.getType()) {
            case STRIPPED_OAK_LOG:
                b.setType(Material.OAK_LOG, false);
                break;
            case STRIPPED_SPRUCE_LOG:
                b.setType(Material.SPRUCE_LOG, false);
                break;
            case STRIPPED_BIRCH_LOG:
                b.setType(Material.BIRCH_LOG, false);
                break;
            case STRIPPED_JUNGLE_LOG:
                b.setType(Material.JUNGLE_LOG, false);
                break;
            case STRIPPED_DARK_OAK_LOG:
                b.setType(Material.DARK_OAK_LOG, false);
                break;
            case STRIPPED_ACACIA_LOG:
                b.setType(Material.ACACIA_LOG, false);
                break;
        }
    }
}