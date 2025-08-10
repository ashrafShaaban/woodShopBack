package tmd.tmdAdmin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import tmd.tmdAdmin.data.dto.VideoDTO;
import tmd.tmdAdmin.data.entities.Videos;
import tmd.tmdAdmin.data.repositories.VideosRepository;

@Controller
public class VideoController {
   @Autowired
   private VideosRepository videosRepository;
    @GetMapping("/addVideo")
    public String addVideoForm(Model model){
        Videos video=new Videos();
        model.addAttribute("video",video);
        return "addVideo";
    }
        @PostMapping("/saveVideo")
        public String saveVideo(@ModelAttribute Videos video)
        {
            videosRepository.save(video);
            return"redirect:/dashboard";
        }

}
