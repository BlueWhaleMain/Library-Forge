package com.bluewhalemain.library;

import com.bluewhalemain.library.common.Items;
import com.bluewhalemain.library.common.Recipes;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Library入口类
 *
 * @author BlueWhaleMain
 * @version 2021/06/17
 * @since 2021/06/14
 */
@Mod(Constants.Library.id)
public class Library {
    // 保留这个日志记录器，总有一天会有用
//    private static final Logger LOGGER = LogManager.getLogger();

    public Library() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.COMMON_SPEC);
        IEventBus iEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        iEventBus.addListener(this::setup);
        iEventBus.addListener(this::enqueueIMC);
        iEventBus.addListener(this::processIMC);
        iEventBus.addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 初始化
     */
    private void setup(final FMLCommonSetupEvent event) {
    }

    /**
     * 仅客户端
     */
    private void doClientStuff(final FMLClientSetupEvent event) {
    }

    /**
     * IMC mod间通讯 发送
     */
    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    /**
     * IMC mod间通讯 接收
     */
    private void processIMC(final InterModProcessEvent event) {
    }

    /**
     * 服务器启动
     */
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        Recipes.disableRecipes(event.getServer());
    }

    /**
     * 服务器启动完毕
     */
    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        /**
         * 注册方块
         */
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
        }

        /**
         * 注册物品
         */
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            Items.registerItems(itemRegistryEvent);
        }
    }
}
