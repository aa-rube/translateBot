package app.translater.bundle;

import app.bot.config.BotConfig;
import app.bot.data.Messages;
import app.bot.util.MessageExecutor;
import app.translater.bundle.model.Bundle;
import app.translater.bundle.redis.BundleRedisDao;
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
    private Boolean sourceGroupUpdate;
    private Integer commonMsgId;

    private Long getAdminChatId() {
        return botConfig.getSuperUserChatId();
    }

    private String getBotUserName() {
        return botConfig.getBotUsername();
    }

    public boolean handleUpdate(Update update) {
        if (update.getMessage() != null && update.getMessage().getNewChatMembers() != null
                && update.getMessage().getText() == null && update.getMessage().getLeftChatMember() == null) {

            if (sourceGroupUpdate != null) {
                if (sourceGroupUpdate) {
                    return addSourceGroup(update);
                } else {
                    return addTargetGroup(update);
                }
            }
        }

        if (update.getMessage() != null && update.getMessage().hasText()
                && update.getMessage().getFrom().getId().equals(getAdminChatId())) {
            return handleTextMessage(update);
        }

        if (update.hasCallbackQuery()) {
            executor.sendCallBackAnswer(Messages.getCallbackQueryAnswer(update));
            return handleCallbackQuery(update);
        }

        return false;
    }

    private boolean handleTextMessage(Update update) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (text.equals("/start")) {
            if (bundleRedisDao.getAllBundles().isEmpty()) {
                executor.sendMessage(Messages.getSendMessage(chatId,
                        "Кажется ни одной связки не создано!\nНачни прямо сейчас!",
                        keyboard.create()));
                return true;
            }

            return showBundleSelectionMenu();
        }

        if (text.equals("/start@".concat(getBotUserName()).concat(" true"))) {

            if (sourceGroupUpdate != null) {
                if (sourceGroupUpdate) {
                    return addSourceGroup(update);
                } else {
                    return addTargetGroup(update);
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

            executor.editMessage(Messages.getEditMessage(getAdminChatId(),
                    "Выберите связку, которую хотите редактировать.\nСвязка названа по группе назначения",
                    keyboard.getBundles(bundleRedisDao.getAllBundles()), msgId));
            return true;
        }

        if (data.equals("CREATE")) {
            sourceGroupUpdate = true;
            commonMsgId = msgId;

            executor.editMessage(Messages.getEditMessage(chatId,
                    "Создание новой связки!\nДавай назначим группу перевода",
                    keyboard.addBotToGroup(getBotUserName(), "Добавить в группу перевода"),
                    msgId));
            return true;
        }

        if (data.contains("lang=") && bundle != null) {
            bundle.setLang(data.split("=")[1]);

            executor.editMessage(Messages.getEditMessage(getAdminChatId(),
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
                executor.editMessage(Messages.getEditMessage(chatId,
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
            executor.sendMessage(Messages.getSendMessage(getAdminChatId(), builder.toString(),
                    keyboard.editBundle(builder.toString(), data)));
            return true;
        }

        executor.editMessage(Messages.getEditMessage(getAdminChatId(), builder.toString(),
                keyboard.editBundle(builder.toString(), data), msgId));
        return true;
    }

    private boolean showBundleSelectionMenu() {
        executor.sendMessage(Messages.getSendMessage(getAdminChatId(),
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

    private boolean addSourceGroup(Update update) {
        sourceGroupUpdate = false;

        bundle.setFrom(update.getMessage().getChat().getId());
        bundle.setNameFrom(update.getMessage().getChat().getTitle());

        executor.sendMessage(Messages.getSendMessage(bundle.getFrom(),
                "Группа перевода добавлена и записана!",
                null));

        executor.editMessage(Messages.getEditMessage(getAdminChatId(),
                "Группа перевода добавлена и записана!\n\nТеперь добавим в целевую группу назначения",
                keyboard.addBotToGroup(getBotUserName(), "Добавить в группу назначения"), commonMsgId));
        return true;

    }

    private boolean addTargetGroup(Update update) {
        sourceGroupUpdate = null;

        bundle.setTo(update.getMessage().getChat().getId());
        bundle.setNameTo(update.getMessage().getChat().getTitle());

        bundle.setLang("");
        bundle.setKey("");
        bundle.setFlag("");

        executor.sendMessage(Messages.getSendMessage(bundle.getTo(),
                "Группа назначения добавлена и записана!",
                null));

        executor.editMessage(Messages.getEditMessage(getAdminChatId(),
                "Бот добавлен в целевую группу, информация записана!\nТеперь выбери язык, на который будем переводить",
                keyboard.languages(), commonMsgId));
        bundleRedisDao.saveBundle(bundle);
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