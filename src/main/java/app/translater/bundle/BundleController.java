package app.translater.bundle;

import app.bot.config.BotConfig;
import app.bot.data.BotContentData;
import app.bot.util.MessageExecutor;
import app.translater.bundle.data.BundleKeyboard;
import app.translater.bundle.model.Bundle;
import app.translater.bundle.dao.BundleRedisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Service
public class BundleController {
    @Autowired
    @Lazy
    private MessageExecutor executor;
    @Autowired
    private BotConfig botConfig;
    @Autowired
    private BundleRedisDao bundleRedisDao;
    @Autowired
    private BundleKeyboard keyboard;
    private final StringBuilder builder = new StringBuilder();
    private Bundle bundle = new Bundle();
    private Boolean sourceUpdate;
    private Integer commonMsgId;

    private Long getAdminChatId() {
        return botConfig.getSuperUserChatId();
    }

    private String getBotUserName() {
        return botConfig.getBotUsername();
    }

    public boolean handleUpdate(Update update) {

        if (!update.hasMessage()
                && update.getMyChatMember() != null
                && update.getMyChatMember().getNewChatMember() != null
                && update.getMyChatMember().getOldChatMember() != null
                && update.getMyChatMember().getNewChatMember() != null

                && !update.getMyChatMember().getOldChatMember().getStatus().equals("administrator")
                && update.getMyChatMember().getNewChatMember().getStatus().equals("administrator")
                && update.getMyChatMember().getNewChatMember().getUser().getUserName().equals(getBotUserName())
                && update.getMyChatMember().getChat().getType().equals("channel")
                && !sourceUpdate) {

            return addTargetChannel(update);
        }


        if (update.getMessage() != null
                && update.getMessage().getNewChatMembers() != null
                && update.getMessage().getText() == null
                && update.getMessage().getLeftChatMember() == null) {


            if (sourceUpdate != null) {
                if (sourceUpdate) {
                    return addSourceChat(update);
                } else {
                    return addTargetChat(update);
                }
            }
        }

        if (update.getMessage() != null && update.getMessage().hasText()
                && update.getMessage().getFrom().getId().equals(getAdminChatId())) {
            return handleTextMessage(update);
        }

        if (update.hasCallbackQuery()) {
            executor.sendCallBackAnswer(BotContentData.getCallbackQueryAnswer(update));
            return handleCallbackQuery(update);
        }

        return false;
    }

    private boolean handleTextMessage(Update update) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (text.equals("/start")) {
            if (bundleRedisDao.getAllBundles().isEmpty()) {
                executor.sendMessage(BotContentData.getSendMessage(chatId,
                        "Кажется ни одной связки не создано!\nНачни прямо сейчас!",
                        keyboard.create()));
                return true;
            }

            return showBundleSelectionMenu();
        }

        if (text.equals("/start@".concat(getBotUserName()).concat(" true"))) {

            if (sourceUpdate != null) {
                if (sourceUpdate) {
                    return addSourceChat(update);
                } else {
                    return addTargetChat(update);
                }
            }

            return true;
        }

        if (text.contains("Внесите изменения и отправьте этот текст:")) {
            return bundleUpdate(update);
        }

