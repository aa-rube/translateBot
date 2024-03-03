package app.translater.model;

import app.translater.util.DeeplFormatter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConstructorGroupMediaMessage {
    @Autowired
    private DeeplFormatter deeplFormatter;

    private SendMediaGroup getSendMediaGroupMsg(Long chatId, List<InputMedia> media) {
        SendMediaGroup msg = new SendMediaGroup();
        msg.setChatId(chatId);
        msg.setMedias(media);
        return msg;
    }

    private SendAudio getSendAudioMsg(Long chatId, String fileId, String caption, InlineKeyboardMarkup markup) {
        SendAudio msg = new SendAudio();
        msg.setChatId(chatId);
        msg.setAudio(new InputFile(fileId));
        msg.setCaption(caption);
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        return msg;
    }

    private SendDocument getSendDocumentMsg(Long chatId, String fileId, String caption, InlineKeyboardMarkup markup) {
        SendDocument msg = new SendDocument();
        msg.setChatId(chatId);
        msg.setDocument(new InputFile(fileId));
        msg.setCaption(caption);
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        return msg;
    }

    private SendPhoto getSendPhoto(Long chatId, String fileId, String caption, InlineKeyboardMarkup markup) {
        SendPhoto msg = new SendPhoto();
        msg.setChatId(chatId);
        msg.setCaption(caption);
        msg.setPhoto(new InputFile(fileId));
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        return msg;
    }

    private SendVideo getSendVideo(Long chatId, String fileId, String caption, InlineKeyboardMarkup markup) {
        SendVideo msg = new SendVideo();
        msg.setChatId(chatId);
        msg.setCaption(caption);
        msg.setParseMode(ParseMode.HTML);
        msg.setVideo(new InputFile(fileId));
        msg.setReplyMarkup(markup);
        return msg;
    }

    public SendMediaGroup getMediaGroupMessage(MediaGroupData data) {
        List<InputMedia> media = getInputMedia(data);

        if (data.getCaption() != null) {
            try {
                String text = data.getHead() + deeplFormatter.getTextAndEntities(data.getDeeplKey(),
                        data.getCaption(), data.getEntities(), data.getDirection());
                media.get(0).setCaption(text);
                media.get(0).setParseMode(ParseMode.HTML);

                return getSendMediaGroupMsg(data.getChatIdToSendMsg(), media);
            }catch (Exception e) {
                e.printStackTrace();
                String text = data.getHead() + deeplFormatter.plainText(data.getDeeplKey(),
                        data.getCaption(), data.getDirection());
                media.get(0).setCaption(text);
                return getSendMediaGroupMsg(data.getChatIdToSendMsg(), media);
            }
        }
        media.get(0).setCaption(data.getHead());
        return getSendMediaGroupMsg(data.getChatIdToSendMsg(), media);
    }

    @NotNull
    private static List<InputMedia> getInputMedia(MediaGroupData data) {
        List<InputMedia> media = new ArrayList<>();

        for (String fileId : data.getListPhotoFilesId()) {
            InputMedia inputMedia = new InputMediaPhoto();
            inputMedia.setMedia(fileId);
            media.add(inputMedia);
        }

        for (String fileId : data.getListVideoFilesId()) {
            InputMedia inputMedia = new InputMediaVideo();
            inputMedia.setMedia(fileId);
            media.add(inputMedia);
        }

        for (String fileId : data.getListAudioFilesId()) {
            InputMedia inputMedia = new InputMediaAudio();
            inputMedia.setMedia(fileId);
            media.add(inputMedia);
        }

        for (String fileId : data.getListDocumentsFilesId()) {
            InputMedia inputMedia = new InputMediaDocument();
            inputMedia.setMedia(fileId);
            media.add(inputMedia);
        }
        return media;
    }

    public SendPhoto getMsgWithOnePhotoAndCaption(String authKey, Long chatIdToSendMsg, Update update, String direction, String head) {
        String fileId = update.getMessage().getPhoto().get(0).getFileId();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        try {
            markup = update.getMessage().getReplyMarkup();
            for(var s : markup.getKeyboard()) {
                for (var ss : s) {
                    String keyText = deeplFormatter.plainText(authKey, ss.getText().trim(), direction);
                    ss.setText(keyText);

                }
            }
        } catch (Exception e) {

        }

        if (update.getMessage().getCaption() != null && update.getMessage().getCaptionEntities() != null) {
            String text = head +  deeplFormatter.getTextAndEntities(authKey, update.getMessage().getCaption(),
                    update.getMessage().getCaptionEntities(), direction);
            return getSendPhoto(chatIdToSendMsg, fileId, text, markup);
        }

        if (update.getMessage().getCaption() != null && update.getMessage().getCaptionEntities() == null) {
            String text = head + deeplFormatter.plainText(authKey, update.getMessage().getCaption(), direction);
            return getSendPhoto(chatIdToSendMsg, fileId, text, markup);
        }

        return getSendPhoto(chatIdToSendMsg, fileId, null, markup);
    }


    public SendVideo getMsgWithOneVideoAndCaption(String authKey, Long chatIdToSendMsg, Update update, String direction, String head) {
        String fileId = update.getMessage().getVideo().getFileId();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        try {
            markup = update.getMessage().getReplyMarkup();
            for(var s : markup.getKeyboard()) {
                for (var ss : s) {
                    String keyText = deeplFormatter.plainText(authKey, ss.getText().trim(), direction);
                    ss.setText(keyText);
                }
            }
        } catch (Exception e) {

        }

        if (update.getMessage().getCaption() != null && update.getMessage().getCaptionEntities() != null) {
            String text = head + deeplFormatter.getTextAndEntities(authKey, update.getMessage().getCaption(),
                    update.getMessage().getCaptionEntities(), direction);
            return getSendVideo(chatIdToSendMsg, fileId, text, markup);
        }

        if (update.getMessage().getCaption() != null && update.getMessage().getCaptionEntities() == null) {
            String text = head + deeplFormatter.plainText(authKey, update.getMessage().getCaption(), direction);
            return getSendVideo(chatIdToSendMsg, fileId, text, markup);
        }

        return getSendVideo(chatIdToSendMsg, fileId, null, markup);
    }

    public SendDocument getMsgWithOneDocumentAndCaption(String authKey, Long chatIdToSendMsg,
                                                        Update update, String direction, String head) {
        String fileId = update.getMessage().getDocument().getFileId();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        try {
            markup = update.getMessage().getReplyMarkup();
            for(var s : markup.getKeyboard()) {
                for (var ss : s) {
                    String keyText = deeplFormatter.plainText(authKey, ss.getText().trim(), direction);
                    ss.setText(keyText);
                }
            }
        } catch (Exception e) {

        }

        if (update.getMessage().getCaption() != null && update.getMessage().getCaptionEntities() != null) {
            String text = head+  deeplFormatter.getTextAndEntities(authKey, update.getMessage().getCaption(),
                    update.getMessage().getCaptionEntities(), direction);
            return getSendDocumentMsg(chatIdToSendMsg, fileId, text, markup);
        }

        if (update.getMessage().getCaption() != null && update.getMessage().getCaptionEntities() == null) {
            String text = head + deeplFormatter.plainText(authKey, update.getMessage().getCaption(), direction);
            return getSendDocumentMsg(chatIdToSendMsg, fileId, text, markup);
        }

        return getSendDocumentMsg(chatIdToSendMsg, fileId, null, markup);
    }

    public SendAudio getMsgWithOneAudioAndCaption(String authKey, Long chatIdToSendMsg,
                                                  Update update, String direction, String head) {
        String fileId = update.getMessage().getDocument().getFileId();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        try {
            markup = update.getMessage().getReplyMarkup();
            for(var s : markup.getKeyboard()) {
                for (var ss : s) {
                    String keyText = deeplFormatter.plainText(authKey, ss.getText().trim(), direction);
                    ss.setText(keyText);
                }
            }
        } catch (Exception e) {

        }

        if (update.getMessage().getCaption() != null && update.getMessage().getCaptionEntities() != null) {
            String text = head + deeplFormatter.getTextAndEntities(authKey, update.getMessage().getCaption(),
                    update.getMessage().getCaptionEntities(), direction);
            return getSendAudioMsg(chatIdToSendMsg, fileId, text, markup);
        }

        if (update.getMessage().getCaption() != null && update.getMessage().getCaptionEntities() == null) {
            String text = head + deeplFormatter.plainText(authKey, update.getMessage().getCaption(), direction);
            return getSendAudioMsg(chatIdToSendMsg, fileId, text, markup);
        }

        return getSendAudioMsg(chatIdToSendMsg, fileId, null, markup);
    }
}
