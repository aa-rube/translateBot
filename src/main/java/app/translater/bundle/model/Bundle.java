package app.translater.bundle.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Bundle {
    private String nameTo;
    private String nameFrom;

    private String key;
    private String lang;

    private Long to;
    private Long from;

    private String flag;
}