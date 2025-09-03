package tmd.tmdAdmin.controllers;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tmd.tmdAdmin.data.dto.UserDTO;
import tmd.tmdAdmin.data.entities.GalleryType;
import tmd.tmdAdmin.data.entities.User;
import tmd.tmdAdmin.data.repositories.UserRepository;
import tmd.tmdAdmin.utils.ModelAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;
    private final ModelAttributes modelAttributes;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping({"", "/"})
    public String users(
                          Model model,
                          HttpServletRequest request) {

        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        modelAttributes.setModelAttributes(model, request, "Users | El Dahman", new String[]{"/css/gallery.css"});
        return "users";
    }


    @InitBinder
    public void initBinder(WebDataBinder dataBinder){

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/add/form")
    public String adduser(Model model){
      model.addAttribute("newUser",new UserDTO());
      model.addAttribute("isEdit",false);
      return "add-user-form";
    }
    @PostMapping("")
    public String saveUser(@Valid @ModelAttribute("newUser") UserDTO newUser, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes){
       if(bindingResult.hasErrors()){
           model.addAttribute("isEdit", false);
           return "add-user-form";
       }

        try{
           User user =new User();
           user.setUsername(newUser.getUsername());
           user.setPassword(passwordEncoder.encode(newUser.getPassword()));
            user.setRole("ROLE_" + newUser.getRole());
           user.setCreatedAt(System.currentTimeMillis());
           user.setActive(true);
//           user.setRoles(roles);
           userRepository.save(user);
           return "redirect:/users";
       }
       catch (DataIntegrityViolationException e) {

           model.addAttribute("error", "the name must be unique");
           model.addAttribute("isEdit", false);
           return "addUser";
       }
    }
    @PostMapping("/update/form")
    public String updateUser(@RequestParam("userId") int id,Model model){
        User updatedUser=userRepository.findById(id).orElseThrow();
        UserDTO updateduserDto=new UserDTO();
        updateduserDto.setId(updatedUser.getId());
        updateduserDto.setUsername(updatedUser.getUsername());
        updateduserDto.setPassword(updatedUser.getPassword());
        updateduserDto.setActive(updatedUser.getActive());

        model.addAttribute("newUser",updateduserDto);
        model.addAttribute("isEdit",true);
        return "edit-user-form";
    }
    @PostMapping("/update")
    public String saveUpdate(@Valid  @ModelAttribute("newUser") UserDTO newUser,BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return "edit-user-form";
        }
        try{
            User updateduser=userRepository.findById(newUser.getId()).orElseThrow();

            updateduser.setUsername(newUser.getUsername());
            if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
                updateduser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            }
            updateduser.setRole("ROLE_" + newUser.getRole());
            updateduser.setUpdatedAt(System.currentTimeMillis());
            updateduser.setActive(newUser.getActive());
//            updateduser.setRoles(roles);
            userRepository.save(updateduser);
            return "redirect:/users";
        }
        catch (DataIntegrityViolationException e) {

            model.addAttribute("error", "the name must be unique");
            model.addAttribute("isEdit", true);
            return "edit-user-form";
        }
    }
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("userId") int id)
    {
        User deletedUser=userRepository.findById(id).orElseThrow();
        userRepository.delete(deletedUser);
        return "redirect:/users";
    }
}
