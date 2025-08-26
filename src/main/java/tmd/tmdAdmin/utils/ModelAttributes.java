package tmd.tmdAdmin.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.security.Principal;

/**
 * @author : yahyai
 * @mailto : yibrahim.py@gmail.com
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class ModelAttributes {

    public void setModelAttributes(Model model, HttpServletRequest request, String title, String[] pageSpecificCss) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageSpecificCss", pageSpecificCss);
        model.addAttribute("username", currentUserName);
        model.addAttribute("currentUri", request.getRequestURI());
    }

}
