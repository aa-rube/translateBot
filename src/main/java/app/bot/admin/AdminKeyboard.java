package app.bot.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminKeyboard {
    public InlineKeyboardMarkup getNullGroupsMsgKeyboards() {
        InlineKeyboardMarkup inLineKeyBoard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardMatrix = new ArrayList<>();

        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        InlineKeyboardButton pass = new InlineKeyboardButton();
        pass.setText("Добавить");
        pass.setCallbackData("0");
        firstRow.add(pass);

        keyboardMatrix.add(firstRow);
        inLineKeyBoard.setKeyboard(keyboardMatrix);
        return inLineKeyBoard;
    }

    public InlineKeyboardMarkup mainMenuKeyboard(Long groupChatId) {
        InlineKeyboardMarkup inLineKeyBoard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardMatrix = new ArrayList<>();

        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        InlineKeyboardButton delite = new InlineKeyboardButton();
        delite.setText("Удалить");
        delite.setCallbackData("del_" + groupChatId);
        firstRow.add(delite);

        InlineKeyboardButton edit = new InlineKeyboardButton();
        edit.setText("Изменить");
        edit.setCallbackData("edit_" + groupChatId);
        firstRow.add(edit);

        keyboardMatrix.add(firstRow);
        inLineKeyBoard.setKeyboard(keyboardMatrix);
        return inLineKeyBoard;
    }

    public InlineKeyboardMarkup lastInTheList(Long groupChatId) {
        InlineKeyboardMarkup inLineKeyBoard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardMatrix = new ArrayList<>();

        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        InlineKeyboardButton del = new InlineKeyboardButton();
        del.setText("Удалить");
        del.setCallbackData("del_" + groupChatId);
        firstRow.add(del);

        InlineKeyboardButton edit = new InlineKeyboardButton();
        edit.setText("Изменить");
        edit.setCallbackData("edit_" + groupChatId);
        firstRow.add(edit);

        List<InlineKeyboardButton> newDirectionRow = new ArrayList<>();
        InlineKeyboardButton add = new InlineKeyboardButton();
        add.setText("Добавить группу и язык");
        add.setCallbackData("5");
        newDirectionRow.add(add);


        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        InlineKeyboardButton addGroup = new InlineKeyboardButton();
        addGroup.setText("Добавить новый канал");
        addGroup.setCallbackData("0");
        secondRow.add(addGroup);

        keyboardMatrix.add(firstRow);
        keyboardMatrix.add(newDirectionRow);
        keyboardMatrix.add(secondRow);
        inLineKeyBoard.setKeyboard(keyboardMatrix);
        return inLineKeyBoard;
    }

}
