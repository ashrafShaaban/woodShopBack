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


    private final CategoryRepository productsRepository;
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

        long contactLength = contactRepository.count();
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
        model.addAttribute("unreadMessageCount", contactRepository.countByIsReadFalse());


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

}
