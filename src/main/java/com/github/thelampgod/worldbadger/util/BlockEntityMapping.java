package com.github.thelampgod.worldbadger.util;

import java.util.*;

public class BlockEntityMapping {

    // Block Entity -> Set of valid blocks
    private static final Map<String, Set<String>> BLOCK_ENTITY_TO_BLOCKS = new HashMap<>();

    // Block -> Block Entity
    private static final Map<String, String> BLOCK_TO_BLOCK_ENTITY = new HashMap<>();

    static {
        initializeMappings();
    }

    private static void initializeMappings() {
        // shelf
        addBlockEntityMapping("shelf", Arrays.asList(
                "oak_shelf", "spruce_shelf", "birch_shelf", "jungle_shelf",
                "acacia_shelf", "dark_oak_shelf", "mangrove_shelf", "cherry_shelf",
                "pale_oak_shelf", "bamboo_shelf", "crimson_shelf", "warped_shelf"
        ));

        // sign
        addBlockEntityMapping("sign", Arrays.asList(
                "oak_sign", "spruce_sign", "birch_sign", "jungle_sign", "acacia_sign",
                "dark_oak_sign", "mangrove_sign", "cherry_sign", "pale_oak_sign",
                "bamboo_sign", "crimson_sign", "warped_sign", "oak_wall_sign",
                "spruce_wall_sign", "birch_wall_sign", "jungle_wall_sign",
                "acacia_wall_sign", "dark_oak_wall_sign", "mangrove_wall_sign",
                "cherry_wall_sign", "pale_oak_wall_sign", "bamboo_wall_sign",
                "crimson_wall_sign", "warped_wall_sign"
        ));

        // hanging_sign
        addBlockEntityMapping("hanging_sign", Arrays.asList(
                "oak_hanging_sign", "spruce_hanging_sign", "birch_hanging_sign",
                "jungle_hanging_sign", "acacia_hanging_sign", "dark_oak_hanging_sign",
                "mangrove_hanging_sign", "cherry_hanging_sign", "pale_oak_hanging_sign",
                "bamboo_hanging_sign", "crimson_hanging_sign", "warped_hanging_sign",
                "oak_wall_hanging_sign", "spruce_wall_hanging_sign", "birch_wall_hanging_sign",
                "jungle_wall_hanging_sign", "acacia_wall_hanging_sign", "dark_oak_wall_hanging_sign",
                "mangrove_wall_hanging_sign", "cherry_wall_hanging_sign", "pale_oak_wall_hanging_sign",
                "bamboo_wall_hanging_sign", "crimson_wall_hanging_sign", "warped_wall_hanging_sign"
        ));

        // chest
        addBlockEntityMapping("chest", Arrays.asList(
                "chest", "copper_chest", "exposed_copper_chest", "weathered_copper_chest",
                "oxidized_copper_chest", "waxed_copper_chest", "waxed_exposed_copper_chest",
                "waxed_weathered_copper_chest", "waxed_oxidized_copper_chest"
        ));

        // copper_golem_statue
        addBlockEntityMapping("copper_golem_statue", Arrays.asList(
                "copper_golem_statue", "exposed_copper_golem_statue", "weathered_copper_golem_statue",
                "oxidized_copper_golem_statue", "waxed_copper_golem_statue", "waxed_exposed_copper_golem_statue",
                "waxed_weathered_copper_golem_statue", "waxed_oxidized_copper_golem_statue"
        ));

        // banner
        addBlockEntityMapping("banner", Arrays.asList(
                "white_banner", "orange_banner", "magenta_banner", "light_blue_banner",
                "yellow_banner", "lime_banner", "pink_banner", "gray_banner",
                "light_gray_banner", "cyan_banner", "purple_banner", "blue_banner",
                "brown_banner", "green_banner", "red_banner", "black_banner",
                "white_wall_banner", "orange_wall_banner", "magenta_wall_banner",
                "light_blue_wall_banner", "yellow_wall_banner", "lime_wall_banner",
                "pink_wall_banner", "gray_wall_banner", "light_gray_wall_banner",
                "cyan_wall_banner", "purple_wall_banner", "blue_wall_banner",
                "brown_wall_banner", "green_wall_banner", "red_wall_banner", "black_wall_banner"
        ));

        // bed
        addBlockEntityMapping("bed", Arrays.asList(
                "white_bed", "orange_bed", "magenta_bed", "light_blue_bed",
                "yellow_bed", "lime_bed", "pink_bed", "gray_bed",
                "light_gray_bed", "cyan_bed", "purple_bed", "blue_bed",
                "brown_bed", "green_bed", "red_bed", "black_bed"
        ));

        // shulker_box
        addBlockEntityMapping("shulker_box", Arrays.asList(
                "shulker_box", "white_shulker_box", "orange_shulker_box", "magenta_shulker_box",
                "light_blue_shulker_box", "yellow_shulker_box", "lime_shulker_box", "pink_shulker_box",
                "gray_shulker_box", "light_gray_shulker_box", "cyan_shulker_box", "purple_shulker_box",
                "blue_shulker_box", "brown_shulker_box", "green_shulker_box", "red_shulker_box", "black_shulker_box"
        ));

        // skull
        addBlockEntityMapping("skull", Arrays.asList(
                "skeleton_skull", "wither_skeleton_skull", "zombie_head", "player_head",
                "creeper_head", "dragon_head", "piglin_head", "skeleton_wall_skull",
                "wither_skeleton_wall_skull", "zombie_wall_head", "player_wall_head",
                "creeper_wall_head", "dragon_wall_head", "piglin_wall_head"
        ));

        // command_block
        addBlockEntityMapping("command_block", Arrays.asList(
                "command_block", "chain_command_block", "repeating_command_block"
        ));

        // brushable_block
        addBlockEntityMapping("brushable_block", Arrays.asList(
                "suspicious_gravel", "suspicious_sand"
        ));

        // beehive
        addBlockEntityMapping("beehive", Arrays.asList(
                "beehive", "bee_nest"
        ));

        // campfire
        addBlockEntityMapping("campfire", Arrays.asList(
                "campfire", "soul_campfire"
        ));

        // mob_spawner
        addBlockEntityMapping("mob_spawner", List.of(
                "spawner"
        ));

        // Same name block form / block entity (1:1 mapping)
        String[] sameNameEntities = {
                "barrel", "creaking_heart", "end_portal", "end_gateway", "furnace", "blast_furnace",
                "smoker", "trapped_chest", "ender_chest", "enchanting_table", "lectern",
                "jukebox", "bell", "brewing_stand", "trial_spawner", "vault", "decorated_pot",
                "beacon", "conduit", "comparator", "hopper", "dispenser", "dropper",
                "crafter", "daylight_detector", "sculk_sensor", "calibrated_sculk_sensor",
                "sculk_catalyst", "sculk_shrieker", "chiseled_bookshelf", "test_instance_block",
                "test_block", "structure_block", "jigsaw"
        };

        for (String entity : sameNameEntities) {
            addBlockEntityMapping(entity, Collections.singletonList(entity));
        }
    }

