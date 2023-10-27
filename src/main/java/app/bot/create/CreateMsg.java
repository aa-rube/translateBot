package app.bot.create;

import app.bot.groupInfo.Flag;
import app.bot.groupInfo.SendChatData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class CreateMsg {
    @Autowired
    private Flag flag;
    @Autowired
    private CreateKeyboard keyboard;
    private  final StringBuilder builder = new StringBuilder();

    public SendMessage getSendMsg(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.enableHtml(true);
        msg.setParseMode(ParseMode.HTML);
        msg.setText(text);
        msg.setReplyMarkup(markup);

        return msg;
    }

    public SendMessage zeroStepCreate(Long superUserChatId) {
        builder.setLength(0);
        builder.append("<b>Давайте начнем настройку перевода и рассылки в нужные группы.</b>\n\n")
                .append("Сначала укажите Chat ID группы, которую бот будет слушать.\n\n")
                .append("Чтобы сделать это, добавьте бота в группу, куда вы хотите отправлять сообщения в оригинале. ")
                .append("Убедитесь, что у бота есть доступ к сообщениям - без этого он не сможет работать.\n\n")
                .append("В группе введите команду:\n<code>/get_chat_id</code>. Бот отправит вам Chat ID в ответ. ")
                .append("Его можно скопировать нажатием на текст.");
        return getSendMsg(superUserChatId, builder.toString(), null);
    }

    public SendMessage firstStepCreate(Long userChatId) {
        builder.setLength(0);
        builder.append("✅<b>Chat ID для прослушивания успешно добавлен</b>")
                .append("\n\nТеперь добавим ID чатов, куда будем <b>отправлять</b> наши переведенные сообщения.\n\n")
                .append("Всего можно добавить <b>четыре группы для рассылки из одной группы</b>, которую бот слушает.\n\n")
                .append("Добавьте бота в группу, куда будем отправлять переведенные сообщения.\n")

                .append("Убедитесь, что у бота есть доступ к сообщениям - без этого он не сможет работать.\n\n")
                .append("В группе введите команду:\n<code>/get_chat_id</code>. Бот отправит вам CHAT ID в ответ. ")
                .append("Его можно скопировать нажатием на текст.");
                return getSendMsg(userChatId, builder.toString(), null);
    }

    public SendMessage secondStep(Long superChatId) {
        builder.setLength(0);
        builder.append("✅<b>Chat ID для публикации успешно добавлен</b>\n\n")
                .append("Введи язык для перевода, например <code>RU</code>")
                .append("/<code>EN-GB</code>/<code>EN-US</code>");
        return getSendMsg(superChatId, builder.toString(), null);
    }

    public SendMessage thirdStep(Long superChatId) {
        builder.setLength(0);
        builder.append("✅<b>Язык перевода успешно добавлен</b>\n\n Теперь введите API-ключ Deepl для этого языка. Ключи могут использоваться повторно.");
        return getSendMsg(superChatId, builder.toString(), null);
    }

    String fourthStep = "Нажми кнопку ДОБАВИТЬ, чтобы настроить возможность отправки новостей в другие каналы.\n\n" +
            "Ты можешь использовать любой язык, поддерживаемый Deepl.\n\nЕсли пока не готов настраивать другие каналы и правила рассылок, " +
            "добавь основной флаг группы, для исходных сообщений, а <b>настройкой можно заняться позже</b>." +
            "\n\nДля создания флага отправь две буквы страны в международном фoрмате, например - <code>RU</code> или <code>UA</code>." +
            "После этого все данные сохраняться и можно начать работать с этими группами.";

    public SendMessage useExistingRecord(Long superChatId, int i) {
        builder.setLength(0);
        builder.append("✅<b>Информация о группе успешно добавлена</b>\n\n").append(fourthStep);

        if(i < 0) {
            return getSendMsg(superChatId, builder.toString(), null);
        }

        return getSendMsg(superChatId, builder.toString(), keyboard.addRules(i));
    }

    public SendMessage fourthStep(Long superChatId, int i) {
        builder.setLength(0);
        builder.append("✅<b>КЛЮЧ API DEEPL успешно добавлен</b>\n\n").append(fourthStep);

        if(i < 0) {
            return getSendMsg(superChatId, builder.toString(), null);
        }
        return getSendMsg(superChatId, builder.toString(), keyboard.addRules(i));
    }

    public SendMessage finish(Long superChatId, String countryCode) {
        builder.setLength(0);
        builder.append("Флаг ").append(flag.getFlag(countryCode)).append(" ").append("успешно добавлен\n\n")
                .append("✅<b>Настройка успешно завершена</b>✅");
        return getSendMsg(superChatId, builder.toString(), null);
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

    public SendMessage yourRecordIsExist(Long superChatId, SendChatData sendChatData, int i) {
        builder.setLength(0);
        builder.append("\uD83E\uDE84<b>Запись с таким Chat ID уже существует</b>\n\n")
                .append("язык: ").append(sendChatData.getDirection()).append("\n")
                .append("api key: ").append(maskApiKey(sendChatData.getKey()).substring(25)).append("\n")
                .append("Нажми <b>использовать</b> эти данные.\n\nEсли эти данные нужно <b>изменить</b>, то для продолжения ")
                .append("введи язык для перевода, например <code>EN-GB</code>/<code>EN-US</code>/<code>ES</code>")
                .append(" и отправь сообщение. Там будут дальнейшие инструкции.");
        return getSendMsg(superChatId, builder.toString(), keyboard.useExistingRecord(sendChatData.getSendToChatId(), i));
    }
}