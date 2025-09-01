package tmd.tmdAdmin.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tmd.tmdAdmin.data.entities.Gallery;
import tmd.tmdAdmin.data.entities.GalleryType;
import tmd.tmdAdmin.data.entities.Videos;
import tmd.tmdAdmin.data.entities.VideosType;
import tmd.tmdAdmin.data.repositories.GalleryRepository;
import tmd.tmdAdmin.data.repositories.GalleryTypeRepository;
import tmd.tmdAdmin.data.repositories.VideoTypeRepository;
import tmd.tmdAdmin.data.repositories.VideosRepository;
import tmd.tmdAdmin.storage.FileStorageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class VideoController {
    private final VideosRepository videosRepository;
    private final VideoTypeRepository videoTypeRepository;
    private final FileStorageService fileStorageService;

    @GetMapping("/addVideotype")
    public String addVideoForm(Model model, HttpServletRequest request){
        VideosType videoType=new VideosType();
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", "Add New Gallery | El Dahman");

        model.addAttribute("videoType",videoType);
        return "add-videoGallery-form";
    }
        @PostMapping("/saveVideoType")
        public String addGalleryType(@Valid @ModelAttribute("videoType") VideosType videoType,BindingResult bindingResult, // Bind album properties (name, nameAr, nameRu)
                                     @RequestParam("coverVideoFile") MultipartFile coverVideoFile, // Album cover image file
                                     @RequestParam("albumVideoFiles") MultipartFile[] albumVideoFiles, // Album content image files
                                     RedirectAttributes redirectAttributes,Model model,HttpServletRequest request) {
            if (bindingResult.hasErrors()) {
                model.addAttribute("currentUri", request.getRequestURI()); // Keep currentUri for layout
                model.addAttribute("pageTitle", "Create New Album | El Dahman");
                model.addAttribute("videoType",videoType);
                return "add-videoGallery-form"; // Return to the form with errors
            }
            if (videoType.getId() == 0 && videoTypeRepository.findByName(videoType.getName()).isPresent()) { // Only check for new albums
                bindingResult.rejectValue("name", "name.duplicate", "Album with this name already exists.");
                model.addAttribute("currentUri", request.getRequestURI());
                model.addAttribute("pageTitle", "Create New Album | El Dahman");
                return "add-videoGallery-form";
            }
            try {
                if (videoTypeRepository.findByName(videoType.getName()).isPresent()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Gallery album with this name already exists.");
                    return "redirect:/addVideotype";
                }
                if (coverVideoFile == null || coverVideoFile.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Please upload a cover image for the album.");
                    return "redirect:/addVideotype";
                }
                if (albumVideoFiles == null || albumVideoFiles.length == 0 || albumVideoFiles[0].isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Please upload at least one image for the album content.");
                    return "redirect:/addVideotype";
                }
                String coverVideoPath = fileStorageService.storeGalleryTypeCover(coverVideoFile);
                videoType.setPath(coverVideoPath); // Set the path directly to GalleryType.path


                VideosType savedGalleryType = videoTypeRepository.save(videoType); // galleryType.id will be set after save

                // 3. Handle Album Content Image Uploads
                for (MultipartFile file : albumVideoFiles) {
                    if (!file.isEmpty()) {
                        // This creates a 'Gallery' entity (which represents an image)
                        // We can pass original filename as caption for Gallery.name
                        Videos albumVideo = fileStorageService.storeGalleryContentVideo(file, savedGalleryType, file.getOriginalFilename());
                        albumVideo.setVideosType(savedGalleryType);
                        videosRepository.save(albumVideo); // Save the image entity
                        savedGalleryType.addVideo(albumVideo); // Add image to album's list
                    }
                }
                videoTypeRepository.save(savedGalleryType); // Re-save to ensure image list is updated

                redirectAttributes.addFlashAttribute("successMessage", "Gallery album '" + savedGalleryType.getName() + "' and images added successfully!");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload images: " + e.getMessage());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            }
            return "redirect:/seeVideos";

        }
    @GetMapping("/viewVideos/{videoTypeId}")
    public String viewGalleryImages(@PathVariable("videoTypeId") Integer videoTypeId, Model model, HttpServletRequest request) {
        VideosType videoType = videoTypeRepository.findById(videoTypeId)
                .orElseThrow(() -> new RuntimeException("Video album not found with ID: " + videoTypeId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        model.addAttribute("username", currentUserName);
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", videoType.getName() + " Videos | El Dahman");

        model.addAttribute("videoType", videoType);
        model.addAttribute("videos", videoType.getVideos()); // Pass individual images to the template
        model.addAttribute("pageSpecificCss", new String[]{"/css/gallery.css"});

        return "view-videos";  // New template for viewing images in an album
    }
    @GetMapping("/updatevideoType")
    public String updateitem(@RequestParam("itemId") int id,Model model){
        System.out.println(id);
        VideosType updateditem=videoTypeRepository.findById(id).orElseThrow();
        model.addAttribute("updatedItem",updateditem);
        return "edit-video-form";
    }
    @PostMapping("/updatevideoType")
    public String saveUpdate(
            @Valid @ModelAttribute("updatedItem") VideosType itemGallery,
            BindingResult bindingResult,Model model,
            @RequestParam(value = "newCoverImageFile", required = false) MultipartFile newCoverImageFile,
            @RequestParam(value = "oldimageURL", required = false) String oldURL,
            RedirectAttributes redirectAttributes
             ,HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUri", request.getRequestURI()); // Keep currentUri for layout
            model.addAttribute("pageTitle", "Create New Album | El Dahman");
            model.addAttribute("updatedItem",itemGallery);
            return "edit-video-form"; // back to form with validation errors
        }
        Optional<VideosType> existingByName = videoTypeRepository.findByName(itemGallery.getName());
        if (existingByName.isPresent() && existingByName.get().getId() !=itemGallery.getId()) {
            bindingResult.rejectValue("name", "name.duplicate", "Gallery with this name already exists.");
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("pageTitle", "Edit Gallery | El Dahman");
            return "edit-video-form";
        }

        try {
            // 1. Load the existing entity from DB
            VideosType existing = videoTypeRepository.findById(itemGallery.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Album not found with id " + itemGallery.getId()));

            // 2. Update text fields
            existing.setName(itemGallery.getName());
            existing.setNameAr(itemGallery.getNameAr());
            existing.setNameRu(itemGallery.getNameRu());

            // 3. Handle cover image
            if (newCoverImageFile != null && !newCoverImageFile.isEmpty()) {
                // Upload new cover → replace
                String coverImagePath = fileStorageService.storeGalleryTypeCover(newCoverImageFile);
                existing.setPath(coverImagePath);
            } else {
                // Keep the old one
                existing.setPath(oldURL);
            }

            // 4. Save back
            videoTypeRepository.save(existing);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Gallery album '" + existing.getName() + "' updated successfully!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload cover image: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
        }

        return "redirect:/seeVideos";
    }
    @PostMapping("/deleteAlbum")
    public String remove(@RequestParam("itemId") int deletedone){
        VideosType deleteGallery=videoTypeRepository.findById(deletedone).orElseThrow();
        List<Videos> videos=videosRepository.findAllByVideosType_Id(deletedone);

        videosRepository.saveAll(videos);
        videoTypeRepository.delete(deleteGallery);
        return "redirect:/seeVideos";
    }
//    @PostMapping("/deleteitemGType")
//    public String remove(@RequestParam("itemId") int deletedone){
//        GalleryType deleteGallery=galleryTypeRepository.findById(deletedone).orElseThrow();
//        List<Gallery> galleries=galleryRepository.findAllByGalleryType_Id(deletedone);
//
//        galleryRepository.saveAll(galleries);
//        galleryTypeRepository.delete(deleteGallery);
//        return "redirect:/gallery";
//    }


    @GetMapping("/addVideosToAlbum/{typeId}")
    public String addGallery(@PathVariable("typeId") int id,Model model){
        System.out.println(id);
        Videos video=new Videos();
        VideosType videoType= videoTypeRepository.findById(id).orElseThrow();
        model.addAttribute("video",video);
        model.addAttribute("videoType",videoType);
        return "add-video-to-album-form";
    }
    @PostMapping("/addVideosToAlbum/{typeId}")
    public String saveGallery(@RequestParam("newAlbumVideoFiles") MultipartFile newVideoURL,@PathVariable("typeId") int typeId,Model model ) throws IOException {
//        if(bindingResult.hasErrors()){
//            GalleryType galleryType= galleryTypeRepository.findById(typeId).orElseThrow();
//            model.addAttribute("galleryType",galleryType);
//            return  "additeminGType";
//        }
//    try {
        Videos video=new Videos();
        if (newVideoURL != null && !newVideoURL.isEmpty()) {
            // Upload new cover → replace
            String newVideoePath = fileStorageService.storeGalleryTypeCover(newVideoURL);
            video.setPath(newVideoePath);
            video.setName(newVideoURL.getOriginalFilename());
        }




        video.setVideosType(videoTypeRepository.findById(typeId).orElseThrow());

        videosRepository.save(video);


        return "redirect:/viewVideos/" + typeId;


    }
    @PostMapping("/deleteVideo")
    public String deleteiteminGType(@RequestParam("videoId") int deletedone,@RequestParam("videoTypeId") int typeId){
        Videos deleteVideo=videosRepository.findById(deletedone).orElseThrow();

        videosRepository.delete(deleteVideo);
        return "redirect:/viewVideos/" + typeId;
    }
//        @PostMapping("/updatevideoType")
//        public String updatevideoT(@RequestParam("videoId") int id,Model model){
//          VideosType updatedvideo=videoTypeRepository.findById(id).orElseThrow();
//          model.addAttribute("updatedvideo",updatedvideo);
//          return "updateVideoType";
//        }
//        @PostMapping("/saveUpdatedVideo")
//        public String saveUpdatedVideo(@Valid @ModelAttribute("updatedvideo") VideosType updatedvideo,BindingResult bindingResult,@RequestParam("newvideoURL") MultipartFile newvideoURL, @RequestParam("oldvideoURL") String oldvideoURL,Model model) throws IOException {
//           if(bindingResult.hasErrors()){
//               return "updateVideoType";
//           }
//        try {
//               if (!newvideoURL.isEmpty()) {
//                   if (updatedvideo.getPath() != null) {
//                       String folder = "C:/videos/" + Paths.get(updatedvideo.getPath());
//                       File oldimgFile = new File(folder);
//                       if (oldimgFile.exists()) oldimgFile.delete();
//                   }
//
//                   String newimgFolder = "C:/videos/";
//                   String newImgname = UUID.randomUUID() + "_" + newvideoURL.getOriginalFilename();
//                   Path newpath = Paths.get(newimgFolder, newImgname);
//                   Files.write(newpath, newvideoURL.getBytes());
//                   updatedvideo.setPath("/videos/" + newImgname);
//               }
//               else {
//
//                   updatedvideo.setPath(oldvideoURL);
//               }
//               videoTypeRepository.save(updatedvideo);
//               return "redirect:/seeVideos";
//           }
//           catch (DataIntegrityViolationException e) {
//               model.addAttribute("error", "the name is must be unique");
//               return "updateVideoType";
//           }
//        }
//        @PostMapping("/deletevideoType")
//        public String deleteVideoType(@RequestParam("videoId") int deletedone){
//          VideosType deletedvideo=videoTypeRepository.findById(deletedone).orElseThrow();
//          videoTypeRepository.delete(deletedvideo);
//          return "redirect:/seeVideos";
//        }
//    @GetMapping("/videoGalaryType/{id}")
//    public String galleryType(@PathVariable("id") int typeid, Model model, Principal principal){
//        System.out.println(typeid);
//        List<Videos> videos=videosRepository.findAllByTypeId(typeid);
//        model.addAttribute("videos",videos);
//        model.addAttribute("TypeId",typeid);
//        if(principal !=null){
//            model.addAttribute("username",principal.getName());
//        }
//        return "videoType_Details";
//
//    }
//
//    @GetMapping("/addvideoinvideotype/{typeId}")
//    public String additeminGType(@PathVariable("typeId") int id,Model model){
//
//        Videos video=new Videos();
//        VideosType videosType= videoTypeRepository.findById(id).orElseThrow();
//        model.addAttribute("video",video);
//        model.addAttribute("videoType",videosType);
//        return "addvideoinVideoType";
//    }
//    @PostMapping("/savevideoinVideoType")
//    public String saveitemType(@Valid @ModelAttribute("video") Videos video, @RequestParam("typeId") int typeId, @Valid @RequestParam("videoFile")MultipartFile videoURL, BindingResult bindingResult ) throws IOException {
//        if(bindingResult.hasErrors()){
//            return  "addvideoinVideoType";
//        }
//        String folder="C:/videos/";
//        String filename= UUID.randomUUID() + "_" + videoURL.getOriginalFilename();
//        Path filepath= Paths.get(folder,filename);
//        Files.write(filepath,videoURL.getBytes());
//
//        Videos videoitem=new Videos();
//        videoitem.setPath("/videos/"+filename);
//        videoitem.setTypeId(videoTypeRepository.findById(typeId).orElseThrow().getId());
//
//
//
//        videosRepository.save(videoitem);
//        System.out.println(typeId);
//
//        return "redirect:/videoGalaryType/" + typeId;
//
//    }
//    @PostMapping("/updateVideo")
//    public String updateiteminGType(@RequestParam("itemId") int id,Model model){
//        Videos video=videosRepository.findById(id).orElseThrow();
//        model.addAttribute("video",video);
//        return "updateVideo";
//    }
//    @PostMapping("/saveVideoAfterUpdate")
//    public String saveVideoafterUpdate(@Valid @ModelAttribute("video") Videos video, @RequestParam("newvideoURL") MultipartFile newURL, @RequestParam("oldvideoURl") String oldURL ) throws IOException {
//        //       if(bindingResult.hasErrors()){
//        //           return "updateForm";
//        //       }
//
//        if (!newURL.isEmpty()) {
//            if (video.getPath() != null) {
//                String folder = "C:/videos/" + Paths.get(video.getPath());
//                File oldvideoFile = new File(folder);
//                if (oldvideoFile.exists()) oldvideoFile.delete();
//            }
//
//            String newvideoFolder = "C:/videos/";
//            String newvideoname = UUID.randomUUID() + "_" + newURL.getOriginalFilename();
//            Path newpath = Paths.get(newvideoFolder, newvideoname);
//            Files.write(newpath, newURL.getBytes());
//            video.setPath("/videos/" + newvideoname);
//        }
//        else {
//            System.out.println(oldURL);
//            video.setPath(oldURL);
//        }
//        int typeId=video.getTypeId();
//        videosRepository.save(video);
//        return  "redirect:/videoGalaryType/" + typeId;
//    }
//    @PostMapping("/deleteVideo")
//    public String deleteiteminGType(@RequestParam("itemId") int deletedone){
//        Videos deletevideo=videosRepository.findById(deletedone).orElseThrow();
//        int typeId =deletevideo.getTypeId();
//        videosRepository.delete(deletevideo);
//        return "redirect:/videoGalaryType/" + typeId;
//    }
//


}
