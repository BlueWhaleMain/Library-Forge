package com.bluewhalemain.library.common;

import com.bluewhalemain.library.Constants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 资源加载器-原生 支持文件夹和jar
 *
 * @author BlueWhaleMain
 * @version 2021/6/19
 * @since 2021/06/17
 */
public class ResourceLoader {
    // 保留这个日志记录器，总有一天会有用
//    private static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        try {
            ResourceLoader.loader(resource -> {
                Recipes.accept(resource);
                LootTables.accept(resource);
            });
        } catch (IOException e) {
            throw new RuntimeException("读取自身文件失败", e);
        }
    }

    private static void push(Consumer<Resource> consumer, File[] files) throws FileNotFoundException {
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    push(consumer, file.listFiles());
                } else if (file.isFile()) {
                    consumer.accept(new Resource(file.getAbsolutePath(),
                            new FileInputStream(file.getAbsolutePath())));
                }
            }
        }
    }

    private static void loader(Consumer<Resource> consumer) throws IOException {
        String beacon = "pack.mcmeta";
        Pattern folderPatten = Pattern.compile("file:/(.*)/" + beacon);
        URL url = ResourceLoader.class.getClassLoader().getResource(beacon);
        assert url != null;
        String urlString = url.toString();
        Matcher matcher = folderPatten.matcher(urlString);
        if (urlString.contains(Constants.Symbol.packSpit)) {
            List<ModInfo> mods = ModList.get().getMods();
            for (ModInfo mod : mods) {
                if (mod.getModId().equals(Constants.Library.id)) {
                    ModFileInfo modFileInfo = mod.getOwningFile();
                    JarFile jarFile = new JarFile(modFileInfo.getFile().getFilePath().toString());
                    Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                    while (jarEntryEnumeration.hasMoreElements()) {
                        JarEntry jarEntry = jarEntryEnumeration.nextElement();
                        if (!jarEntry.isDirectory()) {
                            consumer.accept(new Resource(jarEntry.getName(), jarFile.getInputStream(jarEntry)));
                        }
                    }
                    break;
                }
            }
        } else if (matcher.find()) {
            push(consumer, new File(matcher.group(1)).listFiles());
        } else {
            throw new IllegalStateException("无法加载资源");
        }
    }

    /**
     * 资源类
     */
    static class Resource {
        /**
         * 名称
         */
        private final String name;
        /**
         * 输入流
         */
        private final InputStream inputStream;

        Resource(String name, InputStream inputStream) {
            this.name = name;
            this.inputStream = inputStream;
        }

        public String getName() {
            return name;
        }

        public InputStream getInputStream() {
            return inputStream;
        }
    }
}
