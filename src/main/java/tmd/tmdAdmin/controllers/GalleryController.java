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
import tmd.tmdAdmin.data.dto.GalleryItemTypeDTO;
import tmd.tmdAdmin.data.entities.Gallery;
import tmd.tmdAdmin.data.entities.GalleryType;
import tmd.tmdAdmin.data.repositories.GalleryRepository;
import tmd.tmdAdmin.data.repositories.GalleryTypeRepository;
import tmd.tmdAdmin.storage.FileStorageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class GalleryController {
    @InitBinder
    public void initBinder(WebDataBinder dataBinder){

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
    private final GalleryRepository galleryRepository;

    private final GalleryTypeRepository galleryTypeRepository;

    private final FileStorageService fileStorageService;


    @GetMapping("/addGalleryType")
    public String showAddGalleryForm(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", "Add New Gallery | El Dahman");
        model.addAttribute("galleryType", new GalleryType()); // For Thymeleaf form binding
        return "add-gallery-form";
    }

    @PostMapping("/saveGalleryType")
    public String addGalleryType(@ModelAttribute GalleryType galleryType, // Bind album properties (name, nameAr, nameRu)
                                 @RequestParam("coverImageFile") MultipartFile coverImageFile, // Album cover image file
                                 @RequestParam("albumImageFiles") MultipartFile[] albumImageFiles, // Album content image files
                                 RedirectAttributes redirectAttributes) {
        try {
            if (galleryTypeRepository.findByName(galleryType.getName()).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Gallery album with this name already exists.");
                return "redirect:/addGalleryType";
            }
            if (coverImageFile == null || coverImageFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please upload a cover image for the album.");
                return "redirect:/addGalleryType";
            }
            if (albumImageFiles == null || albumImageFiles.length == 0 || albumImageFiles[0].isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please upload at least one image for the album content.");
                return "redirect:/addGalleryType";
            }
            String coverImagePath = fileStorageService.storeGalleryTypeCover(coverImageFile);
            galleryType.setPath(coverImagePath); // Set the path directly to GalleryType.path


            GalleryType savedGalleryType = galleryTypeRepository.save(galleryType); // galleryType.id will be set after save

            // 3. Handle Album Content Image Uploads
            for (MultipartFile file : albumImageFiles) {
                if (!file.isEmpty()) {
                    // This creates a 'Gallery' entity (which represents an image)
                    // We can pass original filename as caption for Gallery.name
                    Gallery albumImage = fileStorageService.storeGalleryContentImage(file, savedGalleryType, file.getOriginalFilename());
                    albumImage.setGalleryType(savedGalleryType);
                    galleryRepository.save(albumImage); // Save the image entity
                    savedGalleryType.addImage(albumImage); // Add image to album's list
                }
            }
            galleryTypeRepository.save(savedGalleryType); // Re-save to ensure image list is updated

            redirectAttributes.addFlashAttribute("successMessage", "Gallery album '" + savedGalleryType.getName() + "' and images added successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload images: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
        }
        return "redirect:/gallery";
    }

//    @PostMapping("/save")
//    public String saveitem(@Valid @ModelAttribute("item") GalleryItemTypeDTO onegallery, BindingResult bindingResult, @RequestParam(value = "imageFile",required = false) MultipartFile imageurl,Model model) throws IOException {
//        if(bindingResult.hasErrors()){
//            return "addGalleryType";
//        }
//    try {
//        String folder = "C:/wood-images/";
//        String filename = UUID.randomUUID() + "_" + imageurl.getOriginalFilename();
//        Path filepath = Paths.get(folder, filename);
//        Files.write(filepath, imageurl.getBytes());
//
//        GalleryType gallery = new GalleryType();
//        gallery.setName(onegallery.getName());
//        gallery.setNameAr(onegallery.getName_ar());
//        gallery.setNameRu(onegallery.getName_ru());
//        gallery.setPath("/images/" + filename);
//
//        galleryTypeRepository.save(gallery);
//        return "redirect:/gallery";
//    }
//    catch (DataIntegrityViolationException e) {
//        model.addAttribute("error", "the name is must be unique");
//        return "addGalleryType";
//    }
//
//    }
//
    @GetMapping("/viewGallery/{galleryTypeId}")
    public String viewGalleryImages(@PathVariable("galleryTypeId") Integer galleryTypeId, Model model, HttpServletRequest request) {
        GalleryType galleryType = galleryTypeRepository.findById(galleryTypeId)
                .orElseThrow(() -> new RuntimeException("Gallery album not found with ID: " + galleryTypeId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        model.addAttribute("username", currentUserName);
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", galleryType.getName() + " Images | El Dahman");

        model.addAttribute("galleryType", galleryType);
        model.addAttribute("images", galleryType.getImages()); // Pass individual images to the template
        model.addAttribute("pageSpecificCss", new String[]{"/css/gallery.css"});

        return "view-gallery";  // New template for viewing images in an album
    }
    @GetMapping("/updateitemGType")
    public String updateitem(@RequestParam("itemId") int id,Model model){
        GalleryType updateditem=galleryTypeRepository.findById(id).orElseThrow();
        model.addAttribute("updatedItem",updateditem);
        return "edit-gallery-form";
    }
    @PostMapping("/updateitemGType")
    public String saveUpdate(
            @Valid @ModelAttribute("updatedItem") GalleryType itemGallery,
            BindingResult bindingResult,
            @RequestParam(value = "newCoverImageFile", required = false) MultipartFile newCoverImageFile,
            @RequestParam(value = "oldimageURL", required = false) String oldURL,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "edit-gallery-form"; // back to form with validation errors
        }

        try {
            // 1. Load the existing entity from DB
            GalleryType existing = galleryTypeRepository.findById(itemGallery.getId())
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
            galleryTypeRepository.save(existing);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Gallery album '" + existing.getName() + "' updated successfully!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload cover image: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
        }

        return "redirect:/gallery";
    }
    @PostMapping("/deleteitemGType")
    public String remove(@RequestParam("itemId") int deletedone){
        GalleryType deleteGallery=galleryTypeRepository.findById(deletedone).orElseThrow();
        List<Gallery> galleries=galleryRepository.findAllByGalleryType_Id(deletedone);

        galleryRepository.saveAll(galleries);
        galleryTypeRepository.delete(deleteGallery);
        return "redirect:/gallery";
    }


    @GetMapping("/addImagesToAlbum/{typeId}")
    public String addGallery(@PathVariable("typeId") int id,Model model){
        System.out.println(id);
        Gallery gallery=new Gallery();
       GalleryType galleryType= galleryTypeRepository.findById(id).orElseThrow();
        model.addAttribute("gallery",gallery);
        model.addAttribute("galleryType",galleryType);
        return "add-images-to-album-form";
    }
    @PostMapping("/addImagesToAlbum/{typeId}")
    public String saveGallery(@RequestParam("newAlbumImageFiles") MultipartFile newImageURL,@PathVariable("typeId") int typeId,Model model ) throws IOException {
//        if(bindingResult.hasErrors()){
//            GalleryType galleryType= galleryTypeRepository.findById(typeId).orElseThrow();
//            model.addAttribute("galleryType",galleryType);
//            return  "additeminGType";
//        }
//    try {
        Gallery gallery=new Gallery();
        if (newImageURL != null && !newImageURL.isEmpty()) {
            // Upload new cover → replace
            String newImagePath = fileStorageService.storeGalleryTypeCover(newImageURL);
            gallery.setPath(newImagePath);
            gallery.setName(newImageURL.getOriginalFilename());
        }




        gallery.setGalleryType(galleryTypeRepository.findById(typeId).orElseThrow());

        galleryRepository.save(gallery);


        return "redirect:/viewGallery/" + typeId;
//    }
//    catch (DataIntegrityViolationException e) {
//        model.addAttribute("error", "the name is must be unique");
//
//        GalleryType galleryType= galleryTypeRepository.findById(typeId).orElseThrow();
//        model.addAttribute("gallery",gallery);
//        model.addAttribute("galleryType",galleryType);
//        return "additeminGType";
//    }

    }
//    @PostMapping("/updateGallery")
//    public String updateiteminGType(@RequestParam("itemId") int id,Model model){
//        Gallery updateGallery=galleryRepository.findById(id).orElseThrow();
//        model.addAttribute("updateGallery",updateGallery);
//        return "updateGallery";
//    }
//    @PostMapping("/saveGalleryAfterUpdate")
//    public String saveainGTfterUpdate(@Valid @ModelAttribute("updateGallery") Gallery itemGallery,BindingResult bindingResul, @RequestParam("newimageURL") MultipartFile newURL, @RequestParam("oldimageURL") String oldURL,BindingResult bindingResult,Model model ) throws IOException {
//           if(bindingResult.hasErrors()){
//               return "updateGallery";
//           }
//         try {
//             if (!newURL.isEmpty()) {
//                 if (itemGallery.getPath() != null) {
//                     String folder = "C:/wood-images/" + Paths.get(itemGallery.getPath());
//                     File oldimgFile = new File(folder);
//                     if (oldimgFile.exists()) oldimgFile.delete();
//                 }
//
//                 String newimgFolder = "C:/wood-images/";
//                 String newImgname = UUID.randomUUID() + "_" + newURL.getOriginalFilename();
//                 Path newpath = Paths.get(newimgFolder, newImgname);
//                 Files.write(newpath, newURL.getBytes());
//                 itemGallery.setPath("/images/" + newImgname);
//             }
//             else {
//                 System.out.println(oldURL);
//                 itemGallery.setPath(oldURL);
//             }
//             galleryRepository.save(itemGallery);
//             return "redirect:/gallery" ;
//         }
//         catch (DataIntegrityViolationException e) {
//             model.addAttribute("error", "the name is must be unique");
//             return "updateGallery";
//         }
//    }
    @PostMapping("/deleteImage")
    public String deleteiteminGType(@RequestParam("imageId") int deletedone,@RequestParam("galleryTypeId") int typeId){
        Gallery deleteGallery=galleryRepository.findById(deletedone).orElseThrow();

        galleryRepository.delete(deleteGallery);
        return "redirect:/viewGallery/" + typeId;
    }
//    @GetMapping("/admingalleryType/{id}")
//    public String galleryType(@PathVariable("id") int typeid, Model model, Principal principal){
//        System.out.println(typeid);
//        List<Gallery> galleries=galleryRepository.findAllByGalleryType_Id(typeid);
//        model.addAttribute("galaries",galleries);
//        model.addAttribute("TypeId",typeid);
//        if(principal !=null){
//            model.addAttribute("username",principal.getName());
//        }
//        return "adminGalleryType_Details";
//
//    }
}
