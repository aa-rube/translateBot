package app.bot.util;

import app.bot.controller.ChatController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class MessageExecutor {

    private final ChatController chat;

    @Autowired
    public MessageExecutor(ChatController chat) {
        this.chat = chat;
    }

    public void sendMessage(Object msg) {
        try {
            chat.executeAsync((SendMessage) msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void editMessage(Object msg) {
        try {
            chat.executeAsync((EditMessageText) msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendCallBackAnswer(AnswerCallbackQuery callbackQueryAnswer) {
        try {
            chat.executeAsync(callbackQueryAnswer);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
