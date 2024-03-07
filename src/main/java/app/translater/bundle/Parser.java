package app.translater.bundle;

import app.translater.bundle.model.Bundle;
import app.translater.bundle.model.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public static Bundle parseStringToBundle(String input) {
        String[] parts = input.split("#");
        String groupInfo = parts[0];

        String groupName = groupInfo.substring(groupInfo.indexOf(':') + 1, groupInfo.indexOf(',')).trim();
        Long groupId = parseId(groupInfo.substring(groupInfo.indexOf("ID:") + 3).trim());

        Bundle bundle = new Bundle();
        bundle.setNameFrom(groupName);
        bundle.setFrom(groupId);

        for (int i = 1; i < parts.length; i++) {
            String targetInfo = parts[i].trim();
            Target target = new Target();

            String name = targetInfo.substring(targetInfo.indexOf(':') + 1, targetInfo.indexOf(',')).trim();
            Long chatId = parseId(targetInfo.substring(targetInfo.indexOf("ID:") + 3).trim());
            String lang = extractValue(targetInfo, "Переводим на:");
            String flag = extractValue(targetInfo, "Флаг:");
            String deeplApiKey = extractValue(targetInfo, "API Deepl:");

            target.setName(name);
            target.setChatId(chatId);
            target.setLang(lang);
            target.setFlag(flag);
            target.setDeeplApiKey(deeplApiKey);

            bundle.getTargetGroupList().add(target);
        }

        return bundle;
    }

    private static String extractValue(String targetInfo, String marker) {
        int start = targetInfo.indexOf(marker) + marker.length();
        int end = targetInfo.indexOf('\n', start);
        if (end == -1) {
            end = targetInfo.length();
        }
        return targetInfo.substring(start, end).trim();
    }

    private static Long parseId(String text) {
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Long.parseLong(matcher.group());
        }
        throw new IllegalArgumentException("Invalid ID format: " + text);
    }


//    public static void main(String[] args) {
//        // Тестирование метода
//        String input = "@aar_botfortest_bot Группа для перевода: test test, ID: -1002038821841\n" +
//                "\n" +
//                "Целевая группа: no target, ID: 0\n" +
//                "Переводим на: \n" +
//                "Флаг: \n" +
//                "API Deepl: \n" +
//                "#\n" +
//                "Целевая группа: photo storage, ID: -1001996648766\n" +
//                "Переводим на: ES\n" +
//                "Флаг: null\n" +
//                "API Deepl: dgfhfhgdghjjgh\n" +
//                "#";
//        Bundle bundle = parseStringToBundle(input);
//        System.out.println(bundle.getFrom());
//        System.out.println(bundle.getNameFrom());
//        System.out.println("$$$$$$$$$$$$$$$");
//
//        for (Target t : bundle.getTargetGroupList()) {
//            System.out.println(t.getName());
//            System.out.println(t.getChatId());
//            System.out.println(t.getLang());
//            System.out.println(t.getFlag());
//            System.out.println(t.getDeeplApiKey());
//            System.out.println("_________________");
//        }
//    }
}
