package app.translater.groupInfo;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class Header {
    private final static StringBuilder head = new StringBuilder();
    public static String createHead(Update update) {
        head.setLength(0);
        try {
            return head.append("  <b>").append(update.getMessage().getForwardFromChat().getTitle()).append("</b> | ")
                    .append("<a href=\"" + "https://t.me/").append(update.getMessage().getForwardFromChat().getUserName())
                    .append("/").append(update.getMessage().getForwardFromMessageId())
                    .append("\">").append("message").append("</a>\n").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "???";
        }
    }
}
