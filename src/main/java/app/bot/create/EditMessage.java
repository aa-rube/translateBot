package app.bot.create;

import app.bot.groupInfo.Flag;
import app.bot.groupInfo.ListenChatData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class EditMessage {
    @Autowired
    private Flag flag;
    @Autowired
    private CreateKeyboard createKeyboard;

    private final StringBuilder builder = new StringBuilder();

    public SendMessage getSendMessage(Long chatId, String tetx, InlineKeyboardMarkup markup) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(tetx);
        msg.setReplyMarkup(markup);
        msg.enableHtml(true);
        msg.setParseMode(ParseMode.HTML);
        return msg;
    }
    public SendMessage startMessage(Long chatId, Long recordId, ListenChatData listenChatData) {
        builder.setLength(0);
        String f = flag.getFlag(listenChatData.getCountryCode());
        recordId = recordId * (-1);

        builder.append("Сейчас мы можем изменить данные для прослушивания группы Chat ID: \n")
                .append("<code>").append(recordId * (-1)).append("</code> \n")

                .append("Флаг:").append(f).append(f).append(f).append(f).append(f).append("\n")
                .append("/changeFlag_").append(recordId).append(" \n\n")

                .append("Отправляем в группы: \n");
        int i = 0;
        if (listenChatData.getSendToTheGroup1() != null) {
            i++;
            builder.append("Chat ID: <code>").append(listenChatData.getSendToTheGroup1()).append("</code>\n")
                    .append("Удалить правило: /deleteRule_").append(recordId).append("_").append(1).append(" \n\n");
        }

        if (listenChatData.getSendToTheGroup2() != null) {
            i++;
            builder.append("Chat ID: <code>").append(listenChatData.getSendToTheGroup1()).append("</code>\n")
                    .append("Удалить правило: /deleteRule_").append(recordId).append("_").append(2).append(" \n\n");
        }
        if (listenChatData.getSendToTheGroup3() != null) {
            i++;
            builder.append("Chat ID: <code>").append(listenChatData.getSendToTheGroup1()).append("</code>\n")
                    .append("Удалить правило: /deleteRule_").append(recordId).append("_").append(3).append(" \n\n");
        }
        if (listenChatData.getSendToTheGroup4() != null) {
            i++;
            builder.append("Chat ID: <code>").append(listenChatData.getSendToTheGroup1()).append("</code>\n")
                    .append("Удалить правило: /deleteRule_").append(recordId).append("_").append(4).append(" \n\n");
        }

        if (i != 4) {
            return getSendMessage(chatId, builder.toString(), createKeyboard.addRules(i));

        }

        return getSendMessage(chatId, builder.toString(), null);
    }
}
