package app.translater.bundle;

import app.bot.config.BotConfig;
import app.bot.data.BotContentData;
import app.bot.util.MessageExecutor;
import app.translater.bundle.data.BundleKeyboard;
import app.translater.bundle.model.Bundle;
import app.translater.bundle.model.Target;
import app.translater.bundle.service.RedisBundleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Service
public class BundleController {
    @Autowired
    @Lazy
    private MessageExecutor executor;
    @Autowired
    private BotConfig botConfig;
    @Autowired
    private RedisBundleService redisRepository;
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

        try {
            update.getMyChatMember().getChat().getType();
            if (update.getMyChatMember().getNewChatMember().getStatus().equals("administrator")
                    && update.getMyChatMember().getNewChatMember().getUser().getUserName().equals(getBotUserName())
                    && !sourceUpdate) {
                return addTargetChannel(update);
            }

        } catch (Exception e) {
            if (update.hasMessage()
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

        }

        return false;
    }

    private boolean handleTextMessage(Update update) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (text.equals("/start")) {
            if (redisRepository.findAll().isEmpty()) {
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

        if (text.contains("Группа для перевода:")
                && text.contains("Целевая группа:")
                && text.contains("Переводим на:")) {

            bundle = Parser.parseStringToBundle(text);
            redisRepository.deleteBundle(bundle.getFrom());
            redisRepository.saveBundle(bundle);

            executor.deleteMessage(BotContentData.getDeleteMessage(chatId, commonMsgId));
            executor.sendMessage(BotContentData.getSendMessage(getAdminChatId(), builder.toString(),
                    keyboard.editBundle(builder.toString(), bundle.getFrom(), getBotUserName())));
            return true;
        }

        return false;
    }

    private boolean handleCallbackQuery(Update update) {
        Long chatId = update.getCallbackQuery().getFrom().getId();
        String data = update.getCallbackQuery().getData();
        int msgId = update.getCallbackQuery().getMessage().getMessageId();
        commonMsgId = msgId;
        sourceUpdate = null;

        if (data.equals("BACK")) {
            executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                    "Выберите связку, которую хотите редактировать.\nСвязка названа по группе назначения",
                    keyboard.getBundles(redisRepository.findAll()), msgId));
            return true;
        }

        if (data.equals("CREATE")) {
            sourceUpdate = true;

            executor.editMessage(BotContentData.getEditMessage(chatId,
                    "Создание новой связки!\nДавай назначим группу/тему для новых сообщений, для перевода (ОТКУДА)",
                    keyboard.addBotToSourceChatOrChannel(getBotUserName()),
                    msgId));
            return true;
        }