    private static void addBlockEntityMapping(String blockEntityId, List<String> blockIds) {
        Set<String> blockSet = new HashSet<>(blockIds);
        BLOCK_ENTITY_TO_BLOCKS.put(blockEntityId, blockSet);

        // Create reverse mapping
        for (String blockId : blockSet) {
            BLOCK_TO_BLOCK_ENTITY.put(blockId, blockEntityId);
        }
    }

    /**
     * Get all valid blocks for a block entity type
     */
    public static Set<String> getBlocksForBlockEntity(String blockEntityId) {
        return BLOCK_ENTITY_TO_BLOCKS.getOrDefault(normalizeNamespace(blockEntityId), Collections.emptySet());
    }

    /**
     * Get the block entity type for a given block
     */
    public static String getBlockEntityForBlock(String blockId) {
        return BLOCK_TO_BLOCK_ENTITY.get(normalizeNamespace(blockId));
    }

    /**
     * Check if a block entity has a valid corresponding block at the given position
     */
    public static boolean isValidBlockForBlockEntity(String blockEntityId, String blockId) {
        Set<String> validBlocks = getBlocksForBlockEntity(normalizeNamespace(blockEntityId));
        return validBlocks.contains(normalizeNamespace(blockId));
    }

    /**
     * Check if a block should have a block entity
     */
    public static boolean shouldHaveBlockEntity(String blockId) {
        return BLOCK_TO_BLOCK_ENTITY.containsKey(normalizeNamespace(blockId));
    }

    /**
     * Check if a block entity type is known
     */
    public static boolean isKnownBlockEntity(String blockEntityId) {
        return BLOCK_ENTITY_TO_BLOCKS.containsKey(normalizeNamespace(blockEntityId));
    }

    /**
     * Get all known block entity types
     */
    public static Set<String> getAllBlockEntityTypes() {
        return BLOCK_ENTITY_TO_BLOCKS.keySet();
    }

    /**
     * Get all blocks that should have block entities
     */
    public static Set<String> getAllBlocksWithBlockEntities() {
        return BLOCK_TO_BLOCK_ENTITY.keySet();
    }

    private static String normalizeNamespace(String id) {
        if (id.startsWith("minecraft:")) {
            return id.substring(10);
        }

        return id;
    }
}