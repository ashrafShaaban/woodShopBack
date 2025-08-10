package tmd.tmdAdmin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import tmd.tmdAdmin.data.entities.Contact;
import tmd.tmdAdmin.data.entities.Gallery_Type;
import tmd.tmdAdmin.data.repositories.ContactRepository;
import tmd.tmdAdmin.data.repositories.GalleryRepository;
import tmd.tmdAdmin.data.repositories.GalleryTypeRepository;

import java.util.List;

@Controller
public class DashboardController {
    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private GalleryRepository galleryRepository;

    @Autowired
    private GalleryTypeRepository galleryTypeRepository;

    @GetMapping("/dashboard")
    public String adminD(Model model){
        List<Gallery_Type> galleries=galleryTypeRepository.findAll();
        int galleryLength=galleries.size();
        List<Contact> contacts=contactRepository.findAll();
        int contactLength=contacts.size();

        model.addAttribute("galleryLength",galleryLength);
        model.addAttribute("contactLength",contactLength);
        return "dashboard";
    }
    @GetMapping("/seeGallery")
    public String seeGallery(Model model){
        List<Gallery_Type> galleries=galleryTypeRepository.findAll();
        model.addAttribute("galleries",galleries);
        return "adminGallery";
    }

    @GetMapping("/seemessages")
    public String seemessages(Model model){
        List<Contact> contacts= contactRepository.findAll();
        model.addAttribute("messages",contacts);
        return "adminMessages";
    }

}
