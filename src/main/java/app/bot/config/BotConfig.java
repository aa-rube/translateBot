package app.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {
    @Value("${bot.username}")
    String botUsername;
    @Value("${bot.token}")
    String botToken;
    @Value("${bot.super.user}")
    Long superUserChatId;

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotToken() {
        return botToken;
    }

    public Long getSuperUserChatId() {
        return superUserChatId;
    }
}