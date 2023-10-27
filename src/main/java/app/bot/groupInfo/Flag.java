package app.bot.groupInfo;

import org.springframework.stereotype.Service;
@Service
public class Flag {
    public String getFlag(String countryCode) {
        try {
            countryCode = countryCode.toUpperCase();
            int firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
            int secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;
            return (new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter)));

        } catch (Exception e) {
        }
        return "";
    }
}
