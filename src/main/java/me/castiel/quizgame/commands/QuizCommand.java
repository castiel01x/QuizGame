package me.castiel.quizgame.commands;

import me.castiel.quizgame.QuizGame;
import me.castiel.quizgame.game.Game;
import me.castiel.quizgame.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QuizCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("quizgame.reload")) {
                Settings.sendMessage(sender, Settings.MESSAGES_NO_PERMISSION);
                return true;
            }
            Settings.reload();
            Settings.sendMessage(sender, Settings.MESSAGES_RELOAD);
            return true;
        }

        if (!(sender instanceof Player)) {
            Settings.sendMessage(sender, "%prefix% &cPlease use this command in-game.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length >= 1 && args[0].equalsIgnoreCase("score")) {
            if (args.length == 1) {
                QuizGame.getInstance().getMariaDB().getPlayerScore(player.getName()).whenComplete((score, throwable)
                        -> Settings.sendMessage(player, Settings.MESSAGES_SCORE_DISPLAY.replace("%score%", String.valueOf(score))));
            }
            else {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null && target.isOnline()) {
                    QuizGame.getInstance().getMariaDB().getPlayerScore(target.getName()).whenComplete((score, throwable)
                            -> Settings.sendMessage(player, Settings.MESSAGES_SCORE_DISPLAY_OTHER
                            .replace("%score%", String.valueOf(score))
                            .replace("%player%", target.getName())));
                }
                else {
                    Settings.sendMessage(player, Settings.MESSAGES_NO_PLAYER);
                }
            }
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("start")) {
            Settings.sendMessage(player, new ArrayList<>(Settings.MESSAGES_HELP));
            return true;
        }

        String category = args[1];

        if (Settings.QUESTIONS.keySet().stream().noneMatch(s -> s.equalsIgnoreCase(args[1]))) {
            Settings.sendMessage(player, Settings.MESSAGES_INVALID_CATEGORY.replace("%category%", args[1]));
            return true;
        }

        for (String key : Settings.QUESTIONS.keySet()) {
            if (key.equalsIgnoreCase(category)) {
                category = key;
                break;
            }
        }

        if (!player.hasPermission("quizgame.start")) {
            Settings.sendMessage(player, Settings.MESSAGES_NO_PERMISSION);
            return true;
        }

        List<Settings.Question> questions = Settings.QUESTIONS.get(category);

        if (questions == null || questions.isEmpty()) {
            Settings.sendMessage(player, "%prefix% &cCategory doesn't have any questions %category%".replace("%category%", category));
            return true;
        }

        if (!Game.start(player, category, questions)) {
            Settings.sendMessage(player, "%prefix% &cThere's a game already running.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("start", "reload", "score"));
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            completions.addAll(Settings.QUESTIONS.keySet());
        }

        return completions;
    }
}
