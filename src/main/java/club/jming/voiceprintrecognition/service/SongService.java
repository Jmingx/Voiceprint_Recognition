package club.jming.voiceprintrecognition.service;

import club.jming.voiceprintrecognition.pojo.Song;

import java.util.List;
import java.util.Map;

public interface SongService {

    /**
     * 持久化song
     * @param song
     * @return
     */
    int insertSong(Song song);

    /**
     * 查找音乐的名字
     * @param songName
     * @return
     */
    Integer getID(String songName);

    /**
     * 为对应的song创建音乐指纹
     * @param song
     */
    void createSongPrint(Song song);

    /**
     * 匹配
     * @param filePath
     * @return
     */
    Map<Integer, Float> match(String filePath);

    /**
     * 按照songID查找对应的歌曲
     * @param songID
     * @return
     */
    Song getSongByID(int songID);


    /**
     * 查找所有的歌曲
     * @return
     */
    List<Song> getAllSongs();
}
