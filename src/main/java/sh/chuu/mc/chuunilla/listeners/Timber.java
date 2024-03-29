package sh.chuu.mc.chuunilla.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
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
            c.unstrip();
            Iterator<Block> it = c.logs.iterator();
            it.next(); // skip first block

            int unb = axe.getEnchantmentLevel(Enchantment.DURABILITY) + 1;

            timbering.add(p);
            if (interval == 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        while (it.hasNext()) {
                            Block b = it.next();
                            if (b.getType() != c.log)
                                continue;
                            if (breakLogFailed(p, b, axe, unb))
                                break;
                        }
                        timbering.remove(p);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBarTextEnd);
                    }
                }.runTaskLater(plugin, 1L);
            } else {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBarTextStart);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!it.hasNext()) {
                            this.cancel();
                            return;
                        }
                        Block b = it.next();
                        while (b.getType() != c.log) {
                            if (!it.hasNext()) {
                                this.cancel();
                                return;
                            }
                            b = it.next();
                        }
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
    }

    private boolean breakLogFailed(Player p, Block b, ItemStack axe, int unb) {
        if (!p.getInventory().getItemInMainHand().isSimilar(axe)) // if item is out of hand (lol)
            return true;

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

        b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());

        b.breakNaturally(axe);
        axe.setItemMeta(d);
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
        return switch (axe) {
            case WOODEN_AXE -> 2;
            case STONE_AXE -> 4;
            case IRON_AXE -> 6;
            case GOLDEN_AXE -> 12;
            case DIAMOND_AXE -> 8;
            case NETHERITE_AXE -> 10;
            default -> -1;
        };
    }

    private int multiplierToTicks(int base, int eff, int haste) {
        int m = base;
        if (eff != 0)
            m += eff * eff + 1;
        if (haste != 0)
            m = m * (5 + haste) / 5;

        if (m >= 35)
            return 0;
        if (m >= 4)
            return 60 / m;
        else
            return -1;
    }

    private boolean isInitialLog(Block b) {
        BlockData data = b.getBlockData();
        // Check if initial log is orientated in Y (natural logs are usually like this)
        if (!(data instanceof Orientable) || ((Orientable) data).getAxis() != Axis.Y)
            return false;

        return switch (b.getType()) {
            case STRIPPED_OAK_LOG, STRIPPED_SPRUCE_LOG, STRIPPED_BIRCH_LOG, STRIPPED_JUNGLE_LOG, STRIPPED_DARK_OAK_LOG, STRIPPED_ACACIA_LOG, STRIPPED_WARPED_STEM, STRIPPED_CRIMSON_STEM -> true;
            default -> false;
        };
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
            return leaves.getType() == this.leaves && !(leaves instanceof Leaves && ((Leaves) leaves.getBlockData()).isPersistent());
        }

        private Material[] getLogLeaves(Material m) {
            return switch (m) {
                case OAK_LOG, STRIPPED_OAK_LOG, OAK_LEAVES -> new Material[]{Material.OAK_LOG, Material.OAK_LEAVES};
                case SPRUCE_LOG, STRIPPED_SPRUCE_LOG, SPRUCE_LEAVES -> new Material[]{Material.SPRUCE_LOG, Material.SPRUCE_LEAVES};
                case BIRCH_LOG, BIRCH_LEAVES, STRIPPED_BIRCH_LOG -> new Material[]{Material.BIRCH_LOG, Material.BIRCH_LEAVES};
                case JUNGLE_LOG, STRIPPED_JUNGLE_LOG, JUNGLE_LEAVES -> new Material[]{Material.JUNGLE_LOG, Material.JUNGLE_LEAVES};
                case DARK_OAK_LOG, STRIPPED_DARK_OAK_LOG, DARK_OAK_LEAVES -> new Material[]{Material.DARK_OAK_LOG, Material.DARK_OAK_LEAVES};
                case ACACIA_LOG, STRIPPED_ACACIA_LOG, ACACIA_LEAVES -> new Material[]{Material.ACACIA_LOG, Material.ACACIA_LEAVES};
                case WARPED_STEM, STRIPPED_WARPED_STEM, WARPED_WART_BLOCK -> new Material[]{Material.WARPED_STEM, Material.WARPED_WART_BLOCK};
                case CRIMSON_STEM, STRIPPED_CRIMSON_STEM, NETHER_WART_BLOCK -> new Material[]{Material.CRIMSON_STEM, Material.NETHER_WART_BLOCK};
                default -> null;
            };
        }
    }

    private record LogQueueParams(BlockFace face, int side, boolean isLog, boolean notEndOfBranch) {
    }
}