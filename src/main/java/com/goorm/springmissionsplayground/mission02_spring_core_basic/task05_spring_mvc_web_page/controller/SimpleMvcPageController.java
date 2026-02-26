package com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.dto.LearningRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.service.MvcPageContentService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mission02/task05/mvc")
public class SimpleMvcPageController {

    private static final String DEFAULT_NAME = "학습자";
    private static final String DEFAULT_TOPIC = "Spring MVC";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MvcPageContentService mvcPageContentService;

    public SimpleMvcPageController(MvcPageContentService mvcPageContentService) {
        this.mvcPageContentService = mvcPageContentService;
    }

    @GetMapping
    public String showPage(@RequestParam(required = false) String name, Model model) {
        String displayName = normalizeOrDefault(name, DEFAULT_NAME);
        renderModel(model, displayName, DEFAULT_TOPIC, false);
        return "mission02/task05/home";
    }

    @PostMapping("/preview")
    public String previewPage(@ModelAttribute LearningRequest learningRequest, Model model) {
        String displayName = normalizeOrDefault(learningRequest.getName(), DEFAULT_NAME);
        String topic = normalizeOrDefault(learningRequest.getTopic(), DEFAULT_TOPIC);
        renderModel(model, displayName, topic, true);
        return "mission02/task05/home";
    }

    private void renderModel(Model model, String displayName, String topic, boolean submitted) {
        model.addAttribute("displayName", displayName);
        model.addAttribute("topic", topic);
        model.addAttribute("submitted", submitted);
        model.addAttribute("serverTime", LocalDateTime.now().format(TIME_FORMATTER));
        model.addAttribute("welcomeMessage", mvcPageContentService.welcomeMessage(displayName, topic));
        model.addAttribute("learningChecklist", mvcPageContentService.learningChecklist(topic));
        model.addAttribute("learningRequest", new LearningRequest());
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return value.trim().replaceAll("\\s{2,}", " ");
    }
}
