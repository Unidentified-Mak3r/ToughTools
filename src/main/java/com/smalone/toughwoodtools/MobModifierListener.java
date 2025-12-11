@EventHandler
public void onMobSpawn(CreatureSpawnEvent event) {
    Entity entity = event.getEntity();

    // Handle adult zombies
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
                // Some servers may restrict unsafe enchantments
            }

            zombie.getEquipment().setItemInHand(weapon);
            zombie.getEquipment().setItemInHandDropChance(0.05F);
        }
    }

    // Handle creepers (always spawn charged)
    if (entity instanceof Creeper) {
        Creeper creeper = (Creeper) entity;
        creeper.setPowered(true);
    }
}