        if (data.startsWith("DELETE_")) {
            Long from = Long.parseLong(data.split("_")[1]);

            redisRepository.deleteBundle(from);

            executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                    "Выберите связку, которую хотите редактировать.\nСвязка названа по группе назначения",
                    keyboard.getBundles(redisRepository.findAll()), msgId));
            return true;
        }

        if (data.contains("lang=") && bundle != null) {
            List<Target> targetList = bundle.getTargetGroupList();
            targetList.get(targetList.size() - 1).setLang(data.split("=")[1]);

            executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                    "Готово! Связка "
                            + bundle.getNameFrom() + " -> " + targetList.get(targetList.size() - 1).getName()
                            + " сохранена!",
                    null, msgId));

            redisRepository.saveBundle(bundle);
            bundle = new Bundle();

            return showBundleSelectionMenu();
        }

        if (data.contains("bundles")) {
            bundle = redisRepository.getBundle(Long.valueOf(data.split(":")[1]));

            if (bundle == null) {
                executor.editMessage(BotContentData.getEditMessage(chatId,
                        "Что-то пошло не так, сформируйте сообщение со связками заново нажав /start",
                        null, msgId));
                return true;

            } else {
                sourceUpdate = false;
                return sendBundleDescription(bundle.getFrom(), msgId);
            }
        }

        return false;
    }

    private boolean showBundleSelectionMenu() {
        executor.sendMessage(BotContentData.getSendMessage(getAdminChatId(),
                "Выберите связку, которую хотите редактировать.\nСвязка названа по группе назначения",
                keyboard.getBundles(redisRepository.findAll())));
        return true;
    }

    private boolean sendBundleDescription(Long from, int msgId) {
        bundle = redisRepository.getBundle(from);

        builder.setLength(0);
        builder.append("Группа для перевода: ")
                .append(bundle.getNameFrom()).append(", ID: ").append(bundle.getFrom()).append("\n#\n");

        for (Target target : bundle.getTargetGroupList()) {
            builder.append("Целевая группа: ")
                    .append(target.getName()).append(", ID: ").append(target.getChatId()).append("\n")
                    .append("Переводим на: ").append(target.getLang()).append("\n")
                    .append("Флаг: ").append(target.getFlag()).append("\n")
                    .append("API Deepl: ").append(target.getDeeplApiKey()).append("\n#\n");
        }

        if (msgId == -1) {
            executor.sendMessage(BotContentData.getSendMessage(getAdminChatId(), builder.toString(),
                    keyboard.editBundle(builder.toString(), bundle.getFrom(), getBotUserName())));
        } else {
            executor.editMessage(BotContentData.getEditMessage(getAdminChatId(), builder.toString(),
                    keyboard.editBundle(builder.toString(), bundle.getFrom(), getBotUserName()), msgId));
        }
        return true;
    }

    private boolean addSourceChat(Update update) {
        sourceUpdate = null;

        bundle = new Bundle();
        bundle.setFrom(update.getMessage().getChat().getId());
        bundle.setNameFrom(update.getMessage().getChat().getTitle());

        Target target = new Target();
        target.setLang("");
        target.setFlag("");
        target.setChatId(0L);
        target.setName("no target");
        target.setDeeplApiKey("");

        bundle.getTargetGroupList().add(target);

        redisRepository.saveBundle(bundle);

        executor.sendMessage(BotContentData.getSendMessage(bundle.getFrom(),
                "Группа перевода добавлена и записана!", null));

        executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                "Выберите связку, которую хотите редактировать.\nСвязка названа по группе перевода",
                keyboard.getBundles(redisRepository.findAll()), commonMsgId));
        return true;
    }

    private boolean addTargetChat(Update update) {
        List<Target> targetList = bundle.getTargetGroupList();

        if (targetList.size() == 1 && targetList.get(0).getChatId().equals(0L)) {
            targetList.remove(0);
        }

        Target target = new Target();
        target.setChatId(update.getMessage().getChatId());
        target.setName(update.getMessage().getChat().getTitle());

        targetList.add(target);
        bundle.setTargetGroupList(targetList);
        redisRepository.saveBundle(bundle);

        executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                "Бот добавлен в целевую группу, информация записана!\nТеперь выбери язык, на который будем переводить",
                keyboard.languages(), commonMsgId));

        return true;//sendBundleDescription(bundle.getFrom(), commonMsgId);

    }

    private boolean addTargetChannel(Update update) {
        List<Target> targetList = bundle.getTargetGroupList();

        if (targetList.size() == 1 && targetList.get(0).getChatId().equals(0L)) {
            targetList.remove(0);
        }

        Target target = new Target();
        target.setChatId(update.getMyChatMember().getChat().getId());
        target.setName(update.getMyChatMember().getChat().getTitle());

        bundle.setTargetGroupList(targetList);
        redisRepository.saveBundle(bundle);

        executor.editMessage(BotContentData.getEditMessage(getAdminChatId(),
                "Бот добавлен в целевой канал, информация записана!\nТеперь выбери язык, на который будем переводить",
                keyboard.languages(), commonMsgId));

        return true;//sendBundleDescription(bundle.getFrom(), commonMsgId);
    }
}