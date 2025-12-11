package com.smalone.toughwoodtools;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MobModifierListener implements Listener {

    private final Random random = new Random();

    public MobModifierListener() {
        // No plugin reference required for now, but you can add one if needed.
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();

        // Adult zombies get a random Knockback I sword
        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) entity;

            if (!zombie.isBaby()) {
                Material[] swords = {
                    Material.WOOD_SWORD,
                    Material.STONE_SWORD,
                    Material.IRON_SWORD,
                    Material.GOLD_SWORD,
                    Material.DIAMOND_SWORD
                };

                Material chosen = swords[random.nextInt(swords.length)];
                ItemStack weapon = new ItemStack(chosen);

                try {
                    weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                } catch (Exception ignored) {
                }

                zombie.getEquipment().setItemInHand(weapon);
                zombie.getEquipment().setItemInHandDropChance(0.05F);
            }
        }

        // Creepers spawn charged
        if (entity instanceof Creeper) {
            Creeper creeper = (Creeper) entity;
            creeper.setPowered(true);
        }
    }
}
