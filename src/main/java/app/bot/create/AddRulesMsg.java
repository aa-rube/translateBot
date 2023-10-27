package app.bot.create;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class AddRulesMsg {
    private final String ok = "успешно добавлен✅\n\n";

    public SendMessage getSendMsg(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.enableHtml(true);
        msg.setParseMode(ParseMode.HTML);
        msg.setText(text);
        msg.setReplyMarkup(markup);

        return msg;
    }
}
