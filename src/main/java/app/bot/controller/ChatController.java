package app.bot.controller;

import app.bot.config.BotConfig;
import app.bot.admin.AdminMessage;
import app.bot.constructor.ConstructorGroupMediaMessage;
import app.bot.constructor.ConstructorPlainTextMsg;
import app.bot.create.AddRulesMsg;
import app.bot.create.CreateMsg;
import app.bot.create.EditMessage;
import app.bot.groupInfo.SendChatData;
import app.bot.groupInfo.Flag;
import app.bot.groupInfo.ListenChatData;
import app.bot.groupInfo.Header;
import app.bot.model.MediaGroupData;
import app.bot.service.SendToChatDataService;
import app.bot.service.GroupChatInfoService;
import jakarta.annotation.PostConstruct;
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
    @Autowired
    private GroupChatInfoService groupService;
    @Autowired
    private AdminMessage adminMessage;
    @Autowired
    private AddRulesMsg addRulesMsg;
    @Autowired
    private CreateMsg createDataMsg;
    @Autowired
    private ConstructorPlainTextMsg textMessage;
    @Autowired
    private ConstructorGroupMediaMessage constructorGroupMediaMessage;
    @Autowired
    private Header header;
    @Autowired
    private Flag flag;
    @Autowired
    private SendToChatDataService sendToChatDataService;
    @Autowired
    private EditMessage editMessage;
    private final HashSet<Integer> creatingSteps = new HashSet<>();
    private final HashMap<Long, ListenChatData> chatData = new HashMap<>();
    private final HashMap<Long, SendChatData> sendToChatData = new HashMap<>();
    private final HashMap<Integer, Long> editRecords = new HashMap<>();
    private final HashMap<String, MediaGroupData> mediaGroupMsgUpdateCollector = new HashMap<>();
    private Long creatingDataListenGroupChatId = 0l;
    private Long chatToSendMsg = 0L;
    private final HashMap<Long, String> isEdit = new HashMap<>();
    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }
    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }
    @PostConstruct
    private void init() {
        for (ListenChatData info : groupService.getAllGroups()) {
            chatData.put(info.getListenTheGroup(), info);
        }

        for (SendChatData key : sendToChatDataService.getAllRecords()) {
            sendToChatData.put(key.getSendToChatId(), key);
        }

        if (chatData.isEmpty()) {
            sendMessage(adminMessage.getNullStartMessage(botConfig.getSuperUserChatId()));
        }

    }
    @Scheduled(fixedRate = 2000)
    public void getUpdate() {
        List<String> idList = new ArrayList<>();

        for (Map.Entry<String, MediaGroupData> data : mediaGroupMsgUpdateCollector.entrySet()) {
            String id = data.getKey();
            LocalDateTime plusTime = data.getValue().getLastUpdate().plusSeconds(5);
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(plusTime)) {
                sendGroupMediaMsg(constructorGroupMediaMessage.getMediaGroupMessage(data.getValue()));
                idList.add(id);
            }
        }

        for (String id : idList) {
            mediaGroupMsgUpdateCollector.remove(id);
        }
        idList.clear();
    }
    private void sendListSettings() {
        for (Map.Entry<Long, ListenChatData> entry : chatData.entrySet()) {
            sendSimpleAdminMsg(adminMessage.getAllEntities(botConfig.getSuperUserChatId(),
                    entry.getValue(), sendToChatData));
        }
        sendSimpleAdminMsg(adminMessage.getOfferToAdd(botConfig.getSuperUserChatId()));
        return;
    }
    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(LocalDateTime.now());
        try {
            if (update.hasMessage() && update.getMessage().isCommand() && update.getCallbackQuery() == null) {
                String command = update.getMessage().getText();

                if (command.equals("/get_chat_id")) {
                    sendMessage(adminMessage.getChatIdMsg(update.getMessage().getChatId()));
                    return;
                }
            }

            if (update.hasChannelPost()
                    && update.getChannelPost().hasText()
                    && update.getChannelPost().getText().equals("/get_chat_id")) {

                sendMessage(adminMessage.getChatIdMsg(update.getChannelPost().getChatId()));
                return;
            }

            if (update.hasMessage()
                    && update.getMessage().hasText()
                    && update.getMessage().getText().equals("/get_chat_id")) {
                sendMessage(adminMessage.getChatIdMsg(update.getMessage().getChatId()));
                return;
            }
        } catch (Exception e) {
        }

        Long superUserChatId = botConfig.getSuperUserChatId();
        try {
            if (update.getMessage().isCommand() && update.getMessage().getChatId().equals(superUserChatId)) {
                commandHandle(update);
                return;
            }
        } catch (Exception e) {
        }

        try {
            if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getChatId().equals(superUserChatId)) {

                textHandle(update);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (update.hasCallbackQuery() && update.getCallbackQuery()
                    .getMessage().getChatId().equals(superUserChatId)) {
                callbackDataHandle(update);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        try {
            if (!chatData.isEmpty()) {
                Long chatIdUpdate = update.getMessage().getChatId();

                List<Long> chatIdToSendMsgList = chatData.get(chatIdUpdate).getAllChatIdToSend();
                for (int i = 0; i < chatIdToSendMsgList.size(); i++) {

                    if (chatIdToSendMsgList.get(i) != null) {
                        Long sendTo = chatIdToSendMsgList.get(i);

                        String direction = sendToChatData.get(sendTo).getDirection();
                        String key = sendToChatData.get(sendTo).getKey();
                        forwardHandle(update, sendTo, direction, key, i);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void forwardHandle(Update update, Long chatIdToSendMsg, String direction, String key, int call) {
        Long chatIdUpdate = update.getMessage().getChatId();

        String head = flag.getFlag(chatData.get(chatIdUpdate).getCountryCode())
                + header.createHead(update);

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getEntities() == null) {

            sendMessage(textMessage.translatePlainText(key, chatIdToSendMsg, update,
                    direction, head));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getEntities() != null) {
            sendMessage(textMessage.translateTextWithEntities(key, chatIdToSendMsg, update,
                    direction, head));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasPhoto() && update.getMessage().getMediaGroupId() == null) {
            sendPhotoMsg(constructorGroupMediaMessage
                    .getMsgWithOnePhotoAndCaption(key, chatIdToSendMsg, update, direction, head));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasDocument() && update.getMessage().getMediaGroupId() == null) {
            sendDocumentMsg(constructorGroupMediaMessage
                    .getMsgWithOneDocumentAndCaption(key, chatIdToSendMsg, update, direction, head));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasAudio() && update.getMessage().getMediaGroupId() == null) {
            sendAudioMsg(constructorGroupMediaMessage
                    .getMsgWithOneAudioAndCaption(key, chatIdToSendMsg, update, direction, head));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasVideo() && update.getMessage().getMediaGroupId() == null) {
            sendVideoMsg(constructorGroupMediaMessage
                    .getMsgWithOneVideoAndCaption(key, chatIdToSendMsg, update, direction, head));
            return;
        }

        try {

            if (update.hasMessage() && update.getMessage().hasPhoto()
                    || update.getMessage().hasVideo() || update.getMessage().hasDocument() || update.getMessage().hasAudio()
                    && update.getMessage().getMediaGroupId() != null) {

                createMediaGroupData(update, head, chatIdToSendMsg, direction, call, key);
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
    private void createMediaGroupData(Update update, String head, Long chatIdToSendMsg, String direction, int call, String key) {
        String id = call + "_" + update.getMessage().getMediaGroupId();

        if (!mediaGroupMsgUpdateCollector.containsKey(id)) {
            mediaGroupMsgUpdateCollector.put(id, new MediaGroupData());
            mediaGroupMsgUpdateCollector.get(id).setChatIdToSendMsg(chatIdToSendMsg);
            mediaGroupMsgUpdateCollector.get(id).setDirection(direction);
            mediaGroupMsgUpdateCollector.get(id).setHead(head);
            mediaGroupMsgUpdateCollector.get(id).setDeeplKey(key);
        }

        if (update.getMessage().hasPhoto()) {
            mediaGroupMsgUpdateCollector.get(id).getListPhotoFilesId().add(update.getMessage().getPhoto().get(0).getFileId());
        }

        if (update.getMessage().hasVideo()) {
            mediaGroupMsgUpdateCollector.get(id).getListVideoFilesId().add(update.getMessage().getVideo().getFileId());
        }

        if (update.getMessage().hasAudio()) {
            mediaGroupMsgUpdateCollector.get(id).getListAudioFilesId().add(update.getMessage().getAudio().getFileId());
        }

        if (update.getMessage().hasDocument()) {
            mediaGroupMsgUpdateCollector.get(id).getListDocumentsFilesId().add(update.getMessage().getDocument().getFileId());
        }

        if (update.getMessage().getCaption() != null) {
            mediaGroupMsgUpdateCollector.get(id).setCaption(update.getMessage().getCaption());
            if (update.getMessage().getCaptionEntities() != null) {
                mediaGroupMsgUpdateCollector.get(id).setEntities(update.getMessage().getCaptionEntities());
            }
        }

        mediaGroupMsgUpdateCollector.get(id).setLastUpdate(LocalDateTime.now());
    }
    private void commandHandle(Update update) {
        String command = update.getMessage().getText();
        if (command.equals("/start")) {
            creatingSteps.clear();
            sendToChatData.clear();
            chatData.clear();

            chatToSendMsg = 0l;


            for (ListenChatData data : groupService.getAllGroups()) {
                chatData.put(data.getListenTheGroup(), data);
            }

            for (SendChatData data : sendToChatDataService.getAllRecords()) {
                sendToChatData.put(data.getSendToChatId(), data);
            }

            if (chatData.isEmpty() || sendToChatData.isEmpty()) {
                sendMessage(adminMessage.getNullStartMessage(botConfig.getSuperUserChatId()));
            } else {
                sendListSettings();
                return;
            }
        }

        if (command.contains("/deleteRule_")) {

            Long recordId = Long.valueOf(command.split("_")[1]) * (-1);
            int i = Integer.parseInt(command.split("_")[2]);
            ListenChatData data = chatData.get(recordId);
            groupService.deleteByObject(data);

            if (i == 1) {
                data.setSendToTheGroup1(null);
            }
            if (i == 2) {
                data.setSendToTheGroup2(null);
            }
            if (i == 3) {
                System.out.println(3);
                data.setSendToTheGroup3(null);
            }
            if (i == 4) {
                data.setSendToTheGroup4(null);
            }

            groupService.save(data);

            chatData.clear();
            sendToChatData.clear();

            for (ListenChatData key : groupService.getAllGroups()) {
                chatData.put(key.getListenTheGroup(), key);
            }
            for (SendChatData key : sendToChatDataService.getAllRecords()) {
                sendToChatData.put(key.getSendToChatId(), key);
            }

            editMethod(isEdit.get(botConfig.getSuperUserChatId()));
        }

        if (command.contains("/changeFlag_")) {
            creatingSteps.clear();
            creatingSteps.add(14);

            creatingDataListenGroupChatId = Long.parseLong(command.split("_")[1]) * (-1);
            isEdit.put(botConfig.getSuperUserChatId(), command);
            sendSimpleAdminMsg(createDataMsg.getSendMsg(botConfig.getSuperUserChatId(),
                    "Для изменения флага отправь две буквы страны в международном фoрмате, например:" +
                            "<code>RU</code> или <code>UA</code>", null));
        }

    }
    private void editMethod(String data) {
        isEdit.clear();
        creatingSteps.clear();

        isEdit.put(botConfig.getSuperUserChatId(), data);


        String[] splitData = data.split("_");
        long recordId = Long.parseLong(splitData[1]);

        if (recordId > 0) {
            recordId = recordId * (-1);
        }

        chatData.clear();
        sendToChatData.clear();
        for (ListenChatData info : groupService.getAllGroups()) {
            chatData.put(info.getListenTheGroup(), info);
        }
        for (SendChatData key : sendToChatDataService.getAllRecords()) {
            sendToChatData.put(key.getSendToChatId(), key);
        }

        sendSimpleAdminMsg(editMessage.startMessage(botConfig.getSuperUserChatId(), recordId,
                chatData.get(recordId)));
    }
    private void callbackDataHandle(Update update) {
        String message = "Введи chatId куда будем дополнительно переводить и отправлять сообщения";
        String data = update.getCallbackQuery().getData();
        Long superChatId = botConfig.getSuperUserChatId();

        if (data.equals("0")) {
            creatingSteps.clear();
            creatingSteps.add(0);
            sendMessage(createDataMsg.zeroStepCreate(botConfig.getSuperUserChatId()));
            return;
        }

        if (data.contains("del_")) {
            try {
                String[] splitData = data.split("_");
                Long listenGroupChatId = Long.valueOf(splitData[1]);
                groupService.deleteByObject(chatData.get(listenGroupChatId));
                chatData.remove(listenGroupChatId);
            } catch (Exception e) {
                if (chatData.isEmpty()) {
                    sendMessage(adminMessage.getNullStartMessage(botConfig.getSuperUserChatId()));
                    return;
                }
            }
            if (!chatData.isEmpty()) {
                sendListSettings();
                return;
            } else {
                sendMessage(adminMessage.getNullStartMessage(botConfig.getSuperUserChatId()));
            }
        }

        if (data.contains("edit_")) {
            editMethod(data);
            return;
        }

        if (data.contains("addRules_")) {
            int i = Integer.parseInt(data.split("_")[1]);
            if (i == 1) {
                creatingSteps.clear();
                creatingSteps.add(5);
                sendSimpleAdminMsg(createDataMsg.getSendMsg(botConfig.getSuperUserChatId(),
                        message, null));
                return;
            }

            if (i == 2) {
                creatingSteps.clear();
                creatingSteps.add(8);
                sendSimpleAdminMsg(createDataMsg.getSendMsg(botConfig.getSuperUserChatId(),
                        message, null));
                return;
            }

            creatingSteps.clear();
            creatingSteps.add(11);
            sendSimpleAdminMsg(createDataMsg.getSendMsg(botConfig.getSuperUserChatId(),
                    message, null));
            return;
        }

        if (data.contains("useThis_")) {
            Long chatId = Long.valueOf(data.split("_")[1]);
            int i = Integer.parseInt(data.split("_")[2]);
            creatingSteps.clear();
            creatingSteps.add(4);

            if (i == 1) {
                chatData.get(creatingDataListenGroupChatId).setSendToTheGroup1(chatId);
                if (isEdit.containsKey(superChatId)) {

                    groupService.save(chatData.get(creatingDataListenGroupChatId));
                    chatData.clear();

                    for (ListenChatData d : groupService.getAllGroups()) {
                        chatData.put(d.getListenTheGroup(), d);
                    }
                    creatingDataListenGroupChatId = 0l;
                    editMethod(isEdit.get(superChatId));
                    return;
                }
                sendSimpleAdminMsg(createDataMsg.useExistingRecord(botConfig.getSuperUserChatId(), i));
                return;
            }
            if (i == 5) {
                chatData.get(creatingDataListenGroupChatId).setSendToTheGroup2(chatId);
                if (isEdit.containsKey(superChatId)) {

                    groupService.save(chatData.get(creatingDataListenGroupChatId));
                    chatData.clear();

                    for (ListenChatData d : groupService.getAllGroups()) {
                        chatData.put(d.getListenTheGroup(), d);
                    }
                    creatingDataListenGroupChatId = 0l;
                    editMethod(isEdit.get(superChatId));
                    return;
                }
                sendSimpleAdminMsg(createDataMsg.useExistingRecord(botConfig.getSuperUserChatId(), i));
                return;
            }
            if (i == 8) {
                chatData.get(creatingDataListenGroupChatId).setSendToTheGroup3(chatId);
                if (isEdit.containsKey(superChatId)) {

                    groupService.save(chatData.get(creatingDataListenGroupChatId));
                    chatData.clear();

                    for (ListenChatData d : groupService.getAllGroups()) {
                        chatData.put(d.getListenTheGroup(), d);
                    }
                    creatingDataListenGroupChatId = 0l;
                    editMethod(isEdit.get(superChatId));
                    return;
                }
                sendSimpleAdminMsg(createDataMsg.useExistingRecord(botConfig.getSuperUserChatId(), i));
                return;
            }
            if (i == 11) {
                chatData.get(creatingDataListenGroupChatId).setSendToTheGroup4(chatId);

                if (isEdit.containsKey(superChatId)) {

                    groupService.save(chatData.get(creatingDataListenGroupChatId));
                    chatData.clear();

                    for (ListenChatData d : groupService.getAllGroups()) {
                        chatData.put(d.getListenTheGroup(), d);
                    }
                    creatingDataListenGroupChatId = 0l;
                    editMethod(isEdit.get(superChatId));
                    return;
                }

                sendSimpleAdminMsg(createDataMsg.useExistingRecord(botConfig.getSuperUserChatId(), -1));
                return;
            }
        }
    }
    private void textHandle(Update update) {
        String text = update.getMessage().getText();
        Long superChatId = update.getMessage().getChatId();
        createUserData(superChatId, text);
    }
    private void createUserData(Long superChatId, String text) {
        String mistake = "Кажется ты ввел что-то другое. Повтори попытку.";
        String offerDirection = "Введи язык для перевода, например <code>RU</code>" +
                "/<code>EN-GB</code>/<code>EN-US</code>";

        if (creatingSteps.contains(0)) {

            try {
                creatingDataListenGroupChatId = Long.valueOf(text.trim());

                ListenChatData d = new ListenChatData();
                d.setListenTheGroup(creatingDataListenGroupChatId);
                chatData.put(creatingDataListenGroupChatId, d);

                creatingSteps.clear();
                creatingSteps.add(1);
                sendSimpleAdminMsg(createDataMsg.firstStepCreate(superChatId));
                return;
            } catch (NumberFormatException n) {
                sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                        mistake, null));
            }
            return;
        }

        if (creatingSteps.contains(1)) {//here 1
            try {
                chatToSendMsg = Long.valueOf(text.trim());

                if (sendToChatDataService.findByChatIdToSendNews(chatToSendMsg)) {
                    sendSimpleAdminMsg(createDataMsg.yourRecordIsExist(superChatId,
                            sendToChatData.get(chatToSendMsg), 1));
                    return;
                }

                chatData.get(creatingDataListenGroupChatId).setSendToTheGroup1(chatToSendMsg);

                SendChatData d = new SendChatData();
                d.setSendToChatId(chatToSendMsg);
                sendToChatData.put(chatToSendMsg, d);

                creatingSteps.clear();
                creatingSteps.add(2);
                sendSimpleAdminMsg(createDataMsg.secondStep(superChatId));
                return;
            } catch (NumberFormatException n) {
                sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                        mistake, null));
            }
            return;
        }

        if (creatingSteps.contains(2)) {
            if (text.length() < 7) {
                sendToChatData.get(chatToSendMsg).setDirection(text.trim().toUpperCase());

                creatingSteps.clear();
                creatingSteps.add(3);
                sendSimpleAdminMsg(createDataMsg.thirdStep(superChatId));
                return;
            }
            sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                    mistake, null));
            return;
        }

        if (creatingSteps.contains(3)) {
            sendToChatData.get(chatToSendMsg).setKey(text.trim());

            sendToChatDataService.save(sendToChatData.get(chatToSendMsg));
            sendToChatData.clear();

            for (SendChatData s : sendToChatDataService.getAllRecords()) {
                sendToChatData.put(s.getSendToChatId(), s);
            }

            creatingSteps.clear();
            creatingSteps.add(4);

            if (isEdit.containsKey(superChatId)) {
                editMethod(isEdit.get(superChatId));
                return;
            }
            sendSimpleAdminMsg(createDataMsg.fourthStep(superChatId, 1));
            return;
        }

        if (creatingSteps.contains(4)) {
            if (text.length() == 2) {
                chatData.get(creatingDataListenGroupChatId).setCountryCode(text.trim().toUpperCase());

                groupService.save(chatData.get(creatingDataListenGroupChatId));
                chatData.clear();

                for (ListenChatData d : groupService.getAllGroups()) {
                    chatData.put(d.getListenTheGroup(), d);
                }

                creatingDataListenGroupChatId = 0l;
                chatToSendMsg = 0l;
                creatingSteps.clear();
                sendSimpleAdminMsg(createDataMsg.finish(superChatId, text.trim().toUpperCase()));
                sendListSettings();
                return;
            }

            sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                    mistake, null));
            return;
        }

        if (creatingSteps.contains(5)) {//here2
            try {
                chatToSendMsg = Long.valueOf(text.trim());

                if (sendToChatDataService.findByChatIdToSendNews(chatToSendMsg)) {
                    sendSimpleAdminMsg(createDataMsg.yourRecordIsExist(superChatId,
                            sendToChatData.get(chatToSendMsg), 5));
                    return;
                }


                chatData.get(creatingDataListenGroupChatId).setSendToTheGroup2(chatToSendMsg);

                SendChatData d = new SendChatData();
                d.setSendToChatId(chatToSendMsg);
                sendToChatData.put(chatToSendMsg, d);

                creatingSteps.clear();
                creatingSteps.add(6);

                sendSimpleAdminMsg(addRulesMsg.getSendMsg(superChatId,
                        offerDirection, null));
                return;
            } catch (NumberFormatException n) {
                sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                        mistake, null));
            }
            return;
        }

        if (creatingSteps.contains(6)) {
            if (text.length() < 7) {
                sendToChatData.get(chatToSendMsg).setDirection(text.trim().toUpperCase());

                creatingSteps.clear();
                creatingSteps.add(7);
                sendSimpleAdminMsg(addRulesMsg.getSendMsg(superChatId,
                        "Теперь введи API KEY DEEPL", null));
                return;
            }
            sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                    mistake, null));
            return;
        }

        if (creatingSteps.contains(7)) {
            sendToChatData.get(chatToSendMsg).setKey(text.trim());

            sendToChatDataService.save(sendToChatData.get(chatToSendMsg));
            sendToChatData.clear();
            chatToSendMsg = 0l;
            creatingSteps.clear();
            creatingSteps.add(4);

            for (SendChatData s : sendToChatDataService.getAllRecords()) {
                sendToChatData.put(s.getSendToChatId(), s);
            }

            if (isEdit.containsKey(superChatId)) {
                editMethod(isEdit.get(superChatId));
                return;
            }
            sendSimpleAdminMsg(createDataMsg.fourthStep(superChatId, 2));
        }

        if (creatingSteps.contains(8)) {//here3
            try {
                chatToSendMsg = Long.valueOf(text.trim());

                if (sendToChatDataService.findByChatIdToSendNews(chatToSendMsg)) {
                    sendSimpleAdminMsg(createDataMsg.yourRecordIsExist(superChatId,
                            sendToChatData.get(chatToSendMsg), 8));
                    return;
                }
                chatData.get(creatingDataListenGroupChatId).setSendToTheGroup3(chatToSendMsg);

                SendChatData d = new SendChatData();
                d.setSendToChatId(chatToSendMsg);
                sendToChatData.put(chatToSendMsg, d);

                creatingSteps.clear();
                creatingSteps.add(9);

                sendSimpleAdminMsg(addRulesMsg.getSendMsg(superChatId,
                        offerDirection, null));
                return;
            } catch (NumberFormatException n) {
                sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                        mistake + 7, null));
            }
            return;
        }

        if (creatingSteps.contains(9)) {
            if (text.length() < 7) {
                sendToChatData.get(chatToSendMsg).setDirection(text.trim().toUpperCase());

                creatingSteps.clear();
                creatingSteps.add(10);
                sendSimpleAdminMsg(addRulesMsg.getSendMsg(superChatId,
                        "Теперь введи API KEY DEEPL", null));
                return;
            }
            sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                    mistake, null));
            return;
        }

        if (creatingSteps.contains(10)) {
            sendToChatData.get(chatToSendMsg).setKey(text.trim());

            sendToChatDataService.save(sendToChatData.get(chatToSendMsg));
            sendToChatData.clear();
            chatToSendMsg = 0l;
            creatingSteps.clear();
            creatingSteps.add(4);

            for (SendChatData s : sendToChatDataService.getAllRecords()) {
                sendToChatData.put(s.getSendToChatId(), s);
            }

            if (isEdit.containsKey(superChatId)) {
                editMethod(isEdit.get(superChatId));
                return;
            }
            sendSimpleAdminMsg(createDataMsg.fourthStep(superChatId, 3));
        }

        if (creatingSteps.contains(11)) {//here4
            try {
                chatToSendMsg = Long.valueOf(text.trim());

                if (sendToChatDataService.findByChatIdToSendNews(chatToSendMsg)) {
                    sendSimpleAdminMsg(createDataMsg.yourRecordIsExist(superChatId,
                            sendToChatData.get(chatToSendMsg), 11));
                    return;
                }
                chatData.get(creatingDataListenGroupChatId).setSendToTheGroup4(chatToSendMsg);

                SendChatData d = new SendChatData();
                d.setSendToChatId(chatToSendMsg);
                sendToChatData.put(chatToSendMsg, d);

                creatingSteps.clear();
                creatingSteps.add(12);

                sendSimpleAdminMsg(addRulesMsg.getSendMsg(superChatId,
                        offerDirection, null));
                return;
            } catch (NumberFormatException n) {
                sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                        mistake, null));
            }
            return;
        }

        if (creatingSteps.contains(12)) {
            if (text.length() < 7) {
                sendToChatData.get(chatToSendMsg).setDirection(text.trim().toUpperCase());

                creatingSteps.clear();
                creatingSteps.add(13);
                sendSimpleAdminMsg(addRulesMsg.getSendMsg(superChatId,
                        "Теперь введи API KEY DEEPL", null));
                return;
            }
            sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                    mistake, null));
            return;
        }

        if (creatingSteps.contains(13)) {
            sendToChatData.get(chatToSendMsg).setKey(text.trim());

            sendToChatDataService.save(sendToChatData.get(chatToSendMsg));
            sendToChatData.clear();
            chatToSendMsg = 0l;
            creatingSteps.clear();
            creatingSteps.add(4);

            for (SendChatData s : sendToChatDataService.getAllRecords()) {
                sendToChatData.put(s.getSendToChatId(), s);
            }

            sendSimpleAdminMsg(createDataMsg.fourthStep(superChatId, -1));
        }

        if (creatingSteps.contains(14)) {
            if (text.length() == 2) {
                chatData.get(creatingDataListenGroupChatId).setCountryCode(text.trim().toUpperCase());

                groupService.save(chatData.get(creatingDataListenGroupChatId));

                chatData.clear();
                for (ListenChatData d : groupService.getAllGroups()) {
                    chatData.put(d.getListenTheGroup(), d);
                }

                creatingDataListenGroupChatId = 0l;
                chatToSendMsg = 0l;
                creatingSteps.clear();

                editMethod(isEdit.get(superChatId));
                return;
            }

            sendSimpleAdminMsg(createDataMsg.getSendMsg(superChatId,
                    mistake, null));
            return;
        }
    }
    private void sendSimpleAdminMsg(SendMessage msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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
        if(input == null) {
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