package app.translater.bundle.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Target {
    private Long chatId;
    private String name;
    private String lang;
    private String flag;
    private String deeplApiKey;
}
