package xiangrong.testmod.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.brewing.BrewingRecipeRegisterEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.block.BlockDirtPortal;
import xiangrong.testmod.block.ModBlocks;
import xiangrong.testmod.effect.ModEffects;
import xiangrong.testmod.enchantment.ModEnchantments;
import xiangrong.testmod.entity.EntityDirtBoss;
import xiangrong.testmod.entity.EntityDirtGolem;
import xiangrong.testmod.entity.ModEntities;
import xiangrong.testmod.item.ModItems;
import xiangrong.testmod.potion.ModPotions;
import xiangrong.testmod.villager.ModVillagers;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    @SubscribeEvent
    public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().is(ModItems.DIRT_BALL.get())) {
            event.setBurnTime(400);
        }
    }

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        if(event.getType()== ModVillagers.DIRT_WORKER.get()){
            var trades = event.getTrades();

            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD,1),
                    new ItemStack(ModItems.DIRT_BALL.get(),1),6,4,0.05F
            ));
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(Items.DIRT,12),
                    new ItemStack(Items.EMERALD,1),6,4,0.05F
            ));


            trades.get(2).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(ModBlocks.COMPRESSED_DIRT_BLOCK.get(),1),
                    new ItemStack(Items.EMERALD,1),6,6,0.05F
            ));
            trades.get(2).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD,5),
                    new ItemStack(ModItems.DIRT_PICKAXE.get(),1),6,7,0.05F
            ));

            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD,6),
                    new ItemStack(ModItems.DIRT_HELMET.get(),1),6,10,0.05F
            ));
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD,6),
                    Optional.of(new ItemCost(ModBlocks.COMPRESSED_DIRT_BLOCK.get(),1)),
                    new ItemStack(ModItems.DIRT_LEGGINGS.get(),1),6,11,0.05F
            ));
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD,6),
                    new ItemStack(ModItems.DIRT_BOOTS.get(),1),6,10,0.05F
            ));
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD,6),
                    Optional.of(new ItemCost(ModBlocks.COMPRESSED_DIRT_BLOCK.get(),1)),
                    new ItemStack(ModItems.DIRT_CHESTPLATE.get(),1),6,12,0.05F
            ));

            trades.get(4).add((pTrader, pRandom) -> {
                ItemStack enchantedPick = new ItemStack(ModItems.DIRT_PICKAXE.get());

                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                mutable.set(ModEnchantments.DIRT_DESTROYER.getOrThrow(pTrader), 1);
                EnchantmentHelper.setEnchantments(enchantedPick, mutable.toImmutable());

                return new MerchantOffer(
                        new ItemCost(Items.EMERALD, 10),
                        Optional.of(new ItemCost(ModBlocks.COMPRESSED_DIRT_BLOCK.get(), 3)),
                        enchantedPick, 2, 15, 0.05F
                );
            });

            trades.get(5).add((pTrader, pRandom) -> {
                EnchantmentInstance enchantmentInstance = new EnchantmentInstance(ModEnchantments.DIRT_DESTROYER.getOrThrow(pTrader),1);
                ItemStack enchantedBook = EnchantmentHelper.createBook(enchantmentInstance);

                return new MerchantOffer(
                        new ItemCost(Items.EMERALD, 24),
                        Optional.of(new ItemCost(ModBlocks.COMPRESSED_DIRT_BLOCK.get(), 10)),
                       enchantedBook, 2, 20, 0.05F
                );
            });
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();

        if(!entity.hasEffect(ModEffects.DIRT_PROTECTION_EFFECT.getHolder().get())) return;

        BlockPos posBelow = entity.blockPosition().below();
        Block blockBelow = entity.level().getBlockState(posBelow).getBlock();

        if (blockBelow == Blocks.DIRT ||
                blockBelow == Blocks.GRASS_BLOCK ||
                blockBelow == ModBlocks.COMPRESSED_DIRT_BLOCK.get()) {
            event.setDistance(event.getDistance()*0.5F);
        }

    }

    @SubscribeEvent
    public static void onBrewingRecipeRegister(BrewingRecipeRegisterEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();

        builder.addMix(Potions.AWKWARD, ModItems.DIRT_BALL.get(), ModPotions.DIRT_PROTECTION_POTION.getHolder().get());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity().level().isClientSide) return;

        Player player = event.getEntity();;
        if(player.getPersistentData().getBoolean("dirt_book_given")) return;

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        WrittenBookContent content = new WrittenBookContent(
                Filterable.passThrough("模组手册"), // title
                "翔嵘",                                   // author
                0,                                        // generation
                List.of(
                        Filterable.passThrough(Component.literal(
                                "DirtCraft 泥土工业\n测试版 V0.3\n\n"+
                                        "最新内容：\n\n"+
                                        "1.现在击杀土灵掉落一个§4泥土之星§r\n\n"+
                                        "2.添加了新维度§4泥土世界§r，该维度完全由泥土和压缩泥土构成，会刷新大量的土傀儡\n\n"+
                                        "3.通过§4泥土传送门§r进行维度传送，建立方法和暮色森林类"
                        )),
                        Filterable.passThrough(Component.literal(
                                "似：压缩泥土组成水平4×4框架，中间四格放水，往水里丢入泥土之星激活传送门\n\n"+
                                        "4.泥土世界刷新§4泥土精华矿§r，生成在32格以下，较为稀有\n\n"+
                                        "5.泥土精华矿可以提取")
                                .append(Component.literal("泥土精华").withStyle(ChatFormatting.DARK_RED))
                                .append(Component.literal(
                                        "（提取方式后续加入）\n\n"+
                                        "6.九个泥土精华可以合成"
                                ))
                                .append(Component.literal("泥土精华块").withStyle(ChatFormatting.DARK_RED))
                                .append(Component.literal("，泥土精华块可以解压成九个泥土精华\n\n"))
                        ),
                        Filterable.passThrough(Component.literal(
                                "V0.2更新：\n\n"+
                                        "1.添加了敌对生物§4土傀儡§r，使用压缩泥土和南瓜头召唤（和雪傀儡一样），会发射泥土球，死亡有概率掉落§4土傀儡头§r\n\n"+
                                        "2.添加了BOSS§4土灵§r，使用压缩泥土和土傀儡头召唤（和铁傀儡一样），初始一段时间无敌，然后飞到天上向玩家发射土傀儡头炸弹，每隔一段时间吸取附近的泥土、"
                        )),
                        Filterable.passThrough(Component.literal(
                                "草方块、压缩泥土，每吸够五个召唤一只土傀儡，死亡掉落泥土破坏者附魔书（暂时）\n\n"
                        )),
                        Filterable.passThrough(Component.literal(
                                "V0.1更新：\n\n"+
                                        "1.一个泥土可以合成§4泥土球§r，泥土球可以投掷，可以当燃料烧炼两个物品\n\n" +
                                        "2.九个泥土可以合成§4压缩泥土§r，压缩泥土可以解压成九个泥土\n\n" +
                                        "3.压缩泥土可以合成§4泥土镐§r，泥土镐对泥土、草方块、压缩泥土的挖掘速度快\n\n"
                        )),
                        Filterable.passThrough(Component.literal(
                                "4.泥土镐专属附魔§4泥土破坏者§r，可以破坏3*3*3范围的泥土、草方块、压缩泥土，可通过附魔台或铁砧获得\n\n" +
                                        "5.压缩泥土可以制作§4泥土护甲§r，满耐久护甲可以烧炼得到一个压缩泥土\n\n" +
                                        "6.新药水§4泥土保护§r，有普通、喷溅型、滞留型三种以及泥土保护之箭，可以获得泥土保护效果，减轻在泥土、"

                        )),
                        Filterable.passThrough(Component.literal(
                                "草方块、压缩泥土上的摔落伤害\n\n" +
                                        "7.新村民职业§4泥土工人§r，工作方块为压缩泥土，可以兑换模组特有物品\n\n" +
                                        "更多内容持续添加中~"))

                ),
                false                                     // resolved
        );

        // 把内容塞进书
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        book.set(DataComponents.ITEM_NAME, Component.literal("看看模组添加了什么?"));

        player.getInventory().addAndPickItem(book);
        player.getPersistentData().putBoolean("dirt_book_given", true);

    }

    @SubscribeEvent
    public static void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        Level level = event.getEntity().level();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if(event.getPlacedBlock().is(Blocks.CARVED_PUMPKIN)){
            spawnDirtGolem(level, pos, state);
        }

        if(event.getPlacedBlock().is(ModBlocks.DIRT_GOLEM_HEAD.get())||event.getPlacedBlock().is(ModBlocks.DIRT_GOLEM_WALL_HEAD.get())){
            spawnDirtBoss(level, pos, state);
        }
    }

    private static void spawnDirtBoss(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;

        // 检测T型结构
        BlockPattern.BlockPatternMatch match = getDirtBossPattern().find(level, pos);
        if (match == null) return;

        // 创建BOSS实体
        EntityDirtBoss boss = ModEntities.DIRT_BOSS.get().create(level, EntitySpawnReason.TRIGGERED);
        if (boss == null) return;

        // 清理结构方块
        clearPatternBlocks(level, match);

        // 计算生成位置
        BlockPos spawnPos = match.getBlock(1, 2, 0).getPos();
        Direction facing = match.getForwards();
        boss.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY() + 0.55,
                spawnPos.getZ() + 0.5,
                facing.getAxis() == Direction.Axis.X ? 0f : 90f,
                0f
        );
        boss.yBodyRot = boss.getYRot();

        // 生成时短暂无敌
        boss.makeInvulnerable();

        // 添加到世界
        level.addFreshEntity(boss);
    }

    private static void spawnDirtGolem(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;

        BlockState block1 = level.getBlockState(pos.below());
        BlockState block2 = level.getBlockState(pos.below().below());
        if (block1.is(ModBlocks.COMPRESSED_DIRT_BLOCK.get()) && block2.is(ModBlocks.COMPRESSED_DIRT_BLOCK.get())) {
            level.removeBlock(pos, false);
            level.removeBlock(pos.below(), false);
            level.removeBlock(pos.below().below(), false);

            EntityDirtGolem dirtGolem = new EntityDirtGolem(ModEntities.DIRT_GOLEM.get(), level);
            dirtGolem.setPos(pos.getX(), pos.below().below().getY(), pos.getZ());
            level.addFreshEntity(dirtGolem);
        }

    }

    private static BlockPattern getDirtBossPattern() {
        return BlockPatternBuilder.start()
                .aisle(" ^ ",
                        "###",
                        "~#~")
                .where('^',  BlockInWorld.hasState(BlockStatePredicate.forBlock(ModBlocks.DIRT_GOLEM_HEAD.get()).or(BlockStatePredicate.forBlock(ModBlocks.DIRT_GOLEM_WALL_HEAD.get()))))
                .where('#',  (blockInWorld) -> blockInWorld.getState().is(ModBlocks.COMPRESSED_DIRT_BLOCK.get()))
                .where('~',  (blockInWorld) -> blockInWorld.getState().isAir())
                .build();
    }
    private static void clearPatternBlocks(Level level, BlockPattern.BlockPatternMatch match) {
        for (int x = 0; x < match.getWidth(); x++) {
            for (int y = 0; y < match.getHeight(); y++) {
                for (int z = 0; z < match.getDepth(); z++) {
                    BlockInWorld biw = match.getBlock(x, y, z);
                    if (biw != null && !biw.getState().isAir()) {
                        level.removeBlock(biw.getPos(), false);
                    }
                }
            }
        }
    }

}
