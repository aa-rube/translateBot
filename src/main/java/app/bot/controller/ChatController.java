package app.bot.controller;

import app.bot.config.BotConfig;
import app.translater.bundle.model.Bundle;
import app.translater.bundle.model.Target;
import app.translater.bundle.service.RedisBundleService;
import app.translater.groupInfo.Header;
import app.translater.bundle.BundleController;
import app.translater.util.ConstructorGroupMediaMessage;
import app.translater.util.ConstructorPlainTextMsg;
import app.translater.model.MediaGroupData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.time.LocalDateTime;
import java.util.*;

@Controller
public class ChatController extends TelegramLongPollingBot {
    @Autowired
    private BotConfig botConfig;

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (bundleController.handleUpdate(update)) return;


        if (!update.hasMessage()) return;
        sender(update);
    }

    @Autowired
    private BundleController bundleController;
    @Autowired
    private RedisBundleService service;
    @Autowired
    private ConstructorPlainTextMsg plainTextMsg;
    @Autowired
    private ConstructorGroupMediaMessage groupMediaMessage;
    private final HashMap<String, MediaGroupData> groupsMessages = new HashMap<>();

    @Scheduled(fixedRate = 2000)
    public void sendGroupMessage() {
        List<String> idList = new ArrayList<>();

        for (Map.Entry<String, MediaGroupData> data : groupsMessages.entrySet()) {
            String id = data.getKey();
            LocalDateTime plusTime = data.getValue().getLastUpdate().plusSeconds(5);
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(plusTime)) {
                sendGroupMediaMsg(groupMediaMessage.getMediaGroupMessage(data.getValue()));
                idList.add(id);
            }
        }

        for (String id : idList) {
            groupsMessages.remove(id);
        }
        idList.clear();
    }

    private void sender(Update update) {
        Long from = update.getMessage().getChat().getId();
        Bundle bundleOpt = service.getBundle(from);
        if (bundleOpt == null) return;

        for (Target target : bundleOpt.getTargetGroupList()) {
            String head = target.getFlag().concat(Header.createHead(update));
            forwardTranslatedMsg(update, target.getDeeplApiKey(), target.getChatId(), target.getLang(), head);
        }
    }

    public void forwardTranslatedMsg(Update update, String key, Long chatIdToSendMsg, String direction, String head) {
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getEntities() == null) {

            sendMessage(plainTextMsg.translatePlainText(key, chatIdToSendMsg, update, direction, head));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getEntities() != null) {
            sendMessage(plainTextMsg.translateTextWithEntities(key, chatIdToSendMsg, update,
                    direction, head));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasPhoto() && update.getMessage().getMediaGroupId() == null) {
            sendPhotoMsg(groupMediaMessage
                    .getMsgWithOnePhotoAndCaption(key, chatIdToSendMsg, update, direction, head));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasDocument() && update.getMessage().getMediaGroupId() == null) {
            sendDocumentMsg(groupMediaMessage
                    .getMsgWithOneDocumentAndCaption(key, chatIdToSendMsg, update, direction, head));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasAudio() && update.getMessage().getMediaGroupId() == null) {
            sendAudioMsg(groupMediaMessage
                    .getMsgWithOneAudioAndCaption(key, chatIdToSendMsg, update, direction, head));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasVideo() && update.getMessage().getMediaGroupId() == null) {
            sendVideoMsg(groupMediaMessage
                    .getMsgWithOneVideoAndCaption(key, chatIdToSendMsg, update, direction, head));
            return;
        }

        try {

            if (update.hasMessage() && update.getMessage().hasPhoto()
                    || update.getMessage().hasVideo() || update.getMessage().hasDocument() || update.getMessage().hasAudio()
                    && update.getMessage().getMediaGroupId() != null) {

                createMediaGroupData(update, head, chatIdToSendMsg, direction, key);
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private void createMediaGroupData(Update update, String head,
                                      Long chatIdToSendMsg, String direction, String key) {
        String id = update.getMessage().getMediaGroupId();

        if (!groupsMessages.containsKey(id)) {
            groupsMessages.put(id, new MediaGroupData());
            groupsMessages.get(id).setChatIdToSendMsg(chatIdToSendMsg);
            groupsMessages.get(id).setDirection(direction);
            groupsMessages.get(id).setHead(head);
            groupsMessages.get(id).setDeeplKey(key);
        }

        if (update.getMessage().hasPhoto()) {
            groupsMessages.get(id).getListPhotoFilesId().add(update.getMessage().getPhoto().get(0).getFileId());
        }

        if (update.getMessage().hasVideo()) {
            groupsMessages.get(id).getListVideoFilesId().add(update.getMessage().getVideo().getFileId());
        }

        if (update.getMessage().hasAudio()) {
            groupsMessages.get(id).getListAudioFilesId().add(update.getMessage().getAudio().getFileId());
        }

        if (update.getMessage().hasDocument()) {
            groupsMessages.get(id).getListDocumentsFilesId().add(update.getMessage().getDocument().getFileId());
        }

        if (update.getMessage().getCaption() != null) {
            groupsMessages.get(id).setCaption(update.getMessage().getCaption());
            if (update.getMessage().getCaptionEntities() != null) {
                groupsMessages.get(id).setEntities(update.getMessage().getCaptionEntities());
            }
        }

        groupsMessages.get(id).setLastUpdate(LocalDateTime.now());
    }

    public List<String> splitString(String originalText, int maxLength) {
        List<String> parts = new ArrayList<>();
        int length = Math.min(originalText.length(), maxLength);
        String part1 = originalText.substring(0, length);
        String part2 = originalText.substring(length);
        int newLineIndex = part2.indexOf("\n");
        int spaceIndex = part2.indexOf(" ", maxLength);
        if (newLineIndex >= 0 && newLineIndex <= 1.3 * length) {
            parts.add(part1 + part2.substring(0, newLineIndex + 1));
            parts.add(part2.substring(newLineIndex + 1));
        } else if (spaceIndex >= 0) {
            parts.add(part1 + part2.substring(0, spaceIndex + 1));
            parts.add(part2.substring(spaceIndex + 1));
        } else {
            parts.add(part1);
            parts.add(part2);
        }
        return parts;
    }

    public String removeHtmlTags(String input) {
        if (input == null) {
            return "-";
        }
        return input.replaceAll("<[^>]+>", "");
    }

    private void sendGroupMediaMsg(SendMediaGroup mainMsg) {
        int length = 1024;
        String originalText = mainMsg.getMedias().get(0).getCaption();
        Long chatIdMain = Long.valueOf(mainMsg.getChatId());
        try {
            if (originalText.length() > length) {
                List<String> splitString = splitString(originalText, length);
                String part1 = splitString.get(0);
                String part2 = splitString.get(1);

                mainMsg.getMedias().get(0).setCaption(part1);

                SendMessage secondPart = new SendMessage();
                secondPart.setChatId(chatIdMain);
                secondPart.enableHtml(true);
                secondPart.setParseMode(ParseMode.HTML);
                secondPart.setText(part2);

                try {
                    execute(mainMsg);
                } catch (TelegramApiException e) {
                    originalText = removeHtmlTags(originalText);
                    mainMsg.getMedias().get(0).setCaption(removeHtmlTags(originalText));
                    mainMsg.getMedias().get(0).setParseMode(null);
                    execute(mainMsg);
                }

                try {
                    execute(secondPart);
                } catch (TelegramApiException e) {
                    if (originalText.length() > length) {

                        List<String> splitStringE = splitString(removeHtmlTags(originalText), length);
                        String part1E = splitStringE.get(0);
                        String part2E = splitStringE.get(1);

                        mainMsg.getMedias().get(0).setParseMode(null);
                        mainMsg.getMedias().get(0).setCaption(part1E);
                        execute(mainMsg);

                        SendMessage second = new SendMessage();
                        second.setChatId(mainMsg.getChatId());
                        second.setText(part2E);
                        execute(second);
                        return;
                    }
                }
                return;
            }

            try {
                execute(mainMsg);
            } catch (Exception e) {
                throw new Exception();
            }
        } catch (Exception e) {

        }
    }

    private void sendMessage(SendMessage mainMsg) {
        int length = 4096;
        String originalText = mainMsg.getText();
        Long chatIdMain = Long.valueOf(mainMsg.getChatId());

        try {
            if (originalText.length() > length) {
                List<String> splitString = splitString(originalText, length);
                String part1 = splitString.get(0);
                String part2 = splitString.get(1);
                mainMsg.setText(part1);

                SendMessage secondPart = new SendMessage();
                secondPart.setChatId(chatIdMain);
                secondPart.enableHtml(true);
                secondPart.setParseMode(ParseMode.HTML);
                secondPart.setText(part2);

                try {
                    execute(mainMsg);
                } catch (TelegramApiException e) {
                    throw new Exception();
                }

                try {
                    execute(secondPart);
                } catch (TelegramApiException e) {
                }
                return;
            }

            try {
                execute(mainMsg);
            } catch (Exception e) {
                throw new Exception();
            }
        } catch (Exception e) {

            try {
                originalText = removeHtmlTags(originalText);

                if (originalText.length() > length) {

                    List<String> splitString = splitString(originalText, length);
                    String part1 = splitString.get(0);
                    String part2 = splitString.get(1);

                    mainMsg.setParseMode(null);
                    mainMsg.setText(part1);
                    execute(mainMsg);

                    SendMessage second = new SendMessage();
                    second.setChatId(mainMsg.getChatId());
                    second.setText(part2);
                    execute(second);
                    return;
                }

                mainMsg.setText(removeHtmlTags(originalText));
                mainMsg.setParseMode(null);
                execute(mainMsg);
            } catch (TelegramApiException exx) {
                e.printStackTrace();

            }
        }
    }

    private void sendPhotoMsg(SendPhoto mainMsg) {
        int length = 1024;
        String originalText = mainMsg.getCaption();
        Long chatIdMain = Long.valueOf(mainMsg.getChatId());

        try {
            if (originalText.length() > length) {
                List<String> splitString = splitString(originalText, length);
                String part1 = splitString.get(0);
                String part2 = splitString.get(1);
                mainMsg.setCaption(part1);

                SendMessage secondPart = new SendMessage();
                secondPart.setChatId(chatIdMain);
                secondPart.enableHtml(true);
                secondPart.setParseMode(ParseMode.HTML);
                secondPart.setText(part2);

                try {
                    execute(mainMsg);
                } catch (TelegramApiException e) {
                    List<String> splitStringE = splitString(removeHtmlTags(originalText), length);
                    String part1E = splitStringE.get(0);
                    String part2E = splitStringE.get(1);

                    mainMsg.setParseMode(null);
                    mainMsg.setCaption(part1E);
                    execute(mainMsg);

                    SendMessage second = new SendMessage();
                    second.setChatId(mainMsg.getChatId());
                    second.setText(part2E);
                    execute(second);
                    return;
                }

                try {
                    execute(secondPart);
                } catch (TelegramApiException e) {
                }
                return;
            }

            try {
                execute(mainMsg);
            } catch (Exception e) {
                throw new Exception();
            }
        } catch (Exception e) {

            try {
                originalText = removeHtmlTags(originalText);

                mainMsg.setCaption(removeHtmlTags(originalText));
                mainMsg.setParseMode(null);
                execute(mainMsg);
            } catch (TelegramApiException exx) {
                e.printStackTrace();

            }
        }
    }

    private void sendVideoMsg(SendVideo mainMsg) {
        int length = 1024;
        String originalText = mainMsg.getCaption();
        Long chatIdMain = Long.valueOf(mainMsg.getChatId());

        try {
            if (originalText.length() > length) {
                List<String> splitString = splitString(originalText, length);
                String part1 = splitString.get(0);
                String part2 = splitString.get(1);
                mainMsg.setCaption(part1);

                SendMessage secondPart = new SendMessage();
                secondPart.setChatId(chatIdMain);
                secondPart.enableHtml(true);
                secondPart.setParseMode(ParseMode.HTML);
                secondPart.setText(part2);

                try {
                    execute(mainMsg);
                } catch (TelegramApiException e) {
                    List<String> splitStringE = splitString(removeHtmlTags(originalText), length);
                    String part1E = splitStringE.get(0);
                    String part2E = splitStringE.get(1);

                    mainMsg.setParseMode(null);
                    mainMsg.setCaption(part1E);
                    execute(mainMsg);

                    SendMessage second = new SendMessage();
                    second.setChatId(mainMsg.getChatId());
                    second.setText(part2E);
                    execute(second);
                    return;
                }

                try {
                    execute(secondPart);
                } catch (TelegramApiException e) {
                }
                return;
            }

            try {
                execute(mainMsg);
            } catch (Exception e) {
                throw new Exception();
            }
        } catch (Exception e) {

            try {
                originalText = removeHtmlTags(originalText);

                mainMsg.setCaption(removeHtmlTags(originalText));
                mainMsg.setParseMode(null);
                execute(mainMsg);
            } catch (TelegramApiException exx) {
                e.printStackTrace();

            }
        }
    }

    private void sendDocumentMsg(SendDocument mainMsg) {
        int length = 1024;
        String originalText = mainMsg.getCaption();
        Long chatIdMain = Long.valueOf(mainMsg.getChatId());

        try {
            if (originalText.length() > length) {
                List<String> splitString = splitString(originalText, length);
                String part1 = splitString.get(0);
                String part2 = splitString.get(1);
                mainMsg.setCaption(part1);

                SendMessage secondPart = new SendMessage();
                secondPart.setChatId(chatIdMain);
                secondPart.enableHtml(true);
                secondPart.setParseMode(ParseMode.HTML);
                secondPart.setText(part2);

                try {
                    execute(mainMsg);
                } catch (TelegramApiException e) {
                    List<String> splitStringE = splitString(removeHtmlTags(originalText), length);
                    String part1E = splitStringE.get(0);
                    String part2E = splitStringE.get(1);

                    mainMsg.setParseMode(null);
                    mainMsg.setCaption(part1E);
                    execute(mainMsg);

                    SendMessage second = new SendMessage();
                    second.setChatId(mainMsg.getChatId());
                    second.setText(part2E);
                    execute(second);
                    return;
                }

                try {
                    execute(secondPart);
                } catch (TelegramApiException e) {
                }
                return;
            }

            try {
                execute(mainMsg);
            } catch (Exception e) {
                throw new Exception();
            }
        } catch (Exception e) {

            try {
                originalText = removeHtmlTags(originalText);

                mainMsg.setCaption(removeHtmlTags(originalText));
                mainMsg.setParseMode(null);
                execute(mainMsg);
            } catch (TelegramApiException exx) {
                e.printStackTrace();

            }
        }
    }

    private void sendAudioMsg(SendAudio mainMsg) {
        int length = 1024;
        String originalText = mainMsg.getCaption();
        Long chatIdMain = Long.valueOf(mainMsg.getChatId());

        try {
            if (originalText.length() > length) {
                List<String> splitString = splitString(originalText, length);
                String part1 = splitString.get(0);
                String part2 = splitString.get(1);
                mainMsg.setCaption(part1);

                SendMessage secondPart = new SendMessage();
                secondPart.setChatId(chatIdMain);
                secondPart.enableHtml(true);
                secondPart.setParseMode(ParseMode.HTML);
                secondPart.setText(part2);

                try {
                    execute(mainMsg);
                } catch (TelegramApiException e) {
                    List<String> splitStringE = splitString(removeHtmlTags(originalText), length);
                    String part1E = splitStringE.get(0);
                    String part2E = splitStringE.get(1);

                    mainMsg.setParseMode(null);
                    mainMsg.setCaption(part1E);
                    execute(mainMsg);

                    SendMessage second = new SendMessage();
                    second.setChatId(mainMsg.getChatId());
                    second.setText(part2E);
                    execute(second);
                    return;
                }

                try {
                    execute(secondPart);
                } catch (TelegramApiException e) {
                }
                return;
            }

            try {
                execute(mainMsg);
            } catch (Exception e) {
                throw new Exception();
            }
        } catch (Exception e) {

            try {
                originalText = removeHtmlTags(originalText);

                mainMsg.setCaption(removeHtmlTags(originalText));
                mainMsg.setParseMode(null);
                execute(mainMsg);
            } catch (TelegramApiException exx) {
                e.printStackTrace();

            }
        }
    }
}