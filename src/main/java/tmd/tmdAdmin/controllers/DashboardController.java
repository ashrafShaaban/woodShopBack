package tmd.tmdAdmin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import tmd.tmdAdmin.data.entities.*;
import tmd.tmdAdmin.data.repositories.*;
import tmd.tmdAdmin.services.ContactService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {
    @Autowired
    private ContactService contactService;
    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private GalleryRepository galleryRepository;

    @Autowired
    private GalleryTypeRepository galleryTypeRepository;

    @Autowired
    private VideosRepository videosRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private  VideoTypeRepository videoTypeRepository;

    @GetMapping("/dashboard")
    public String adminD(Model model,Principal principal){
        List<Gallery_Type> galleries=galleryTypeRepository.findAll();
        int galleryLength=galleries.size()+galleryRepository.findAll().size();
        List<Contact> contacts=contactRepository.findAll();
        int contactLength=contacts.size();
        int videosLength=videosRepository.findAll().size()+videoTypeRepository.findAll().size();
        List<User> users=userRepository.findAll();
        int usersize=users.size();
        if(principal !=null){
            model.addAttribute("username",principal.getName());
        }
        System.out.println(videosLength);
        model.addAttribute("galleryLength",galleryLength);
        model.addAttribute("contactLength",contactLength);
        model.addAttribute("videosLength",videosLength);
        model.addAttribute("users",usersize);
        return "dashboard";
    }
    @GetMapping("/dashboard-data")
    @ResponseBody
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // Example: counts per month
        data.put("labels", List.of( "July", "Aug"));

        data.put("messages", contactService.getMonthlyCounts());

        return data;
    }
    @GetMapping("/seeGallery")
    public String seeGallery(Model model,Principal principal){
        List<Gallery_Type> galleries=galleryTypeRepository.findAll();
        model.addAttribute("galleries",galleries);
        if(principal !=null){
            model.addAttribute("username",principal.getName());
        }
        return "adminGallery";
    }

    @GetMapping("/seemessages")
    public String seemessages(Model model,Principal principal){
        List<Contact> contacts= contactRepository.findAll();
        model.addAttribute("messages",contacts);
        if(principal !=null){
            model.addAttribute("username",principal.getName());
        }
        return "adminMessages";
    }
    @GetMapping("/seeVideos")
    public String seeVideos(Model model,Principal principal){
        List<VideosType> videos=videoTypeRepository.findAll();
        model.addAttribute("videos",videos);
        if(principal !=null){
            model.addAttribute("username",principal.getName());
        }
        return "Videos";
    }
    @GetMapping("/seeUsers")
    public String seeUsers(Model model,Principal principal){
        if(principal !=null){
            model.addAttribute("username",principal.getName());
        }
        List<User> users;
        User currentUser = userRepository.findUserByUsername(principal.getName());
        System.out.println(currentUser);
        boolean isSuperAdmin = currentUser.getRoles()
                .stream()
                .anyMatch(role -> role.getRolename() != null &&
                        role.getRolename().trim().equalsIgnoreCase("ROLE_SUPERADMIN"));

        boolean isAdmin = currentUser.getRoles()
                .stream()
                .anyMatch(role -> role.getRolename() != null &&
                        role.getRolename().trim().equalsIgnoreCase("ROLE_ADMIN"));


        if(isSuperAdmin)
        {
            users=userRepository.findAll();
        } else if (isAdmin) {
            users=userRepository.findAdminsOnly();

        }
        else{
            users=List.of();
        }
        model.addAttribute("users",users);
        return "Users";
    }


}
