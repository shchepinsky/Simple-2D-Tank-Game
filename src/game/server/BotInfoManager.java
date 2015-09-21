package game.server;

import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Holds computer player status.
 */
class BotInfoManager {
    private final HashSet<BotInfo> botClientInfoSet = new HashSet<>();

    public int getBotCount() {
        return botClientInfoSet.size();
    }

    public void forEach(Consumer<BotInfo> action) {
        botClientInfoSet.forEach(action);
    }

    public void register(BotInfo botInfo) {
        if (botClientInfoSet.contains(botInfo)) {
            throw new IllegalArgumentException(
                    String.format("AI player (%s) is already registered", botInfo.name)
            );
        }

        botClientInfoSet.add(botInfo);
    }
}
