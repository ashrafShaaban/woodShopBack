package tmd.tmdAdmin.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import java.util.List;
import java.util.UUID;

@Controller
public class GalleryController {
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
    public String saveitem(@ModelAttribute("item") GalleryItemTypeDTO onegallery, @RequestParam("imageFile") MultipartFile imageurl ) throws IOException {
//     if(bindingResult.hasErrors()){
//         return  "additem";
//     }
        String folder="C:/wood-images/";
        String filename= UUID.randomUUID() + "_" + imageurl.getOriginalFilename();
        Path filepath= Paths.get(folder,filename);
        Files.write(filepath,imageurl.getBytes());

        Gallery_Type gallery=new Gallery_Type();
        gallery.setName(onegallery.getName());
        gallery.setName_ar(onegallery.getName_ar());
        gallery.setName_ru(onegallery.getName_ru());
        gallery.setPath("/images/" + filename);

        galleryTypeRepository.save(gallery);
        return "redirect:/seeGallery";

    }
    @PostMapping("/updateitemGType")
    public String updateitem(@RequestParam("itemId") int id,Model model){
        Gallery_Type updateditem=galleryTypeRepository.findById(id).orElseThrow();
        model.addAttribute("updatedItem",updateditem);
        return "updateGType";
    }
    @PostMapping("/saveUpdate")
    public String saveafterUpdate(@Valid @ModelAttribute("updatedItem") Gallery_Type itemGallery, @RequestParam("newimageURL") MultipartFile newURL, @RequestParam("oldimageURL") String oldURL ) throws IOException {
//       if(bindingResult.hasErrors()){
//           return "updateForm";
//       }
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
    public String saveitemType( @ModelAttribute("gallery") Gallery gallery,@RequestParam("typeId") int typeId, @Valid @RequestParam("imageFile")MultipartFile imageurl ) throws IOException {
//     if(bindingResult.hasErrors()){
//         return  "additem";
//     }
        String folder="C:/wood-images/";
        String filename= UUID.randomUUID() + "_" + imageurl.getOriginalFilename();
        Path filepath= Paths.get(folder,filename);
        Files.write(filepath,imageurl.getBytes());

        Gallery galleryitem=new Gallery();
        galleryitem.setName(gallery.getName());
        galleryitem.setType(galleryTypeRepository.findById(typeId).orElseThrow());
        galleryitem.setImageURL("/images/" + filename);


        galleryRepository.save(galleryitem);
        System.out.println(typeId);

        return "redirect:/admingalleryType/" + typeId;

    }
    @PostMapping("/updateGallery")
    public String updateiteminGType(@RequestParam("itemId") int id,Model model){
        Gallery updateGallery=galleryRepository.findById(id).orElseThrow();
        model.addAttribute("updateGallery",updateGallery);
        return "updateGallery";
    }
    @PostMapping("/saveGalleryAfterUpdate")
    public String saveainGTfterUpdate(@Valid @ModelAttribute("updatedItem") Gallery itemGallery, @RequestParam("newimageURL") MultipartFile newURL, @RequestParam("oldimageURL") String oldURL ) throws IOException {
    //       if(bindingResult.hasErrors()){
    //           return "updateForm";
    //       }

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
    @PostMapping("/deleteGallery")
    public String deleteiteminGType(@RequestParam("itemId") int deletedone){
        Gallery deleteGallery=galleryRepository.findById(deletedone).orElseThrow();
        int typeId =deleteGallery.getType().getId();
        galleryRepository.delete(deleteGallery);
        return "redirect:/admingalleryType/" + typeId;
    }
    @GetMapping("/admingalleryType/{id}")
    public String galleryType(@PathVariable("id") int typeid, Model model){
        System.out.println(typeid);
        List<Gallery> galleries=galleryRepository.findAllByType_Id(typeid);
        model.addAttribute("galaries",galleries);
        model.addAttribute("TypeId",typeid);
        return "adminGalleryType_Details";

    }
}
