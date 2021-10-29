package club.jming.voiceprintrecognition.controller;

import club.jming.voiceprintrecognition.pojo.Song;
import club.jming.voiceprintrecognition.pojo.SongDTO;
import club.jming.voiceprintrecognition.pojo.result.Result;
import club.jming.voiceprintrecognition.service.SongService;
import club.jming.voiceprintrecognition.utils.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/song")
@Slf4j
public class SongController {

    @Autowired
    private SongService songService;

    @RequestMapping(value = "/uploadSong", method = RequestMethod.POST)
    public Result uploadSong(@RequestParam("song") MultipartFile uploadSong, @RequestParam("songName") String songName) {
        Result result = null;
        if (uploadSong == null) {
            result = ResultUtil.error(-1, "文件不存在");
        } else {
            String filePath = System.getProperty("user.dir") + System.getProperty("file.separator") + "song" + System.getProperty("file.separator") + songName;
            Date uploadDate = new Date();

            File targetFile = new File(filePath);

            try {
                uploadSong.transferTo(targetFile);
            } catch (IOException e) {
                log.error("文件保存失败");
                e.printStackTrace();
                result = ResultUtil.error(-2, "文件保存出错");
                return result;
            }

            Song song = new Song();
            song.setSongName(songName);
            song.setUploadDate(uploadDate);
            song.setPath(filePath);

            if (songService.getID(songName) == null) {
                //音乐对象持久化
                songService.insertSong(song);
                song.setId(songService.getID(songName));

                //制作音乐指纹
                songService.createSongPrint(song);
            }
            result = ResultUtil.success();
        }
        return result;
    }

    @RequestMapping(value = "/match", method = RequestMethod.POST)
    public Result match(@RequestParam("song") MultipartFile uploadSong, @RequestParam("songName") String songName) {
        Result result = null;
        if (uploadSong == null) {
            result = ResultUtil.error(-1, "文件不存在");
        } else {
            String filePath = System.getProperty("user.dir") + System.getProperty("file.separator") + "tmp" + System.getProperty("file.separator") + songName;
            Date uploadDate = new Date(System.currentTimeMillis());

            File targetFile = new File(filePath);

            try {
                uploadSong.transferTo(targetFile);
            } catch (IOException e) {
                log.error("文件保存失败");
                e.printStackTrace();
                result = ResultUtil.error(-2, "文件保存出错");
                return result;
            }

            Map<Integer, Float> compareResult = songService.match(filePath);

            List<SongDTO> list = new LinkedList<>();

            for (Integer songID : compareResult.keySet()) {
//                //过滤掉匹配值小于1%的数据
//                if (compareResult.get(songID) <= 0.01f){
//                    continue;
//                }
                SongDTO songDTO = new SongDTO();
                Song song = songService.getSongByID(songID);
                songDTO.setSongID(song.getId());
                songDTO.setSongName(song.getSongName());
                songDTO.setRate(compareResult.get(songID));
                list.add(songDTO);
            }

            Collections.sort(list, new Comparator<SongDTO>() {
                @Override
                public int compare(SongDTO o1, SongDTO o2) {
                    if (o1.getRate()-o2.getRate()>0){
                        return -1;
                    }else {
                        return 1;
                    }
                }
            });

            result = ResultUtil.success(list);
        }
        return result;
    }

    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public Result getAllSongs(){
        List<Song> allSongs = songService.getAllSongs();
        return ResultUtil.success(allSongs);
    }
}
