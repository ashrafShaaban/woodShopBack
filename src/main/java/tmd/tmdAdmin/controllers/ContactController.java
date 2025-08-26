package tmd.tmdAdmin.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tmd.tmdAdmin.data.entities.Contact;
import tmd.tmdAdmin.data.repositories.ContactRepository;
import tmd.tmdAdmin.utils.ModelAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author : yahyai
 * @mailto : yahyai@procuredox.com
 **/
@Controller
@RequiredArgsConstructor
@RequestMapping("/contacts")
public class ContactController {
    private final ContactRepository contactRepository;
    private final ModelAttributes modelAttributes;

    @GetMapping({"", "/"})
    public String contacts(@RequestParam(value = "messageId", required = false) Integer messageId,
                           Model model,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        if(messageId != null){
            Optional<Contact> optionalContact = contactRepository.findById(messageId);
            if (optionalContact.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Message not found!");
                return "redirect:/contacts";
            }
            Contact message = optionalContact.get();
            model.addAttribute("pageTitle", "Message: " + message.getSubject() + " | El Dahman");

            if (!message.isRead()) {
                message.setRead(true);
                message.setReadAt(LocalDateTime.now());
                contactRepository.save(message);
            }
            model.addAttribute("message", message);
            model.addAttribute("pageSpecificCss", new String[]{"/css/contact-messages.css"}); // Re-use message styles

            return "view-message";
        }

        modelAttributes.setModelAttributes(model, request, "Contact Messages | El Dahman", new String[]{"/css/contact-messages.css"});
        model.addAttribute("messages", contactRepository.findAll());
        return "contact-messages";
    }

    @PostMapping("/delete/{id}")
    public String deleteMessage(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            contactRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Message deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting message: " + e.getMessage());
        }
        return "redirect:/contacts"; // Redirect back to the messages list
    }
}
