package app.bot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class BotConfig {
    @Value("${bot.username}")
    String botUsername;
    @Value("${bot.token}")
    String botToken;
    @Value("${bot.super.user}")
    Long superUserChatId;
}