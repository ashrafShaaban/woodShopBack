package tmd.tmdAdmin.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import tmd.tmdAdmin.data.entities.*;
import tmd.tmdAdmin.data.repositories.*;
import tmd.tmdAdmin.services.ContactService;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final SliderSideRepository sliderSideRepository;
    private final CategoryRepository categoryRepository;

    private final ContactService contactService;

    private final ContactRepository contactRepository;

    private final GalleryRepository galleryRepository;

    private final GalleryTypeRepository galleryTypeRepository;


    private final VideosRepository videosRepository;

    private final UserRepository userRepository;
    private final VideoTypeRepository videoTypeRepository;

    @GetMapping("/")
    public String dashboard(Model model, Principal principal, HttpServletRequest request) {
        List<GalleryType> galleries = galleryTypeRepository.findAll();
        int galleryLength = galleries.size() + galleryRepository.findAll().size();
        List<Contact> contacts = contactRepository.findAll();
        int contactLength = contacts.size();
        int videosLength = videosRepository.findAll().size() + videoTypeRepository.findAll().size();
        List<User> users = userRepository.findAll();
        int userSize = users.size();
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("galleryLength", galleryLength);
        model.addAttribute("contactLength", contactLength);
        model.addAttribute("videosLength", videosLength);
        model.addAttribute("users", userSize);
        model.addAttribute("pageTitle", "Dashboard | El Dahman");

        return "index";
    }

    @GetMapping("/dashboard-data")
    @ResponseBody
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // Example: counts per month
        data.put("labels", List.of("July", "Aug"));

        data.put("messages", contactService.getMonthlyCounts());

        return data;
    }

    @GetMapping("/seeSlider")
    public String slider(Model model, Principal principal) {
        List<SliderSlide> sliderSlides = sliderSideRepository.findAll();
        model.addAttribute("sliders", sliderSlides);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "slider";
    }

    @GetMapping("/seeCategories")
    public String category(Model model, Principal principal) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "categories";
    }

    @GetMapping("/gallery")
    public String gallery(Model model, Principal principal, HttpServletRequest request) {
        List<GalleryType> galleries = galleryTypeRepository.findAll();
        model.addAttribute("galleries", galleries);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", "Galleries | El Dahman");
        model.addAttribute("pageSpecificCss", new String[]{"/css/gallery.css"});

        return "adminGallery";
    }

    @GetMapping("/seemessages")
    public String seemessages(Model model, Principal principal) {
        List<Contact> contacts = contactRepository.findAll();
        model.addAttribute("messages", contacts);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "adminMessages";
    }

    @GetMapping("/seeVideos")
    public String seeVideos(Model model, Principal principal,HttpServletRequest request) {
        List<VideosType> videos = videoTypeRepository.findAll();
        model.addAttribute("videos", videos);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", "Galleries | El Dahman");
        model.addAttribute("pageSpecificCss", new String[]{"/css/gallery.css"});

        return "Videos";
    }

    @GetMapping("/seeUsers")
    public String seeUsers(Model model, Principal principal,HttpServletRequest request) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("pageTitle", "Galleries | El Dahman");
        List<User> users = userRepository.findAll();
//        User currentUser = userRepository.findUserByUsername(principal.getName());
//        System.out.println(currentUser);
//        boolean isSuperAdmin = currentUser.getRoles()
//                .stream()
//                .anyMatch(role -> role.getRolename() != null &&
//                        role.getRolename().trim().equalsIgnoreCase("ROLE_SUPERADMIN"));
//
//        boolean isAdmin = currentUser.getRoles()
//                .stream()
//                .anyMatch(role -> role.getRolename() != null &&
//                        role.getRolename().trim().equalsIgnoreCase("ROLE_ADMIN"));
//
//
//        if(isSuperAdmin)
//        {
//            users=userRepository.findAll();
//        } else if (isAdmin) {
//            users=userRepository.findAdminsOnly();
//
//        }
//        else{
//            users=List.of();
//        }
        model.addAttribute("users", users);
        return "Users";
    }


}
