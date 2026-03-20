package com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.domain.MvcStudySession;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.service.ModelViewSeparationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mission04/task12/model-view")
public class ModelViewDemoController {

    private final ModelViewSeparationService modelViewSeparationService;

    public ModelViewDemoController(ModelViewSeparationService modelViewSeparationService) {
        this.modelViewSeparationService = modelViewSeparationService;
    }

    @GetMapping
    public String showModelViewDemo(
            @RequestParam(required = false) String name,
            Model model
    ) {
        String learnerName = normalizeOrDefault(name);
        MvcStudySession session = modelViewSeparationService.createSession();

        model.addAttribute("pageTitle", "Model과 View 분리하기");
        model.addAttribute("learnerName", learnerName);
        model.addAttribute("session", session);
        model.addAttribute("coachMessage", modelViewSeparationService.coachMessage(learnerName));
        model.addAttribute("keyPoints", modelViewSeparationService.keyPoints());
        model.addAttribute("checklist", modelViewSeparationService.checklist());
        model.addAttribute("preparedDate", modelViewSeparationService.preparedDate());
        return session.getReferenceViewName();
    }

    private String normalizeOrDefault(String name) {
        if (!StringUtils.hasText(name)) {
            return "MVC 학습자";
        }
        return name.trim().replaceAll("\\s{2,}", " ");
    }
}
