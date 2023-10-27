package app.bot.constructor;

import app.bot.service.Formatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class ConstructorPlainTextMsg {
    @Autowired
    private Formatter Formatter;
    private SendMessage getSendMsg(Long chatId, String txt, InlineKeyboardMarkup markup) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(txt);
        msg.setReplyMarkup(markup);
        msg.enableHtml(true);
        msg.setParseMode(ParseMode.HTML);
        return msg;
    }
    public SendMessage translatePlainText(String authKey, Long chatIdToSendMsg,
                                          Update update, String direction, String head) {
        try {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            markup = update.getMessage().getReplyMarkup();
            String text = head + Formatter.plainText(authKey, update.getMessage().getText(), direction);

            return getSendMsg(chatIdToSendMsg, text, markup);
        } catch (Exception e) {
            String text = head + Formatter.plainText(authKey, update.getMessage().getText(), direction);
            return getSendMsg(chatIdToSendMsg, text, null);
        }
    }

    public SendMessage translateTextWithEntities(String authKey, Long chatIdToSendMsg, Update update, String direction, String head) {
        try {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            markup = update.getMessage().getReplyMarkup();
            String text = head + Formatter.getTextAndEntities(authKey, update.getMessage().getText(),
                    update.getMessage().getEntities(), direction);

            return getSendMsg(chatIdToSendMsg,text, markup);
        } catch (Exception e) {
            String text = head + Formatter.getTextAndEntities(authKey, update.getMessage().getText(),
                    update.getMessage().getEntities(), direction);

            return getSendMsg(chatIdToSendMsg, text, null);
        }
    }
}
