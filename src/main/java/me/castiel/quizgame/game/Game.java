package me.castiel.quizgame.game;

import me.castiel.quizgame.QuizGame;
import me.castiel.quizgame.settings.Settings;
import me.castiel.quizgame.util.num.NumUtils;
import me.castiel.quizgame.util.strings.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Game implements Listener {

    private static Game currentGame;

    private final Settings.Question question;

    private final BukkitTask endGameTask;

    private final Long startTimestamp;

    public Game(String category, Settings.Question question) {
        this.question = question;
        startTimestamp = System.currentTimeMillis();
        QuizGame.getInstance().getServer().getPluginManager().registerEvents(this, QuizGame.getInstance());
        List<String> message = new ArrayList<>(Settings.MESSAGES_GAME_START);
        message.replaceAll(s -> s
                .replace("%question%", question.getQuestion())
                .replace("%category%", category));
        for (Player player : Bukkit.getOnlinePlayers()) {
            Settings.sendMessage(player, message);
        }
        endGameTask = new BukkitRunnable() {
            @Override
            public void run() {
                String message = Settings.MESSAGES_TIMEOUT
                        .replace("%answer%", question.getAnswer());
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Settings.sendMessage(player, message);
                }
                end();
            }
        }.runTaskLater(QuizGame.getInstance(), 30L * 20L);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (question.checkAnswer(ChatColor.stripColor(event.getMessage()))) {
            Settings.sendMessage(event.getPlayer(), Settings.MESSAGES_CORRECT_ANSWER);
            String message = Settings.MESSAGES_CORRECT_ANSWER_BROADCAST
                    .replace("%player%", event.getPlayer().getName())
                    .replace("%time%", StringUtils.format(System.currentTimeMillis() - startTimestamp));
            for (Player player : Bukkit.getOnlinePlayers()) {
                Settings.sendMessage(player, message);
            }
            QuizGame.getInstance().getMariaDB().insertScore(event.getPlayer().getName(), 1);
            if (!endGameTask.isCancelled()) {
                endGameTask.cancel();
            }
            end();
        }
    }

    public void end() {
        HandlerList.unregisterAll(this);
        Game.currentGame = null;
    }

    public static boolean start(Player player, String category, List<Settings.Question> questions) {
        if (currentGame != null) {
            return false;
        }
        currentGame = new Game(category, NumUtils.randomFromList(questions, 1).get(0));
        return true;
    }
}
