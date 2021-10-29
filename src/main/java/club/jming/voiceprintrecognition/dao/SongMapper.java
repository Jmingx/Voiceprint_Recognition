package club.jming.voiceprintrecognition.dao;

import club.jming.voiceprintrecognition.pojo.Song;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface SongMapper {

    @Insert("insert into song (song_name,upload_date,path) values (#{songName},#{uploadDate},#{path})")
    int insert(Song song);

    @Select("select id from song where song_name = #{songName}")
    Integer getID(String songName);

    @Select("select * from song where id = #{id}")
    Song getSongByID(int id);

    @Select("select * from song")
    List<Song> getAllSongs();
}
