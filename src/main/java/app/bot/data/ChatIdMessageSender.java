package app.bot.data;

import app.bot.util.MessageExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
@Service
public class ChatIdMessageSender {
    @Autowired
    @Lazy
    private MessageExecutor msgExecutor;

    public boolean sendChatIdMsg(Update update) {
        if (update.hasCallbackQuery()) return false;

        try {
            if (update.hasChannelPost()
                    && update.getChannelPost().hasText()
                    && update.getChannelPost().getText().equals("/get_chat_id")) {
                msgExecutor.sendMessage(getChatIdMsg(update.getChannelPost().getChatId()));
                return true;
            }

            if (update.hasMessage()
                    && update.getMessage().hasText()
                    && update.getMessage().getText().equals("/get_chat_id")) {
                msgExecutor.sendMessage(getChatIdMsg(update.getMessage().getChatId()));
                return true;
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    private SendMessage getChatIdMsg(Long chatId) {
        return (SendMessage) Messages.getSendMessage(chatId, "Ваш chatId: <code>".
                        concat(String.valueOf(chatId).concat("</code>")),
                null);
    }
}
