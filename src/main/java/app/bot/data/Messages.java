package app.bot.data;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class Messages {
    public static Object getSendMessage(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.enableHtml(true);
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        return msg;
    }

    public static Object getEditMessage(Long chatId, String text, InlineKeyboardMarkup markup, int msgId) {
        EditMessageText msg = new EditMessageText();
        msg.setChatId(chatId);
        msg.setMessageId(msgId);
        msg.setText(text);
        msg.enableHtml(true);
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        return msg;
    }

    public static List<Object> getWrongMessage(Long chatId) {
        List<Object> messages = new ArrayList<>();
        messages.add(getSendMessage(chatId, "something went wrong", null));
        return messages;
    }

    public static Object getDeleteMessage(Long chatId, int msgId) {
        DeleteMessage delete = new DeleteMessage();
        delete.setChatId(chatId);
        delete.setMessageId(msgId);
        return delete;
    }

    public static AnswerCallbackQuery getCallbackQueryAnswer(Update update) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        return answer;
    }

    public static InlineKeyboardMarkup createInlineKeyboardLine(String[] buttonTexts, String[] callbackData) {
        InlineKeyboardMarkup inLineKeyBoard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardMatrix = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int i = 0; i < buttonTexts.length; i++) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(buttonTexts[i]);
            btn.setCallbackData(callbackData[i]);
            row.add(btn);
        }

        keyboardMatrix.add(row);
        inLineKeyBoard.setKeyboard(keyboardMatrix);
        return inLineKeyBoard;
    }
    public static InlineKeyboardMarkup createVerticalInlineKeyboard(String[] buttonTexts, String[] callbackData) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (int i = 0; i < buttonTexts.length; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonTexts[i]);
            button.setCallbackData(callbackData[i]);
            row.add(button);
            keyboardRows.add(row);
        }

        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        return inlineKeyboardMarkup;
    }
}
