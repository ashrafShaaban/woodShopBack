package tmd.tmdAdmin.controllers;

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
import tmd.tmdAdmin.data.entities.User;
import tmd.tmdAdmin.data.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder){

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/addUser")
    public String adduser(Model model){
      model.addAttribute("newUser",new UserDTO());
      model.addAttribute("isEdit",false);
      return "addUser";
    }
    @PostMapping("saveUser")
    public String saveUser(@Valid @ModelAttribute("newUser") UserDTO newUser, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes){
       if(bindingResult.hasErrors()){
           model.addAttribute("isEdit", false);
           return "addUser";
       }

        try{
           User user =new User();
           user.setUsername(newUser.getUsername());
           user.setPassword(passwordEncoder.encode(newUser.getPassword()));
           user.setRole(newUser.getRole());
           user.setCreatedAt(System.currentTimeMillis());
           user.setActive(true);
//           user.setRoles(roles);
           userRepository.save(user);
           return "redirect:/seeUsers";
       }
       catch (DataIntegrityViolationException e) {

           model.addAttribute("error", "the name must be unique");
           model.addAttribute("isEdit", false);
           return "addUser";
       }
    }
    @PostMapping("/updateUser")
    public String updateUser(@RequestParam("userId") int id,Model model){
        User updatedUser=userRepository.findById(id).orElseThrow();
        UserDTO updateduserDto=new UserDTO();
        updateduserDto.setId(updatedUser.getId());
        updateduserDto.setUsername(updatedUser.getUsername());
        updateduserDto.setPassword(updatedUser.getPassword());
        updateduserDto.setActive(updatedUser.getActive());
//        updateduserDto.setRolesIds(
//                updatedUser.getRoles() == null
//                        ? new ArrayList<>()
//                        : updatedUser.getRoles().stream()
//                        .map(Role::getId)
//                        .collect(Collectors.toList())
//        );
        model.addAttribute("newUser",updateduserDto);
        model.addAttribute("isEdit",true);
        return "addUser";
    }
    @PostMapping("/saveuserUpdate")
    public String saveUpdate(@Valid  @ModelAttribute("newUser") UserDTO newUser,BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return "addUser";
        }
        try{
            User updateduser=userRepository.findById(newUser.getId()).orElseThrow();

            updateduser.setUsername(newUser.getUsername());
            if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
                updateduser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            }
            updateduser.setRole(newUser.getRole());
            updateduser.setUpdatedAt(System.currentTimeMillis());
            updateduser.setActive(newUser.getActive());
//            updateduser.setRoles(roles);
            userRepository.save(updateduser);
            return "redirect:/seeUsers";
        }
        catch (DataIntegrityViolationException e) {

            model.addAttribute("error", "the name must be unique");
            model.addAttribute("isEdit", true);
            return "addUser";
        }
    }
    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam("userId") int id)
    {
        User deletedUser=userRepository.findById(id).orElseThrow();
        userRepository.delete(deletedUser);
        return "redirect:/seeUsers";
    }
}
