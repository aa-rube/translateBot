package app.bot.service;

import app.translator.DeeplTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.Comparator;
import java.util.List;

@Component
public class Formatter {
    @Autowired
    private DeeplTranslator deepl;

    public String plainText(String authKey, String text, String direction) {
        return deepl.goTranslate(authKey, text, direction);
    }

    public String getTextAndEntities(String authKey, String text, List<MessageEntity> entities, String direction) {
        StringBuilder marking = new StringBuilder(text);
        int offsetCorrection = 0;
        entities.sort(Comparator.comparingInt(MessageEntity::getOffset));

        for (int i = entities.size() - 1; i >= 0; i--) {
            MessageEntity e = entities.get(i);
            int start = e.getOffset();
            int end = start + e.getLength();

            String strStart = "";
            String strEnd = "";

            boolean overlap = false;
            for (int j = i + 1; j < entities.size(); j++) {
                MessageEntity nextEntity = entities.get(j);
                int nextStart = nextEntity.getOffset();
                int nextEnd = nextStart + nextEntity.getLength();
                if (nextStart <= start && nextEnd >= end) {
                    overlap = true;
                    break;
                }
            }

            if (!overlap) {
                switch (e.getType()) {
                    case "bold":
                        strStart = "<b>";
                        strEnd = "</b>";
                        break;
                    case "italic":
                        strStart = "<i>";
                        strEnd = "</i>";
                        break;
                    case "underline":
                        strStart = "<u>";
                        strEnd = "</u>";
                        break;
                    case "text_link":
                        String url = escapeHtml(e.getUrl());
                        String linkText = marking.substring(start + offsetCorrection, end + offsetCorrection);
                        String replacement = "<a href=\"" + url +"\">" + linkText + "</a>";
                        marking.replace(start + offsetCorrection, end + offsetCorrection, replacement);
                        break;
                    case "strikethrough":
                        strStart = "<s>";
                        strEnd = "</s>";
                        break;
                }

                marking.insert(start + offsetCorrection, strStart);
                marking.insert(end + offsetCorrection + strStart.length(), strEnd);
            }
        }

        return deepl.goTranslate(authKey, marking.toString().trim(), direction);
    }

    private String escapeHtml(String input) {
        input = input.replace("&", "&amp;");
        input = input.replace("<", "&lt;");
        input = input.replace(">", "&gt;");
        return input;
    }

}