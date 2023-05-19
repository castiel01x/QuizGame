package me.castiel.quizgame;

import lombok.Getter;
import me.castiel.quizgame.commands.QuizCommand;
import me.castiel.quizgame.database.MariaDB;
import me.castiel.quizgame.settings.Settings;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class QuizGame extends JavaPlugin {

    @Getter
    private static QuizGame instance;
    @Getter
    private MariaDB mariaDB;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Settings.init();
        mariaDB = new MariaDB();
        PluginCommand pluginCommand = getCommand("quizgame");
        QuizCommand quizCommand = new QuizCommand();
        pluginCommand.setExecutor(quizCommand);
        pluginCommand.setTabCompleter(quizCommand);
        getLogger().info("QuizGame plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        mariaDB.close();
        getLogger().info("QuizGame plugin has been disabled!");
    }
}
