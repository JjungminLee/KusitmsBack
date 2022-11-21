package com.example.demo.src.controller;



import com.example.demo.src.dto.response.BaseException;
import com.example.demo.src.dto.response.GetSpotifyRes;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.dto.response.GetYouTubeRes;
import com.example.demo.src.entity.Music;
import com.example.demo.src.service.MusicService;
import com.example.demo.src.service.YoutubeService;
import com.example.demo.src.spotify.SearchTrack;
import com.example.demo.src.spotify.SpotifyToken;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/bring",produces = "application/json;charset=UTF-8")
public class MusicController {


    @Autowired
    private final SpotifyToken spotifyToken;

    @Autowired
    private final MusicService musicService;

    private final SearchTrack searchTrack;

    private final YoutubeService youtubeService;

    public MusicController(SpotifyToken spotifyToken, MusicService musicService, SearchTrack searchTrack, YoutubeService youtubeService) {
        this.spotifyToken = spotifyToken;
        this.musicService = musicService;
        this.searchTrack = searchTrack;
        this.youtubeService = youtubeService;
    }




    @Operation(summary = "spotify원본 api", description = "/bring/spotify?track={track이름}")
    @GetMapping("/spotify")
    public BaseResponse<Paging<Track>>searchTrack(@RequestParam("track")String track) throws BaseException{

        try{
            String token=spotifyToken.getAccessToken();
            Paging<Track> trackList=searchTrack.searchTracks(token,track);
            return new BaseResponse<>(trackList);
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    @Operation(summary = "track,trackIdx,artist만 페이징 개수만큼 ", description = "/bring/spotify/track?track={track이름}")
    @GetMapping("/spotify/track")
    public BaseResponse<List<GetSpotifyRes>> parsingSearchTrack(@RequestParam("track")String track) throws IOException, BaseException {

        try{
            List<Music> musicList=musicService.parsingSearchTrack(track);
            List<GetSpotifyRes>getSpotifyDtos=musicList.stream()
                    .map(GetSpotifyRes::getSpotifyDto).collect(Collectors.toList());
            return new BaseResponse<>(getSpotifyDtos);
        }catch (BaseException e){
            return  new BaseResponse<>(e.getStatus());
        }

    }

    // 가사 받아오는 api
    @Operation(summary = "가사호출  ", description = "/bring/spotify/lyrics?trackid={스포티파이trackIdx}")
    @GetMapping("/spotify/lyrics")
    public BaseResponse<List<String>> searchLyrics(@RequestParam("trackid")String trackid) throws IOException,BaseException {

        try{
            return new BaseResponse<>(musicService.searchLyrics(trackid));
        }catch (BaseException e){
            return  new BaseResponse<>(e.getStatus());
        }

    }


    //유튜브 videoId 받아오는 api
    @Operation(summary = "유튜브 videoID 받아오기  ", description = "/bring/youtube?track={노래이름}&artist={가수이름}")
    @GetMapping("/youtube")
    public BaseResponse<List<GetYouTubeRes>>searchVideoId(@RequestParam(value="track")String track,@RequestParam(value="artist")String artist) throws BaseException {

        try{
            String searchQuery = track.concat(" ").concat(artist);
            List<GetYouTubeRes> getYouTubeResList = youtubeService.youTubeSearch(track,artist,searchQuery, 1);
            return new BaseResponse<>(getYouTubeResList);
        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }


    }


    @GetMapping("/spotify/access")
    public BaseResponse<String>accessToken(){
        String token=spotifyToken.getAccessToken();

        return new BaseResponse<>(token);
    }
}
