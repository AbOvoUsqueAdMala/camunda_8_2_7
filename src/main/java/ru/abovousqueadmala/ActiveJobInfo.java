package ru.abovousqueadmala;

import lombok.Data;

@Data
public class ActiveJobInfo {

    private Long key;
    private String intent;
    private String timestamp;
    private Long position;

    private String type;
    private String worker;
    private Long timeout;
    private Long deadline;
    private Long processInstanceKey;
    private String elementId;

}