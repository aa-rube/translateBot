package app.bot.admin;

import app.bot.groupInfo.Flag;
import app.bot.groupInfo.ListenChatData;
import app.bot.groupInfo.SendChatData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.HashMap;

@Service
public class AdminMessage {
    @Autowired
    private AdminKeyboard adminKeyboard;
    @Autowired
    private Flag flag;


    private SendMessage getSendMessage(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage msg = new SendMessage();
        msg.setText(text);
        msg.setChatId(chatId);
        msg.setReplyMarkup(markup);
        msg.setParseMode(ParseMode.HTML);
        return msg;
    }

    public SendMessage getNullStartMessage(Long superUserChatId) {
        String text = "Небходимо добавить хотя бы одну пару групп.";
        return getSendMessage(superUserChatId, text, adminKeyboard.getNullGroupsMsgKeyboards());
    }

    public SendMessage getOfferToAdd(Long superUserChatId) {
        String text = " Добавьте чат для прослушивания и четыре группы для отправки и перевода сообщений.";
        return getSendMessage(superUserChatId, text, adminKeyboard.getNullGroupsMsgKeyboards());
    }

    public SendMessage getChatIdMsg(Long chatId) {
        String text = "Ваш чат ID :\n"
                + "<code>" + chatId + "</code>";

        return getSendMessage(chatId, text, null);
    }

    private String maskApiKey(String apiKey) {
        if (apiKey.length() <= 6) {
            return apiKey;
        }
        String maskedPart = apiKey.substring(apiKey.length() - 6);

        StringBuilder maskedKey = new StringBuilder();
        for (int i = 0; i < apiKey.length() - 6; i++) {
            maskedKey.append("*");
        }
        return maskedKey + maskedPart;
    }

    public SendMessage getAllEntities(Long superUserChatId, ListenChatData value, HashMap<Long, SendChatData> sendToChatData) {
        StringBuilder builder = new StringBuilder();
        builder.append("Флаг: ").append(flag.getFlag(value.getCountryCode().toUpperCase())).append("\n\n")
                .append("Слушаем: ").append(value.getListenTheGroup()).append("\n\n");

        for (Long l : value.getAllChatIdToSend()) {
            if (l == null) continue;

            builder.append("Отправляем: ").append(sendToChatData.get(l).getSendToChatId()).append("\n")
                    .append("Переводим на: ").append(sendToChatData.get(l).getDirection())
                    .append("\nApi ключ: ").append(maskApiKey(sendToChatData.get(l).getKey()).substring(25)).append("\n\n");

        }

        return getSendMessage(superUserChatId, builder.toString(),
                adminKeyboard.mainMenuKeyboard(value.getListenTheGroup()));
    }
}