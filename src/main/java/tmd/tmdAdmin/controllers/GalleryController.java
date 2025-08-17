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
import tmd.tmdAdmin.data.dto.GalleryItemTypeDTO;
import tmd.tmdAdmin.data.entities.Gallery;
import tmd.tmdAdmin.data.entities.Gallery_Type;
import tmd.tmdAdmin.data.repositories.GalleryRepository;
import tmd.tmdAdmin.data.repositories.GalleryTypeRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class GalleryController {
    @InitBinder
    public void initBinder(WebDataBinder dataBinder){

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
    @Autowired
    private GalleryRepository galleryRepository;

    @Autowired
    private GalleryTypeRepository galleryTypeRepository;


    @GetMapping("/addGalleryType")
    public  String addGalleryType(Model model){
        GalleryItemTypeDTO galleryT=new GalleryItemTypeDTO();
        model.addAttribute("item",galleryT);


        return "addGalleryType";
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

        Gallery_Type gallery = new Gallery_Type();
        gallery.setName(onegallery.getName());
        gallery.setName_ar(onegallery.getName_ar());
        gallery.setName_ru(onegallery.getName_ru());
        gallery.setPath("/images/" + filename);

        galleryTypeRepository.save(gallery);
        return "redirect:/seeGallery";
    }
    catch (DataIntegrityViolationException e) {
        model.addAttribute("error", "the name is must be unique");
        return "addGalleryType";
    }

    }
    @PostMapping("/updateitemGType")
    public String updateitem(@RequestParam("itemId") int id,Model model){
        Gallery_Type updateditem=galleryTypeRepository.findById(id).orElseThrow();
        model.addAttribute("updatedItem",updateditem);
        return "updateGType";
    }
    @PostMapping("/saveUpdate")
    public String saveafterUpdate(@Valid @ModelAttribute("updatedItem") Gallery_Type itemGallery,BindingResult bindingResult, @RequestParam("newimageURL") MultipartFile newURL, @RequestParam("oldimageURL") String oldURL ,Model model) throws IOException {
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
            return "redirect:/seeGallery";
        }
        catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "the name is must be unique");
            return "updateGType";
        }

    }
    @PostMapping("/deleteitemGType")
    public String remove(@RequestParam("itemId") int deletedone){
        Gallery_Type deleteGallery=galleryTypeRepository.findById(deletedone).orElseThrow();
        List<Gallery> galleries=galleryRepository.findAllByType_Id(deletedone);
        for(Gallery gallery: galleries){
            gallery.setType(null);
        }
        galleryRepository.saveAll(galleries);
        galleryTypeRepository.delete(deleteGallery);
        return "redirect:/seeGallery";
    }


    @GetMapping("/additeminGType/{typeId}")
    public String additeminGType(@PathVariable("typeId") int id,Model model){

        Gallery gallery=new Gallery();
       Gallery_Type galleryType= galleryTypeRepository.findById(id).orElseThrow();
        model.addAttribute("gallery",gallery);
        model.addAttribute("galleryType",galleryType);
        return "additeminGType";
    }
    @PostMapping("/saveiteminGType")
    public String saveitemType(@Valid @ModelAttribute("gallery") Gallery gallery,BindingResult bindingResult,@RequestParam("typeId") int typeId, @Valid @RequestParam("imageFile")MultipartFile imageurl,Model model ) throws IOException {
        if(bindingResult.hasErrors()){
            Gallery_Type galleryType= galleryTypeRepository.findById(typeId).orElseThrow();
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
        galleryitem.setType(galleryTypeRepository.findById(typeId).orElseThrow());
        galleryitem.setImageURL("/images/" + filename);


        galleryRepository.save(galleryitem);


        return "redirect:/admingalleryType/" + typeId;
    }
    catch (DataIntegrityViolationException e) {
        model.addAttribute("error", "the name is must be unique");

        Gallery_Type galleryType= galleryTypeRepository.findById(typeId).orElseThrow();
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
                 if (itemGallery.getImageURL() != null) {
                     String folder = "C:/wood-images/" + Paths.get(itemGallery.getImageURL());
                     File oldimgFile = new File(folder);
                     if (oldimgFile.exists()) oldimgFile.delete();
                 }

                 String newimgFolder = "C:/wood-images/";
                 String newImgname = UUID.randomUUID() + "_" + newURL.getOriginalFilename();
                 Path newpath = Paths.get(newimgFolder, newImgname);
                 Files.write(newpath, newURL.getBytes());
                 itemGallery.setImageURL("/images/" + newImgname);
             }
             else {
                 System.out.println(oldURL);
                 itemGallery.setImageURL(oldURL);
             }
             galleryRepository.save(itemGallery);
             return "redirect:/seeGallery" ;
         }
         catch (DataIntegrityViolationException e) {
             model.addAttribute("error", "the name is must be unique");
             return "updateGallery";
         }
    }
    @PostMapping("/deleteGallery")
    public String deleteiteminGType(@RequestParam("itemId") int deletedone){
        Gallery deleteGallery=galleryRepository.findById(deletedone).orElseThrow();
        int typeId =deleteGallery.getType().getId();
        galleryRepository.delete(deleteGallery);
        return "redirect:/admingalleryType/" + typeId;
    }
    @GetMapping("/admingalleryType/{id}")
    public String galleryType(@PathVariable("id") int typeid, Model model, Principal principal){
        System.out.println(typeid);
        List<Gallery> galleries=galleryRepository.findAllByType_Id(typeid);
        model.addAttribute("galaries",galleries);
        model.addAttribute("TypeId",typeid);
        if(principal !=null){
            model.addAttribute("username",principal.getName());
        }
        return "adminGalleryType_Details";

    }
}
