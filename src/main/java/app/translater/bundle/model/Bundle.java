package app.translater.bundle.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Bundle {
    private Long from;
    private String nameFrom;
    private List<Target> targetGroupList = new ArrayList<>();
}