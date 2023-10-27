package app.bot.groupInfo;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class Header {
    public String createHead(Update update) {
        StringBuilder head = new StringBuilder();
        try {
            return head.append("  <b>").append(update.getMessage().getForwardFromChat().getTitle()).append("</b> | ")
                    .append("<a href=\"" + "https://t.me/").append(update.getMessage().getForwardFromChat().getUserName())
                    .append("/").append(update.getMessage().getForwardFromMessageId())
                    .append("\">").append("message").append("</a>\n").toString();
        } catch (Exception e) {
            return "...";
        }
    }
}
