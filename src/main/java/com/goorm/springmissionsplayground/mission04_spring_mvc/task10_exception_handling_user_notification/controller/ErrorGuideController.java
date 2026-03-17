package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.domain.ErrorGuide;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.service.ErrorGuideService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mission04/task10/error-guides")
public class ErrorGuideController {

    private final ErrorGuideService errorGuideService;

    public ErrorGuideController(ErrorGuideService errorGuideService) {
        this.errorGuideService = errorGuideService;
    }

    @GetMapping
    public String showGuideIndex(Model model) {
        List<ErrorGuide> guides = errorGuideService.findAll();
        model.addAttribute("guides", guides);
        model.addAttribute("brokenGuideId", 999L);
        return "mission04/task10/error-guide-list";
    }

    @GetMapping("/{id}")
    public String showGuideDetail(@PathVariable Long id, Model model) {
        ErrorGuide guide = errorGuideService.findById(id);
        List<ErrorGuide> relatedGuides = errorGuideService.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(id))
                .toList();

        model.addAttribute("guide", guide);
        model.addAttribute("relatedGuides", relatedGuides);
        return "mission04/task10/error-guide-detail";
    }
}
