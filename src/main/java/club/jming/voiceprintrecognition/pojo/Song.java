package club.jming.voiceprintrecognition.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class Song {
    private int id;
    private String songName;
    private Date uploadDate;
    private String path;
}

