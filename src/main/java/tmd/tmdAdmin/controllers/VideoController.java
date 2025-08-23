package tmd.tmdAdmin.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tmd.tmdAdmin.data.entities.Videos;
import tmd.tmdAdmin.data.entities.VideosType;
import tmd.tmdAdmin.data.repositories.VideoTypeRepository;
import tmd.tmdAdmin.data.repositories.VideosRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class VideoController {
    @InitBinder
    public void initBinder(WebDataBinder dataBinder){

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
   @Autowired
   private VideosRepository videosRepository;
   @Autowired
   private VideoTypeRepository videoTypeRepository;

    @GetMapping("/addVideoType")
    public String addVideoForm(Model model){
        VideosType video=new VideosType();
        model.addAttribute("video",video);
        return "addVideoType";
    }
        @PostMapping("/saveVideo")
        public String saveVideo(@Valid @ModelAttribute("video") VideosType video,BindingResult bindingResult, @RequestParam("videoFile" ) MultipartFile videoFile,Model model) throws IOException {
           if(bindingResult.hasErrors()){
               return "addVideoType";
           }
            if(videoFile.isEmpty()){
                return "redirect:/addVideoType";
            }
            try {
                String folder="C:/videos/";
                String filename= UUID.randomUUID()+ "_" +videoFile.getOriginalFilename();
                Path filePath= Paths.get(folder,filename);
                Files.write(filePath,videoFile.getBytes());

                VideosType videoType= new VideosType();
                videoType.setName(video.getName());
                videoType.setNameAr(video.getNameAr());
                videoType.setNameRu(video.getNameRu());
                videoType.setPath("/videos/"+filename);

                videoTypeRepository.save(videoType);
                return"redirect:/seeVideos";
            }
            catch (DataIntegrityViolationException e) {
                model.addAttribute("error", "the name is must be unique");
                return "addVideoType";
            }

        }
        @PostMapping("/updatevideoType")
        public String updatevideoT(@RequestParam("videoId") int id,Model model){
          VideosType updatedvideo=videoTypeRepository.findById(id).orElseThrow();
          model.addAttribute("updatedvideo",updatedvideo);
          return "updateVideoType";
        }
        @PostMapping("/saveUpdatedVideo")
        public String saveUpdatedVideo(@Valid @ModelAttribute("updatedvideo") VideosType updatedvideo,BindingResult bindingResult,@RequestParam("newvideoURL") MultipartFile newvideoURL, @RequestParam("oldvideoURL") String oldvideoURL,Model model) throws IOException {
           if(bindingResult.hasErrors()){
               return "updateVideoType";
           }
        try {
               if (!newvideoURL.isEmpty()) {
                   if (updatedvideo.getPath() != null) {
                       String folder = "C:/videos/" + Paths.get(updatedvideo.getPath());
                       File oldimgFile = new File(folder);
                       if (oldimgFile.exists()) oldimgFile.delete();
                   }

                   String newimgFolder = "C:/videos/";
                   String newImgname = UUID.randomUUID() + "_" + newvideoURL.getOriginalFilename();
                   Path newpath = Paths.get(newimgFolder, newImgname);
                   Files.write(newpath, newvideoURL.getBytes());
                   updatedvideo.setPath("/videos/" + newImgname);
               }
               else {

                   updatedvideo.setPath(oldvideoURL);
               }
               videoTypeRepository.save(updatedvideo);
               return "redirect:/seeVideos";
           }
           catch (DataIntegrityViolationException e) {
               model.addAttribute("error", "the name is must be unique");
               return "updateVideoType";
           }
        }
        @PostMapping("/deletevideoType")
        public String deleteVideoType(@RequestParam("videoId") int deletedone){
          VideosType deletedvideo=videoTypeRepository.findById(deletedone).orElseThrow();
          videoTypeRepository.delete(deletedvideo);
          return "redirect:/seeVideos";
        }
    @GetMapping("/videoGalaryType/{id}")
    public String galleryType(@PathVariable("id") int typeid, Model model, Principal principal){
        System.out.println(typeid);
        List<Videos> videos=videosRepository.findAllByTypeId(typeid);
        model.addAttribute("videos",videos);
        model.addAttribute("TypeId",typeid);
        if(principal !=null){
            model.addAttribute("username",principal.getName());
        }
        return "videoType_Details";

    }

    @GetMapping("/addvideoinvideotype/{typeId}")
    public String additeminGType(@PathVariable("typeId") int id,Model model){

        Videos video=new Videos();
        VideosType videosType= videoTypeRepository.findById(id).orElseThrow();
        model.addAttribute("video",video);
        model.addAttribute("videoType",videosType);
        return "addvideoinVideoType";
    }
    @PostMapping("/savevideoinVideoType")
    public String saveitemType(@Valid @ModelAttribute("video") Videos video, @RequestParam("typeId") int typeId, @Valid @RequestParam("videoFile")MultipartFile videoURL, BindingResult bindingResult ) throws IOException {
        if(bindingResult.hasErrors()){
            return  "addvideoinVideoType";
        }
        String folder="C:/videos/";
        String filename= UUID.randomUUID() + "_" + videoURL.getOriginalFilename();
        Path filepath= Paths.get(folder,filename);
        Files.write(filepath,videoURL.getBytes());

        Videos videoitem=new Videos();
        videoitem.setPath("/videos/"+filename);
        videoitem.setTypeId(videoTypeRepository.findById(typeId).orElseThrow().getId());



        videosRepository.save(videoitem);
        System.out.println(typeId);

        return "redirect:/videoGalaryType/" + typeId;

    }
    @PostMapping("/updateVideo")
    public String updateiteminGType(@RequestParam("itemId") int id,Model model){
        Videos video=videosRepository.findById(id).orElseThrow();
        model.addAttribute("video",video);
        return "updateVideo";
    }
    @PostMapping("/saveVideoAfterUpdate")
    public String saveVideoafterUpdate(@Valid @ModelAttribute("video") Videos video, @RequestParam("newvideoURL") MultipartFile newURL, @RequestParam("oldvideoURl") String oldURL ) throws IOException {
        //       if(bindingResult.hasErrors()){
        //           return "updateForm";
        //       }

        if (!newURL.isEmpty()) {
            if (video.getPath() != null) {
                String folder = "C:/videos/" + Paths.get(video.getPath());
                File oldvideoFile = new File(folder);
                if (oldvideoFile.exists()) oldvideoFile.delete();
            }

            String newvideoFolder = "C:/videos/";
            String newvideoname = UUID.randomUUID() + "_" + newURL.getOriginalFilename();
            Path newpath = Paths.get(newvideoFolder, newvideoname);
            Files.write(newpath, newURL.getBytes());
            video.setPath("/videos/" + newvideoname);
        }
        else {
            System.out.println(oldURL);
            video.setPath(oldURL);
        }
        int typeId=video.getTypeId();
        videosRepository.save(video);
        return  "redirect:/videoGalaryType/" + typeId;
    }
    @PostMapping("/deleteVideo")
    public String deleteiteminGType(@RequestParam("itemId") int deletedone){
        Videos deletevideo=videosRepository.findById(deletedone).orElseThrow();
        int typeId =deletevideo.getTypeId();
        videosRepository.delete(deletevideo);
        return "redirect:/videoGalaryType/" + typeId;
    }



}
