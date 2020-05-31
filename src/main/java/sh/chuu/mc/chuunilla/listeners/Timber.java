package sh.chuu.mc.chuunilla.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import sh.chuu.mc.chuunilla.Chuunilla;

import java.util.*;

// Usage: Strip log first, then break the stripped log to active timber
public class Timber implements Listener {
    private static final String PERMISSION_NODE = "chuunilla.timber";
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
    private final TextComponent actionBarTextStart = new TextComponent("Timber activated");
    private final TextComponent actionBarTextEnd = new TextComponent("Timber finished");

    private final Chuunilla plugin = Chuunilla.getInstance();
    private final Random random = new Random();
    private final Set<Player> timbering = new HashSet<>();

    @EventHandler
    private void onBlockBreak(BlockBreakEvent ev) {
        Player p = ev.getPlayer();

        if (!p.hasPermission(PERMISSION_NODE) || p.isSneaking() || timbering.contains(p)) return;
        Block bl = ev.getBlock();
        if (!isInitialLog(bl)) return;

        ItemStack axe = ev.getPlayer().getInventory().getItemInMainHand();
        int multiplier = axeMultiplier(axe.getType());
        if (multiplier == -1 || p.getPotionEffect(PotionEffectType.SLOW_DIGGING) != null)
            return;
        PotionEffect haste = p.getPotionEffect(PotionEffectType.FAST_DIGGING);
        long interval = multiplierToTicks(multiplier, axe.getEnchantmentLevel(Enchantment.DIG_SPEED), haste != null ? haste.getAmplifier() + 1 : 0);
        if (interval == -1)
            return;

        TreeCheck c = new TreeCheck(bl);
        if (c.prepareLogs()) {
            timbering.add(p);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBarTextStart);
            c.unstrip();
            Iterator<Block> it = c.logs.iterator();
            it.next(); // skip first block

            int unb = axe.getEnchantmentLevel(Enchantment.DURABILITY) + 1;

            if (interval == 0) {
                while (it.hasNext()) {
                    Block b = it.next();
                    if (breakLogFailed(p, b, axe, unb))
                        break;
                }
                timbering.remove(p);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBarTextEnd);
            } else new BukkitRunnable() {
                @Override
                public void run() {
                    if (!it.hasNext()) {
                        this.cancel();
                        return;
                    }
                    Block b = it.next();
                    if (breakLogFailed(p, b, axe, unb))
                        this.cancel();
                }

                @Override
                public synchronized void cancel() throws IllegalStateException {
                    super.cancel();
                    timbering.remove(p);
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBarTextEnd);
                }
            }.runTaskTimer(plugin, interval, interval);
        }
    }

    private boolean breakLogFailed(Player p, Block b, ItemStack axe, int unb) {
        BlockBreakEvent nev = new BlockBreakEvent(b, p);
        Bukkit.getPluginManager().callEvent(nev);
        if (nev.isCancelled()) {
            return true;
        }

        Damageable d = ((Damageable) axe.getItemMeta());
        if (unb == 1 || random.nextInt(unb) == 0) {
            //noinspection ConstantConditions Axes are damageable.
            int durability = d.getDamage() + 1;
            if (durability >= axe.getType().getMaxDurability())
                return true;
            d.setDamage(durability);
        }

        b.breakNaturally(axe);
        axe.setItemMeta((ItemMeta) d);

        World w = b.getWorld();
        w.playSound(b.getLocation(), Sound.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.0f, 0.8f);
        // TODO figure out particles
        //w.spawnParticle(Particle.BLOCK_DUST, b.getLocation(), 10, b.getBlockData());
        return false;
    }


    private boolean facePass(BlockFace face, BlockFace check) {
        int fx = face.getModX();
        int fz = face.getModZ();
        int x = check.getModX();
        int z = check.getModZ();

        return !((fx == 0 && fz == 0) || (fx != 0 && fx == x) || (fz != 0 && fz == z));
    }

    private int axeMultiplier(Material axe) {
        switch (axe) {
            case WOODEN_AXE:
                return 2;
            case STONE_AXE:
                return 4;
            case IRON_AXE:
                return 6;
            case GOLDEN_AXE:
                return 12;
            case DIAMOND_AXE:
                return 8;
            default:
                return -1;
        }
    }

    private int multiplierToTicks(int base, int eff, int haste) {
        int m = base;
        if (eff != 0)
            m += eff * eff + 1;
        if (haste != 0)
            m = m * (5 + haste) / 5;

        if (m >= 35)
            return 0;
        if (m >= 25)
            return 1;
        if (m >= 20)
            return 2;
        if (m >= 15)
            return 3;
        if (m >= 10)
            return 4;
        if (m >= 4)
            return 14 - m;
        else
            return -1;
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
        private final Map<Block, LogQueueParams> queue1 = new LinkedHashMap<>();
        private final Map<Block, LogQueueParams> queue2 = new LinkedHashMap<>();

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

        private boolean prepareLogs() {
            Map<Block, LogQueueParams> queue = queue1;
            Map<Block, LogQueueParams> gather = queue2;
            queue.clear();
            gather.clear();

            queue.put(initial, new LogQueueParams(BlockFace.SELF, 0, true, true));
            while (!queue.isEmpty()) {
                for (Map.Entry<Block, LogQueueParams> e : queue.entrySet()) {
                    LogQueueParams c = e.getValue();
                    if (!checkSurrounding(gather, e.getKey(), c.face, c.side, c.isLog, c.notEndOfBranch))
                        return false;
                }
                queue.clear();
                // swap queue and gather
                queue = gather;
                gather = queue == queue1 ? queue2 : queue1;
            }

            return true;
        }

        private boolean checkSurrounding(Map<Block, LogQueueParams> queue, Block bl, BlockFace face, int side, boolean isLog, boolean notEndOFBranch) {
            if (side > 5)
                return false;

            int newSide = side + 1;
            boolean hasLog = false;

            // itself
            for (BlockFace f : adjacent) {
                if (side != 0 && facePass(face, f)) continue;

                Block b = bl.getRelative(f);
                if (isLog(b)) {
                    hasLog = true;
                    if (addBlock(b)) {
                        queue.put(b, new LogQueueParams(f, newSide, true, true));
                    }
                }
            }

            // Upwards
            if (isLog) {
                Block up = bl.getRelative(BlockFace.UP);

                if (isLog(up)) {
                    if (addBlock(up)) {
                        queue.put(up, new LogQueueParams(face, side, true, hasLog));
                    }
                    return true;
                }

                queue.put(up, new LogQueueParams(BlockFace.SELF, side, false, hasLog));
                return true;
            } else {
                // this is leaves or any other block.  Empty queue means no log were put in list.
                return notEndOFBranch || hasLog || checkLeaves(bl);
            }
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

    private class LogQueueParams {
        private final BlockFace face;
        private final int side;
        private final boolean isLog;
        private final boolean notEndOfBranch;

        private LogQueueParams(BlockFace face, int side, boolean isLog, boolean notEndOfBranch) {
            this.face = face;
            this.side = side;
            this.isLog = isLog;
            this.notEndOfBranch = notEndOfBranch;
        }
    }
}