package tmd.tmdAdmin.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import tmd.tmdAdmin.utils.ModelAttributes;

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
@Slf4j
@RequestMapping("/video")
public class VideoController {
    private final VideosRepository videosRepository;
    private final VideoTypeRepository videoTypeRepository;
    private final FileStorageService fileStorageService;
    private final ModelAttributes modelAttributes;


    @GetMapping({"", "/"})
    public String video(@RequestParam(value = "videoTypeId", required = false) Integer videoTypeId,
                          Model model,
                          HttpServletRequest request) {
        if (videoTypeId != null) {
            VideosType videoType = videoTypeRepository.findById(videoTypeId)
                    .orElseThrow(() -> new RuntimeException("videoGallery album not found with ID: " + videoTypeId));

            modelAttributes.setModelAttributes(model, request, videoType.getName() + " Videos | El Dahman", new String[]{"/css/gallery.css"});
            model.addAttribute("videoType", videoType);
            model.addAttribute("videos", videoType.getVideos()); // Pass individual images to the template
            model.addAttribute("pageSpecificCss", new String[]{"/css/gallery.css"});
            return "view-videos";
        }
        List<VideosType> videosTypes = videoTypeRepository.findAll();
        model.addAttribute("videos", videosTypes);
        modelAttributes.setModelAttributes(model, request, "videoGalleries | El Dahman", new String[]{"/css/gallery.css"});
        return "videos";
    }
    @GetMapping("/add/form")
    public String addVideoForm(Model model, HttpServletRequest request){
        VideosType videoType=new VideosType();
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", "Add New Gallery | El Dahman");

        model.addAttribute("videoType",videoType);
        return "add-videoGallery-form";
    }
        @PostMapping("")
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

                redirectAttributes.addFlashAttribute("successMessage", "videoGallery album '" + savedGalleryType.getName() + "' and images added successfully!");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload images: " + e.getMessage());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            }
            return "redirect:/video";

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
    @GetMapping("/update/form")
    public String updateitem(@RequestParam("itemId") int id,Model model){
        System.out.println(id);
        VideosType updateditem=videoTypeRepository.findById(id).orElseThrow();
        model.addAttribute("updatedItem",updateditem);
        return "edit-video-form";
    }
    @PostMapping("/update")
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
                    "videoGallery album '" + existing.getName() + "' updated successfully!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload cover image: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
        }

        return "redirect:/video";
    }
    @PostMapping("/delete")
    public String deletevideoGalleryType(@RequestParam("itemId") int videoTypeId,RedirectAttributes redirectAttributes){

        try {
            VideosType videoTypeToDelete = videoTypeRepository.findById(videoTypeId)
                    .orElseThrow(() -> new RuntimeException(" video Gallery album not found with ID: " + videoTypeId));

            // Delete all associated files from disk first
            if (videoTypeToDelete.getPath() != null && !videoTypeToDelete.getPath().isEmpty()) {
                fileStorageService.deleteFile(videoTypeToDelete.getPath());
            }
            for (Videos video : videoTypeToDelete.getVideos()) {
                fileStorageService.deleteFile(video.getPath());
            }
            List<Videos> videos=videosRepository.findAllByVideosType_Id(videoTypeId);
            videosRepository.deleteAll(videos);
            videoTypeRepository.delete(videoTypeToDelete);

            redirectAttributes.addFlashAttribute("successMessage", "videoGallery album '" + videoTypeToDelete.getName() + "' and all its images deleted successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete album files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during album deletion: " + e.getMessage());
            e.printStackTrace();
        }
//        videosRepository.saveAll(videos);
//        videoTypeRepository.delete(deleteGallery);
        return "redirect:/video";
    }



    @GetMapping("/videos/add/form/{typeId}")
    public String addGallery(@PathVariable("typeId") int id,Model model){
        System.out.println(id);
        Videos video=new Videos();
        VideosType videoType= videoTypeRepository.findById(id).orElseThrow();
        model.addAttribute("video",video);
        model.addAttribute("videoType",videoType);
        return "add-video-to-album-form";
    }
    @PostMapping("/videos/add/{typeId}")
    public String saveGallery(@RequestParam("newAlbumVideoFiles") MultipartFile newVideoURL,@PathVariable("typeId") int typeId,Model model ) throws IOException {

        Videos video=new Videos();
        if (newVideoURL != null && !newVideoURL.isEmpty()) {
            // Upload new cover → replace
            String newVideoePath = fileStorageService.storeGalleryTypeCover(newVideoURL);
            video.setPath(newVideoePath);
            video.setName(newVideoURL.getOriginalFilename());
        }
        video.setVideosType(videoTypeRepository.findById(typeId).orElseThrow());
        videosRepository.save(video);
        return "redirect:/video/viewVideos/" + typeId;


    }
    @PostMapping("/deleteVideo")
    public String deleteiteminGType(@RequestParam("videoId") int deletedone,@RequestParam("videoTypeId") int typeId){
        Videos deleteVideo=videosRepository.findById(deletedone).orElseThrow();

        videosRepository.delete(deleteVideo);
        return "redirect:/video/viewVideos/" + typeId;
    }

}
