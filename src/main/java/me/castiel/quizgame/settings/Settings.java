package me.castiel.quizgame.settings;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import me.castiel.quizgame.QuizGame;
import me.castiel.quizgame.util.strings.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings {

    public static String DATABASE_JDBC;
    public static String DATABASE_HOST;
    public static String DATABASE_PORT;
    public static String DATABASE_USERNAME;
    public static String DATABASE_PASSWORD;
    public static String DATABASE_NAME;

    public static String MESSAGES_PREFIX;
    public static String MESSAGES_NO_PERMISSION;
    public static String MESSAGES_NO_PLAYER;
    public static String MESSAGES_INVALID_CATEGORY;
    public static List<String> MESSAGES_HELP;
    public static String MESSAGES_RELOAD;
    public static String MESSAGES_SCORE_DISPLAY;
    public static String MESSAGES_SCORE_DISPLAY_OTHER;
    public static List<String> MESSAGES_GAME_START;
    public static String MESSAGES_CORRECT_ANSWER;
    public static String MESSAGES_CORRECT_ANSWER_BROADCAST;
    public static String MESSAGES_TIMEOUT;

    public static Map<String, List<Question>> QUESTIONS;

    public static void init() {
        FileConfiguration config = QuizGame.getInstance().getConfig();
        DATABASE_JDBC = config.getString("Database.URL");
        DATABASE_HOST = config.getString("Database.Host");
        DATABASE_PORT = config.getString("Database.Port");
        DATABASE_USERNAME = config.getString("Database.Username");
        DATABASE_PASSWORD = config.getString("Database.Password");
        DATABASE_NAME = config.getString("Database.Database");

        // Load messages
        MESSAGES_PREFIX = config.getString("Messages.Prefix");
        MESSAGES_NO_PERMISSION = config.getString("Messages.NoPermission");
        MESSAGES_NO_PLAYER = config.getString("Messages.NoPlayer");
        MESSAGES_INVALID_CATEGORY = config.getString("Messages.InvalidCategory");
        MESSAGES_HELP = config.getStringList("Messages.Help");
        MESSAGES_RELOAD = config.getString("Messages.Reload");
        MESSAGES_SCORE_DISPLAY = config.getString("Messages.ScoreDisplay");
        MESSAGES_SCORE_DISPLAY_OTHER = config.getString("Messages.ScoreDisplayOther");
        MESSAGES_GAME_START = config.getStringList("Messages.GameStart");
        MESSAGES_CORRECT_ANSWER = config.getString("Messages.CorrectAnswer");
        MESSAGES_CORRECT_ANSWER_BROADCAST = config.getString("Messages.CorrectAnswerBroadcast");
        MESSAGES_TIMEOUT = config.getString("Messages.Timeout");

        // Load questions
        QUESTIONS = new HashMap<>();
        ConfigurationSection categoriesSection = config.getConfigurationSection("Categories");
        for (String category : categoriesSection.getKeys(false)) {
            List<Question> categoryQuestions = new ArrayList<>();
            ConfigurationSection questionsSection = categoriesSection.getConfigurationSection(category + ".Questions");
            for (String questionNumber : questionsSection.getKeys(false)) {
                String questionText = questionsSection.getString(questionNumber + ".Question");
                List<String> answers = questionsSection.getStringList(questionNumber + ".Answers");
                categoryQuestions.add(new Question(questionText, answers));
            }
            QUESTIONS.put(category, categoryQuestions);
        }
    }

    public static void reload() {
        QuizGame.getInstance().reloadConfig();
        init();
    }

    public static class Question {

        private final String question;
        private final List<String> answers;

        public Question(String question, List<String> answers) {
            this.question = question;
            this.answers = answers;
        }

        public boolean checkAnswer(String answer) {
            for (String s : answers) {
                if (s.equalsIgnoreCase(answer.trim())) {
                    return true;
                }
            }
            return false;
        }

        public String getAnswer() {
            if (!answers.isEmpty()) {
                return answers.get(0);
            }
            return "None Set";
        }

        public String getQuestion() {
            return question;
        }
    }

    public static void sendMessage(CommandSender commandSender, String message) {
        if (message == null || message.length() == 0) {
            return;
        }
        String msg = message.replace("%prefix%", MESSAGES_PREFIX);
        msg = IridiumColorAPI.process(msg.replace("%player%", commandSender.getName()));
        commandSender.sendMessage(StringUtils.color(msg));
    }

    public static void sendMessage(CommandSender commandSender, List<String> message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        List<String> msg = new ArrayList<>(message);
        msg.replaceAll(s -> s.replace("%prefix%", MESSAGES_PREFIX));
        msg.replaceAll(s -> StringUtils.color(s.replace("%player%", commandSender.getName())));
        msg = IridiumColorAPI.process(msg);
        msg.forEach(commandSender::sendMessage);
    }
}
