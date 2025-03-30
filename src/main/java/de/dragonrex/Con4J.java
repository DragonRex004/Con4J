package de.dragonrex;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Con4J {
    private final String id;
    private Path FOLDER_PATH;
    private Consumer<PacketCtx> onReceive;
    private boolean read = false;
    private List<Path> subChannels = new ArrayList<>();

    public Con4J(String folderPath, String id) {
        this.id = id;
        try {
            this.onReceive = (input) -> {
            };
            FOLDER_PATH = Paths.get(folderPath);
            if (!Files.exists(FOLDER_PATH)) {
                Files.createDirectories(FOLDER_PATH);
            }
            Thread watcherThread = new Thread(() -> watchFolder());
            watcherThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSubChannel(String folderPath) {
        try {
            Path temp = Paths.get(FOLDER_PATH + "/" + folderPath);
            if (!Files.exists(temp)) {
                Files.createDirectories(temp);
            }
            WatchService watchService = FileSystems.getDefault().newWatchService();
            temp.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            this.subChannels.add(temp);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onReceive(Consumer<PacketCtx> input) {
        this.onReceive = input;
    }

    private void watchFolder() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            FOLDER_PATH.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if(!read) {
                        Path filePath = FOLDER_PATH.resolve((Path) event.context());
                        if (!new File(String.valueOf(filePath)).getName().startsWith(this.id)) {
                            String ctx = Files.readString(filePath);
                            String id = filePath.toFile().getName().replace(".con4j", "");
                            Files.deleteIfExists(filePath);
                            this.onReceive.accept(new PacketCtx(id, FOLDER_PATH.toString(), ctx));
                        }
                        if(!this.subChannels.isEmpty()) {
                            for (Path channel : this.subChannels) {
                                Path channelPath = channel.resolve((Path) event.context());
                                if (!new File(String.valueOf(channelPath)).getName().startsWith(this.id)) {
                                    String ctx = Files.readString(channelPath);
                                    String id = channelPath.toFile().getName().replace(".con4j", "");
                                    Files.deleteIfExists(channelPath);
                                    this.onReceive.accept(new PacketCtx(id, channelPath.toString(), ctx));
                                }
                            }
                        }
                        read = true;
                    } else {
                        read = false;
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writeFile(String content, String channel) {
        try {
            for (Path ch : this.subChannels) {
                if(ch.toString().equals(channel)) {
                    Path chFilePath = Paths.get(ch.toString(), this.id + ".con4j");
                    Files.write(chFilePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
            }
            Path filePath = Paths.get(FOLDER_PATH.toString(), this.id + ".con4j");
            Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
