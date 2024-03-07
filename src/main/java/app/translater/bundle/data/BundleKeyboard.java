package app.translater.bundle.data;

import app.bot.data.BotContentData;
import app.translater.bundle.model.Bundle;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

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

    public InlineKeyboardMarkup getBundles(List<Bundle> bundles) {
        InlineKeyboardMarkup inLineKeyBoard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardMatrix = new ArrayList<>();

        for (Bundle b : bundles) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();

            btn.setText(b.getNameFrom());
            btn.setCallbackData("bundles:" + b.getFrom());
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

    public InlineKeyboardMarkup editBundle(String text, Long from, String botUserName) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> settingsRow = new ArrayList<>();
        InlineKeyboardButton change = new InlineKeyboardButton();
        change.setText("Изменить");
        change.setSwitchInlineQueryCurrentChat(text);
        settingsRow.add(change);

        InlineKeyboardButton delete = new InlineKeyboardButton();
        delete.setText("Удалить");
        delete.setCallbackData("DELETE_" + from);
        settingsRow.add(delete);

        List<InlineKeyboardButton> chatRow = new ArrayList<>();
        InlineKeyboardButton chat = new InlineKeyboardButton();
        chat.setText("Добавить в целевую группу");
        chat.setUrl("tg://resolve?domain=".concat(botUserName).concat("&startgroup=true"));
        chatRow.add(chat);

        List<InlineKeyboardButton> channelRow = new ArrayList<>();
        InlineKeyboardButton channel = new InlineKeyboardButton();
        channel.setText("Добавить в целевой канал");
        channel.setUrl("tg://resolve?domain=".concat(botUserName).concat("&startchannel=true"));
        channelRow.add(channel);

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("Закрыть");
        back.setCallbackData("BACK");
        backRow.add(back);

        rowsInline.add(settingsRow);
        rowsInline.add(chatRow);
        rowsInline.add(channelRow);
        rowsInline.add(backRow);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

}