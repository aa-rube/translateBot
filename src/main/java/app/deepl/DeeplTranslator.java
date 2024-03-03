package app.deepl;

import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import org.springframework.stereotype.Service;

@Service
public class DeeplTranslator {
    public String goTranslate(String authKey, String string, String direction) {
        Translator translator = new Translator(authKey);

        try {
            TextResult result = translator.translateText(string, null, direction);
            return result.getText();

        } catch (Exception e) {
            return e.toString();
        }
    }
}