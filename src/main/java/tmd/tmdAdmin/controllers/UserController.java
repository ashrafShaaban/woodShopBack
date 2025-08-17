package tmd.tmdAdmin.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import tmd.tmdAdmin.data.dto.UserDTO;
import tmd.tmdAdmin.data.entities.Role;
import tmd.tmdAdmin.data.entities.User;
import tmd.tmdAdmin.data.repositories.RoleRepository;
import tmd.tmdAdmin.data.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {
    @Autowired
    private RoleRepository roleRepository;

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
      model.addAttribute("roles",roleRepository.findAll());
      model.addAttribute("isEdit",false);
      return "addUser";
    }
    @PostMapping("saveUser")
    public String saveUser(@Valid @ModelAttribute("newUser") UserDTO newUser,BindingResult bindingResult,Model model){
       if(bindingResult.hasErrors()){
           model.addAttribute("roles", roleRepository.findAll());
           model.addAttribute("isEdit", false);
           return "addUser";
       }

        try{
           User user =new User();
           user.setUsername(newUser.getUsername());
           user.setPassword(passwordEncoder.encode(newUser.getPassword()));
           List<Role> roles=roleRepository.findAllById(newUser.getRolesIds());
           user.setRoles(roles);
           userRepository.save(user);
           return "redirect:/seeUsers";
       }
       catch (DataIntegrityViolationException e) {

           model.addAttribute("error", "the name must be unique");
           model.addAttribute("roles", roleRepository.findAll());
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
        updateduserDto.setRolesIds(
                updatedUser.getRoles() == null
                        ? new ArrayList<>()
                        : updatedUser.getRoles().stream()
                        .map(Role::getId)
                        .collect(Collectors.toList())
        );
        model.addAttribute("newUser",updateduserDto);
        model.addAttribute("roles",roleRepository.findAll());
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
            List<Role> roles=roleRepository.findAllById(newUser.getRolesIds());
            updateduser.setRoles(roles);
            userRepository.save(updateduser);
            return "redirect:/seeUsers";
        }
        catch (DataIntegrityViolationException e) {

            model.addAttribute("error", "the name must be unique");
            model.addAttribute("roles", roleRepository.findAll());
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
