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

    @PostMapping("/addGalleryType")
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

    @PostMapping("/save")
    public String saveitem(@Valid @ModelAttribute("item") GalleryItemTypeDTO onegallery, BindingResult bindingResult, @RequestParam(value = "imageFile",required = false) MultipartFile imageurl,Model model) throws IOException {
        if(bindingResult.hasErrors()){
            return "addGalleryType";
        }
    try {
        String folder = "C:/wood-images/";
        String filename = UUID.randomUUID() + "_" + imageurl.getOriginalFilename();
        Path filepath = Paths.get(folder, filename);
        Files.write(filepath, imageurl.getBytes());

        GalleryType gallery = new GalleryType();
        gallery.setName(onegallery.getName());
        gallery.setNameAr(onegallery.getName_ar());
        gallery.setNameRu(onegallery.getName_ru());
        gallery.setPath("/images/" + filename);

        galleryTypeRepository.save(gallery);
        return "redirect:/gallery";
    }
    catch (DataIntegrityViolationException e) {
        model.addAttribute("error", "the name is must be unique");
        return "addGalleryType";
    }

    }

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
    @PostMapping("/updateitemGType")
    public String updateitem(@RequestParam("itemId") int id,Model model){
        GalleryType updateditem=galleryTypeRepository.findById(id).orElseThrow();
        model.addAttribute("updatedItem",updateditem);
        return "updateGType";
    }
    @PostMapping("/saveUpdate")
    public String saveafterUpdate(@Valid @ModelAttribute("updatedItem") GalleryType itemGallery, BindingResult bindingResult, @RequestParam("newimageURL") MultipartFile newURL, @RequestParam("oldimageURL") String oldURL , Model model) throws IOException {
       if(bindingResult.hasErrors()){
           return "updateGType";
       }
        try {
            if (!newURL.isEmpty()) {
                if (itemGallery.getPath() != null) {
                    String folder = "C:/wood-images/" + Paths.get(itemGallery.getPath());
                    File oldimgFile = new File(folder);
                    if (oldimgFile.exists()) oldimgFile.delete();
                }

                String newimgFolder = "C:/wood-images/";
                String newImgname = UUID.randomUUID() + "_" + newURL.getOriginalFilename();
                Path newpath = Paths.get(newimgFolder, newImgname);
                Files.write(newpath, newURL.getBytes());
                itemGallery.setPath("/images/" + newImgname);
            }
            else {
                System.out.println(oldURL);
                itemGallery.setPath(oldURL);
            }
            galleryTypeRepository.save(itemGallery);
            return "redirect:/gallery";
        }
        catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "the name is must be unique");
            return "updateGType";
        }

    }
    @PostMapping("/deleteitemGType")
    public String remove(@RequestParam("itemId") int deletedone){
        GalleryType deleteGallery=galleryTypeRepository.findById(deletedone).orElseThrow();
        List<Gallery> galleries=galleryRepository.findAllByGalleryType_Id(deletedone);

        galleryRepository.saveAll(galleries);
        galleryTypeRepository.delete(deleteGallery);
        return "redirect:/gallery";
    }


    @GetMapping("/additeminGType/{typeId}")
    public String additeminGType(@PathVariable("typeId") int id,Model model){

        Gallery gallery=new Gallery();
       GalleryType galleryType= galleryTypeRepository.findById(id).orElseThrow();
        model.addAttribute("gallery",gallery);
        model.addAttribute("galleryType",galleryType);
        return "additeminGType";
    }
    @PostMapping("/saveiteminGType")
    public String saveitemType(@Valid @ModelAttribute("gallery") Gallery gallery,BindingResult bindingResult,@RequestParam("typeId") int typeId, @Valid @RequestParam("imageFile")MultipartFile imageurl,Model model ) throws IOException {
        if(bindingResult.hasErrors()){
            GalleryType galleryType= galleryTypeRepository.findById(typeId).orElseThrow();
            model.addAttribute("galleryType",galleryType);
            return  "additeminGType";
        }
    try {

        String folder="C:/wood-images/";
        String filename= UUID.randomUUID() + "_" + imageurl.getOriginalFilename();
        Path filepath= Paths.get(folder,filename);
        Files.write(filepath,imageurl.getBytes());

        Gallery galleryitem=new Gallery();
        galleryitem.setName(gallery.getName());
        galleryitem.setGalleryType(galleryTypeRepository.findById(typeId).orElseThrow());
        galleryitem.setPath("/images/" + filename);


        galleryRepository.save(galleryitem);


        return "redirect:/admingalleryType/" + typeId;
    }
    catch (DataIntegrityViolationException e) {
        model.addAttribute("error", "the name is must be unique");

        GalleryType galleryType= galleryTypeRepository.findById(typeId).orElseThrow();
        model.addAttribute("gallery",gallery);
        model.addAttribute("galleryType",galleryType);
        return "additeminGType";
    }

    }
    @PostMapping("/updateGallery")
    public String updateiteminGType(@RequestParam("itemId") int id,Model model){
        Gallery updateGallery=galleryRepository.findById(id).orElseThrow();
        model.addAttribute("updateGallery",updateGallery);
        return "updateGallery";
    }
    @PostMapping("/saveGalleryAfterUpdate")
    public String saveainGTfterUpdate(@Valid @ModelAttribute("updateGallery") Gallery itemGallery,BindingResult bindingResul, @RequestParam("newimageURL") MultipartFile newURL, @RequestParam("oldimageURL") String oldURL,BindingResult bindingResult,Model model ) throws IOException {
           if(bindingResult.hasErrors()){
               return "updateGallery";
           }
         try {
             if (!newURL.isEmpty()) {
                 if (itemGallery.getPath() != null) {
                     String folder = "C:/wood-images/" + Paths.get(itemGallery.getPath());
                     File oldimgFile = new File(folder);
                     if (oldimgFile.exists()) oldimgFile.delete();
                 }

                 String newimgFolder = "C:/wood-images/";
                 String newImgname = UUID.randomUUID() + "_" + newURL.getOriginalFilename();
                 Path newpath = Paths.get(newimgFolder, newImgname);
                 Files.write(newpath, newURL.getBytes());
                 itemGallery.setPath("/images/" + newImgname);
             }
             else {
                 System.out.println(oldURL);
                 itemGallery.setPath(oldURL);
             }
             galleryRepository.save(itemGallery);
             return "redirect:/gallery" ;
         }
         catch (DataIntegrityViolationException e) {
             model.addAttribute("error", "the name is must be unique");
             return "updateGallery";
         }
    }
    @PostMapping("/deleteGallery")
    public String deleteiteminGType(@RequestParam("itemId") int deletedone){
        Gallery deleteGallery=galleryRepository.findById(deletedone).orElseThrow();
        int typeId =deleteGallery.getGalleryType().getId();
        galleryRepository.delete(deleteGallery);
        return "redirect:/admingalleryType/" + typeId;
    }
    @GetMapping("/admingalleryType/{id}")
    public String galleryType(@PathVariable("id") int typeid, Model model, Principal principal){
        System.out.println(typeid);
        List<Gallery> galleries=galleryRepository.findAllByGalleryType_Id(typeid);
        model.addAttribute("galaries",galleries);
        model.addAttribute("TypeId",typeid);
        if(principal !=null){
            model.addAttribute("username",principal.getName());
        }
        return "adminGalleryType_Details";

    }
}
