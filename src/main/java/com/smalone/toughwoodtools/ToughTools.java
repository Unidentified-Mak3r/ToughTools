package com.smalone.toughwoodtools;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;

public class ToughTools extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ToughTools enabled: empowering wooden pickaxes and axes.");
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        ItemStack item = e.getItem();
        if (!isToughWoodTool(item)) {
            return;
        }

        ItemStack empowered = empower(item);
        replaceHeldItem(e.getPlayer(), item, empowered);
        e.setDamage(0); // never consume durability on our empowered tools
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent e) {
        ItemStack item = e.getItemInHand();
        if (!isToughWoodTool(item)) {
            return;
        }

        ItemStack empowered = empower(item);
        replaceHeldItem(e.getPlayer(), item, empowered);
        e.setInstaBreak(true); // instantly breaks targeted blocks while swinging
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        ItemStack result = e.getCurrentItem();
        if (!isToughWoodTool(result)) {
            return;
        }

        ItemStack empowered = empower(result);
        e.setCurrentItem(empowered);
    }

    private boolean isToughWoodTool(ItemStack item) {
        if (item == null) {
            return false;
        }

        Material type = item.getType();
        return type == Material.WOOD_PICKAXE || type == Material.WOOD_AXE;
    }

    private ItemStack empower(ItemStack item) {
        if (item == null) {
            return item;
        }

        ItemStack empowered = item.clone();
        ItemMeta meta = empowered.getItemMeta();
        if (meta == null) {
            return empowered;
        }

        // keep the tool pristine
        empowered.setDurability((short) 0);
        meta.setUnbreakable(true);
        empowered.setItemMeta(meta);

        // give the tools extreme efficiency-like behaviour and combat strength
        empowered.addUnsafeEnchantment(Enchantment.DIG_SPEED, 10);
        empowered.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        empowered.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 10);
        empowered.addUnsafeEnchantment(Enchantment.KNOCKBACK, 10);
        empowered.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 5);

        ItemStack withAttributes = applyAttributeModifiers(empowered);
        withAttributes.setAmount(item.getAmount());
        return withAttributes;
    }

    private ItemStack applyAttributeModifiers(ItemStack item) {
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        if (nmsStack == null || nmsStack.isEmpty()) {
            return item;
        }

        NBTTagCompound tag = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();
        NBTTagList modifiers = new NBTTagList();

        modifiers.add(createModifier("generic.attackDamage", "toughtools-damage", 100.0, DAMAGE_ID));
        modifiers.add(createModifier("generic.attackSpeed", "toughtools-speed", 10.0, SPEED_ID));
        modifiers.add(createModifier("generic.attackKnockback", "toughtools-knockback", 10.0, KNOCKBACK_ID));

        tag.set("AttributeModifiers", modifiers);
        nmsStack.setTag(tag);

        ItemStack result = CraftItemStack.asBukkitCopy(nmsStack);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            result.setItemMeta(meta);
        }
        result.setDurability((short) 0);
        return result;
    }

    private NBTTagCompound createModifier(String attributeName, String name, double amount, UUID id) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("AttributeName", attributeName);
        compound.setString("Name", name);
        compound.setDouble("Amount", amount);
        compound.setInt("Operation", 0); // 0 = addition
        compound.setLong("UUIDMost", id.getMostSignificantBits());
        compound.setLong("UUIDLeast", id.getLeastSignificantBits());
        compound.setString("Slot", "mainhand");
        return compound;
    }

    private void replaceHeldItem(Player player, ItemStack previous, ItemStack replacement) {
        if (player == null || previous == null || replacement == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack mainHand = inventory.getItemInMainHand();
        if (mainHand != null && (mainHand == previous || mainHand.equals(previous))) {
            inventory.setItemInMainHand(replacement);
            return;
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (offHand != null && (offHand == previous || offHand.equals(previous))) {
            inventory.setItemInOffHand(replacement);
        }
    }

    private static final UUID DAMAGE_ID = UUID.fromString("77763ca6-4df5-4b0b-8a6c-f78b7d0f5126");
    private static final UUID SPEED_ID = UUID.fromString("1e54d2f5-77bd-4f40-a25f-42595fd4bf3c");
    private static final UUID KNOCKBACK_ID = UUID.fromString("c29b4542-181e-4ccf-87f4-d1c22a640b4c");
}
