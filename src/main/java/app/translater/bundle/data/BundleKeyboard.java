package app.translater.bundle.data;

import app.bot.data.BotContentData;
import app.translater.bundle.model.Bundle;
import app.translater.bundle.dao.BundleRedisDao;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BundleKeyboard {

    public InlineKeyboardMarkup create() {
        String[] buttonTexts = {"Создать связку"};
        String[] callBackData = {"CREATE"};
        return BotContentData.createInlineKeyboardLine(buttonTexts, callBackData);
    }

    public InlineKeyboardMarkup addBotToSourceChatOrChannel(String botUserName) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineButton = new InlineKeyboardButton();
        inlineButton.setText("Добавить в группу перевода");
        inlineButton.setUrl("tg://resolve?domain=".concat(botUserName).concat("&startgroup=true"));
        markupInline.setKeyboard(List.of(List.of(inlineButton)));
        return markupInline;
    }

    public InlineKeyboardMarkup addBotToTargetChatOrChannel(String botUserName) {
        InlineKeyboardMarkup inLineKeyBoard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardMatrix = new ArrayList<>();

        List<InlineKeyboardButton> chatRow = new ArrayList<>();
        InlineKeyboardButton chat = new InlineKeyboardButton();
        chat.setText("Добавить в целевую группу");
        chat.setUrl("tg://resolve?domain=".concat(botUserName).concat("&startgroup=true"));
        chatRow.add(chat);

        List<InlineKeyboardButton> channelRow = new ArrayList<>();
        InlineKeyboardButton channel = new InlineKeyboardButton();
        channel.setText("Добавить в кана целевой канал");
        channel.setUrl("tg://resolve?domain=".concat(botUserName).concat("&startchannel=true"));
        channelRow.add(channel);

        keyboardMatrix.add(chatRow);
        keyboardMatrix.add(channelRow);

        inLineKeyBoard.setKeyboard(keyboardMatrix);
        return inLineKeyBoard;
    }

    public InlineKeyboardMarkup getBundles(HashMap<String, Bundle> bundles) {
        InlineKeyboardMarkup inLineKeyBoard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardMatrix = new ArrayList<>();

        for (Map.Entry<String, Bundle> entry : bundles.entrySet()) {

            System.out.println(entry.getValue().getNameTo());

            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();

            btn.setText(entry.getValue().getNameTo());
            btn.setCallbackData(BundleRedisDao.KEY + entry.getValue().getFrom());
            row.add(btn);

            keyboardMatrix.add(row);
        }

        keyboardMatrix.add(create().getKeyboard().get(0));
        inLineKeyBoard.setKeyboard(keyboardMatrix);
        return inLineKeyBoard;
    }

    public InlineKeyboardMarkup languages() {
        String[] buttonTexts = {"Spanish", "Italian", "Portuguese", "Ukrainian", "English"};
        String[] callBackData = {"lang=ES", "lang=IT", "lang=PT-PT", "lang=UK", "lang=EN-US"};
        return BotContentData.createVerticalInlineKeyboard(buttonTexts, callBackData);
    }

    public InlineKeyboardMarkup editBundle(String text, String data) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> settingsRow = new ArrayList<>();
        InlineKeyboardButton change = new InlineKeyboardButton();
        change.setText("Изменить");
        change.setSwitchInlineQueryCurrentChat(data + "\n\nВнесите изменения и отправьте этот текст:\n\n"
                .concat(text));
        settingsRow.add(change);

        InlineKeyboardButton delete = new InlineKeyboardButton();
        delete.setText("Удалить");
        delete.setCallbackData("DELETE_" + data);
        settingsRow.add(delete);

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("Закрыть");
        back.setCallbackData("BACK");
        backRow.add(back);

        rowsInline.add(settingsRow);
        rowsInline.add(backRow);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

}