        return false;
    }

    private boolean handleCallbackQuery(Update update) {
        Long chatId = update.getCallbackQuery().getFrom().getId();
        String data = update.getCallbackQuery().getData();
        int msgId = update.getCallbackQuery().getMessage().getMessageId();

        if (data.equals("BACK")) {

            executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                    "Выберите связку, которую хотите редактировать.\nСвязка названа по группе назначения",
                    keyboard.getBundles(bundleRedisDao.getAllBundles()), msgId));
            return true;
        }

        if (data.equals("CREATE")) {
            sourceUpdate = true;
            commonMsgId = msgId;

            executor.editMessage(BotContentData.getEditMessage(chatId,
                    "Создание новой связки!\nДавай назначим группу/тему для новых сообщений, для перевода (ОТКУДА)",
                    keyboard.addBotToSourceChatOrChannel(getBotUserName()),
                    msgId));
            return true;
        }

        if (data.contains("lang=") && bundle != null) {
            bundle.setLang(data.split("=")[1]);

            executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                    "Готово! Связка " + bundle.getNameTo() + " сохранена!",
                    null, msgId));

            bundleRedisDao.saveBundle(bundle);
            bundle = null;
            commonMsgId = 0;

            return showBundleSelectionMenu();
        }

        if (data.startsWith("DELETE_")) {
            bundleRedisDao.deleteBundle(data.split("_")[1]);
            return showBundleSelectionMenu();
        }

        if (data.contains(BundleRedisDao.KEY)) {
            Optional<Bundle> optionalBundle = bundleRedisDao.getBundle(data);

            if (optionalBundle.isEmpty()) {
                executor.editMessage(BotContentData.getEditMessage(chatId,
                        "Что-то пошло не так, сформируйте сообщение со связками заново нажав /start",
                        null, msgId));
                return true;
            } else {
                return sendBundleDescription(optionalBundle.get(), data, msgId);
            }
        }

        return false;
    }

    private boolean sendBundleDescription(Bundle foundBundle, String data, int msgId) {
        builder.setLength(0);

        builder.append("Группа для перевода: ")
                .append(foundBundle.getNameFrom()).append(", ").append(foundBundle.getFrom()).append("\n\n")
                .append("Целевая группа: ")
                .append(foundBundle.getNameTo()).append(", ").append(foundBundle.getTo()).append("\n\n")
                .append("Переводим на: ").append(foundBundle.getLang()).append("\n\n")
                .append("Флаг: ").append(foundBundle.getFlag()).append("\n\n")
                .append("API Deepl: ").append(foundBundle.getKey());

        if (msgId == -1) {
            executor.sendMessage(BotContentData.getSendMessage(getAdminChatId(), builder.toString(),
                    keyboard.editBundle(builder.toString(), data)));
            return true;
        }

        executor.editMessage(BotContentData.getEditMessage(getAdminChatId(), builder.toString(),
                keyboard.editBundle(builder.toString(), data), msgId));
        return true;
    }

    private boolean showBundleSelectionMenu() {
        executor.sendMessage(BotContentData.getSendMessage(getAdminChatId(),
                "Выберите связку, которую хотите редактировать.\nСвязка названа по группе назначения",
                keyboard.getBundles(bundleRedisDao.getAllBundles())));
        return true;
    }

    private String[] parseBundleDescription(String input) {
        String[] lines = input.split("\n\n");
        String key = lines[0].split(":")[1];
        String from = lines[2].split(",")[1];
        String to = lines[3].split(",")[1];
        String lang = lines[4].split("Переводим на: ")[1];
        String flag = lines[5].split("Флаг:")[1];
        String api = lines[6].split("API Deepl:")[1];

        return new String[]{key, from, to, lang, flag, api};
    }

    private boolean addSourceChat(Update update) {
        sourceUpdate = false;

        bundle.setFrom(update.getMessage().getChat().getId());
        bundle.setNameFrom(update.getMessage().getChat().getTitle());

        executor.sendMessage(BotContentData.getSendMessage(bundle.getFrom(),
                "Группа перевода добавлена и записана!",
                null));

        executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                "Группа перевода добавлена и записана!\n\nТеперь добаим целевую группу - канал для переведенных сообщений(КУДА)",

                keyboard.addBotToTargetChatOrChannel(getBotUserName()), commonMsgId));
        return true;

    }


    private boolean addTargetChat(Update update) {
        sourceUpdate = null;

        bundle.setTo(update.getMessage().getChat().getId());
        bundle.setNameTo(update.getMessage().getChat().getTitle());

        bundle.setLang("");
        bundle.setKey("");
        bundle.setFlag("");

        executor.sendMessage(BotContentData.getSendMessage(bundle.getTo(),
                "Группа назначения добавлена и записана!",
                null));

        executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                "Бот добавлен в целевую группу, информация записана!\nТеперь выбери язык, на который будем переводить",
                keyboard.languages(), commonMsgId));
        bundleRedisDao.saveBundle(bundle);
        return true;
    }


    private boolean addTargetChannel(Update update) {
        sourceUpdate = null;

//        System.out.println(update.getMyChatMember().getNewChatMember().getStatus());
//        System.out.println(update.getMyChatMember().getNewChatMember().getUser().getFirstName());
//        System.out.println("update:" + update.getMyChatMember().getChat().getType());
//        System.out.println("update:" + update.getMyChatMember().getChat().getTitle());
//        System.out.println("update:" + update.getMyChatMember().getChat().getId());

        bundle.setTo(update.getMyChatMember().getChat().getId());
        bundle.setNameTo(update.getMyChatMember().getChat().getTitle());

        bundle.setLang("");
        bundle.setKey("");
        bundle.setFlag("");
        bundleRedisDao.saveBundle(bundle);

        executor.sendMessage(BotContentData.getSendMessage(bundle.getTo(),
                "Канал назначения добавлен и записан!",
                null));

        executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                "Бот добавлен в целевой канал, информация записана!\nТеперь выбери язык, на который будем переводить",
                keyboard.languages(), commonMsgId));
        return true;
    }

    private boolean bundleUpdate(Update update) {
        String[] data = parseBundleDescription(update.getMessage().getText());
        Bundle oldBundle = bundleRedisDao.getBundle(BundleRedisDao.KEY + data[0]).get();

        oldBundle.setFrom(Long.valueOf(data[1].replaceAll(" ", "")));
        oldBundle.setTo(Long.valueOf(data[2].replaceAll(" ", "")));
        oldBundle.setLang(data[3].replaceAll(" ", ""));
        oldBundle.setFlag(data[4].replaceAll(" ", ""));
        oldBundle.setKey(data[5].replaceAll(" ", ""));

        bundleRedisDao.saveBundle(oldBundle);
        sendBundleDescription(oldBundle, BundleRedisDao.KEY + data[0], -1);
        return true;
    }
}