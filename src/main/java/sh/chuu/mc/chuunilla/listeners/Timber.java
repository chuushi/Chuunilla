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

import java.util.*;

// Usage: Strip log first, then break the stripped log to active timber
public class Timber implements Listener {
    private static final String PERMISSION_NODE = "chuunilla.timber";

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
        TreeCheck c = new TreeCheck(b);

        if (c.checkSurrounding(b, BlockFace.SELF, 0, true)) {
            p.sendMessage("Timber Passed");
            c.unstrip();
            Iterator<Block> it = c.logs.iterator();

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (it.hasNext()) it.next().breakNaturally(axe);
                    else this.cancel();
                }
            }.runTaskTimer(plugin, 0L, interval);
        }
        p.sendMessage("Timber Blocks: " + c.logs.size());
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

    private final class TreeCheck {
        private final Set<Block> logs = new LinkedHashSet<>();
        private final Block initial;
        private final Material leaves;
        private final Material log;

        private TreeCheck(Block initialBlock) {
            Material[] type = getLogLeaves(initialBlock.getType());
            if (type != null) {
                this.leaves = type[1];
                this.log = type[0];
                this.initial = initialBlock;
                this.logs.add(initialBlock);
            } else {
                this.leaves = null;
                this.log = null;
                this.initial = null;
            }
        }

        private boolean checkSurrounding(Block bl, BlockFace face, int side, boolean isLog) {
            if (side > 5) return false;
            int newSide = side + 1;
            Map<Block, BlockFace> toCheck = new LinkedHashMap<>();

            // itself
            if (isLog)
                toCheck.put(bl, face);
            for (BlockFace f : adjacent) {
                if (side != 0 && facePass(face, f)) continue;

                Block b = bl.getRelative(f);
                if (isLog(b) && addBlock(b)) {
                    toCheck.put(b, f);
                    checkSurrounding(b, f, newSide, true);
                }
            }

            // Upwards
            for (Map.Entry<Block, BlockFace> e : toCheck.entrySet()) {
                Block up = e.getKey().getRelative(BlockFace.UP);
                System.out.printf("y: %d\n", up.getY());

                if (isLog(up)) {
                    if (addBlock(up) && !checkSurrounding(up, e.getValue(), side, true))
                        return false;
                    continue;
                }

                if (checkSurrounding(up, BlockFace.SELF, side, false))
                    return true;

                if (!checkLeaves(up))
                    return false;
            }
            return true;
        }


        private void unstrip() {
            initial.setType(log, false);
        }

        private boolean isLog(Block log) {
            return log.getType() == this.log;
        }

        private boolean addBlock(Block log) {
            return logs.add(log);
        }

        private boolean checkLeaves(Block leaves) {
            return leaves.getType() == this.leaves && !((Leaves) leaves.getBlockData()).isPersistent();
        }

        private Material[] getLogLeaves(Material m) {
            switch (m) {
                case OAK_LOG:
                case STRIPPED_OAK_LOG:
                case OAK_LEAVES:
                    return new Material[]{Material.OAK_LOG, Material.OAK_LEAVES};
                case SPRUCE_LOG:
                case STRIPPED_SPRUCE_LOG:
                case SPRUCE_LEAVES:
                    return new Material[]{Material.SPRUCE_LOG, Material.SPRUCE_LEAVES};
                case BIRCH_LOG:
                case BIRCH_LEAVES:
                case STRIPPED_BIRCH_LOG:
                    return new Material[]{Material.BIRCH_LOG, Material.BIRCH_LEAVES};
                case JUNGLE_LOG:
                case STRIPPED_JUNGLE_LOG:
                case JUNGLE_LEAVES:
                    return new Material[]{Material.JUNGLE_LOG, Material.JUNGLE_LEAVES};
                case DARK_OAK_LOG:
                case STRIPPED_DARK_OAK_LOG:
                case DARK_OAK_LEAVES:
                    return new Material[]{Material.DARK_OAK_LOG, Material.DARK_OAK_LEAVES};
                case ACACIA_LOG:
                case STRIPPED_ACACIA_LOG:
                case ACACIA_LEAVES:
                    return new Material[]{Material.ACACIA_LOG, Material.ACACIA_LEAVES};
            }
            return null;
        }

    }
}