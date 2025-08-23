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
import tmd.tmdAdmin.data.dto.SliderDTO;
import tmd.tmdAdmin.data.entities.Gallery;
import tmd.tmdAdmin.data.entities.Gallery_Type;
import tmd.tmdAdmin.data.entities.SliderSlide;
import tmd.tmdAdmin.data.repositories.SliderSideRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class SliderController {
    @InitBinder
    public void initBinder(WebDataBinder dataBinder){

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Autowired
    private SliderSideRepository sliderSideRepository;

    @GetMapping("/addnewSlider")
    public  String addSlider(Model model){
        SliderDTO sliderDTO=new SliderDTO();
        model.addAttribute("slider",sliderDTO);
        return "addSlider";
    }
    @PostMapping("/saveSlider")
    public String saveSlider(@Valid @ModelAttribute("slider") SliderDTO sliderDto, BindingResult bindingResult, @RequestParam(value = "SliderFile",required = false) MultipartFile sliderURL, Model model) throws IOException {
        if(bindingResult.hasErrors()){
            return "addSlider";
        }
        try {
            SliderSlide sliderSlide;
            if(sliderDto.getId() != null){
                sliderSlide=sliderSideRepository.findById(sliderDto.getId()).orElseThrow();
            }
            else{
                sliderSlide=new SliderSlide();
            }
            sliderSlide.setName(sliderDto.getName());
            sliderSlide.setSubtitle(sliderDto.getSubtitle());
            sliderSlide.setButtonText(sliderDto.getButtonText());
            sliderSlide.setButtonUrl(sliderDto.getButtonUrl());
            if(sliderURL !=null && !sliderURL.isEmpty()){
                String folder = "C:/sliders/";
                String filename = UUID.randomUUID() + "_" + sliderURL.getOriginalFilename();
                Path filepath = Paths.get(folder, filename);
                Files.write(filepath, sliderURL.getBytes());
//
//                SliderSlide slider = new SliderSlide();
//                slider.setName(sliderDto.getName());

                sliderSlide.setPath("/sliders/" + filename);
            }
//            String folder = "C:/sliders/";
//            String filename = UUID.randomUUID() + "_" + sliderURL.getOriginalFilename();
//            Path filepath = Paths.get(folder, filename);
//            Files.write(filepath, sliderURL.getBytes());
//
//            SliderSlide slider = new SliderSlide();
//            slider.setName(sliderDto.getName());
//
//            slider.setPath("/sliders/" + filename);

            sliderSideRepository.save(sliderSlide);
            return "redirect:/seeSlider";
        }
        catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "the name is must be unique");
            return "addSlider";
        }

    }
    @PostMapping("/updateSlider")
    public String updateSlider(@RequestParam("sliderId") int id,Model model){
        SliderSlide updateditem=sliderSideRepository.findById(id).orElseThrow();
        SliderDTO sliderDTO=new SliderDTO();
        sliderDTO.setId(updateditem.getId());
        sliderDTO.setName(updateditem.getName());
//        sliderDTO.setPath(updateditem.getPath());

        model.addAttribute("slider",sliderDTO);
        return "addSlider";
    }
//    @PostMapping("/saveUpdate")
//    public String saveafterUpdate(@Valid @ModelAttribute("updatedItem") Gallery_Type itemGallery,BindingResult bindingResult, @RequestParam("newimageURL") MultipartFile newURL, @RequestParam("oldimageURL") String oldURL ,Model model) throws IOException {
//        if(bindingResult.hasErrors()){
//            return "updateGType";
//        }
//        try {
//            if (!newURL.isEmpty()) {
//                if (itemGallery.getPath() != null) {
//                    String folder = "C:/wood-images/" + Paths.get(itemGallery.getPath());
//                    File oldimgFile = new File(folder);
//                    if (oldimgFile.exists()) oldimgFile.delete();
//                }
//
//                String newimgFolder = "C:/wood-images/";
//                String newImgname = UUID.randomUUID() + "_" + newURL.getOriginalFilename();
//                Path newpath = Paths.get(newimgFolder, newImgname);
//                Files.write(newpath, newURL.getBytes());
//                itemGallery.setPath("/images/" + newImgname);
//            }
//            else {
//                System.out.println(oldURL);
//                itemGallery.setPath(oldURL);
//            }
//            galleryTypeRepository.save(itemGallery);
//            return "redirect:/seeGallery";
//        }
//        catch (DataIntegrityViolationException e) {
//            model.addAttribute("error", "the name is must be unique");
//            return "updateGType";
//        }
//
//    }
    @PostMapping("/deleteSlider")
    public String remove(@RequestParam("sliderId") int deletedone){
        SliderSlide deletedslider=sliderSideRepository.findById(deletedone).orElseThrow();


       sliderSideRepository.delete(deletedslider);
        return "redirect:/seeSlider";
    }

}
