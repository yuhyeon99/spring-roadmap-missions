package com.goorm.springmissionsplayground.mission04_spring_mvc.task07_message_source.controller;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mission04/task07/messages")
public class MessageSourceDemoController {

    private static final String DEFAULT_NAME = "MVC 학습자";

    private final MessageSource messageSource;

    public MessageSourceDemoController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping
    public String showMessageDemo(
            @RequestParam(required = false) String name,
            Locale locale,
            Model model
    ) {
        String displayName = normalizeOrDefault(name);

        model.addAttribute("displayName", displayName);
        model.addAttribute("localeCode", locale.toLanguageTag());
        model.addAttribute("pageTitle", message("task07.page.title", locale));
        model.addAttribute("pageSubtitle", message("task07.page.subtitle", locale));
        model.addAttribute("pageDescription", message("task07.page.description", locale));
        model.addAttribute("greeting", message("task07.greeting", locale, displayName));
        model.addAttribute("guideTitle", message("task07.guide.title", locale));
        model.addAttribute("guideBody", message("task07.guide.body", locale));
        model.addAttribute("currentLocaleLabel", message("task07.currentLocale", locale, locale.toLanguageTag()));
        model.addAttribute("messageCodeLabel", message("task07.messageCodeLabel", locale));
        model.addAttribute("messageCodeValue", "task07.greeting");
        model.addAttribute("switchKoLabel", message("task07.switch.ko", locale));
        model.addAttribute("switchEnLabel", message("task07.switch.en", locale));
        model.addAttribute("noteTitle", message("task07.note.title", locale));
        model.addAttribute("noteBody", message("task07.note.body", locale));
        return "mission04/task07/message-source-demo";
    }

    private String message(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    private String normalizeOrDefault(String name) {
        if (!StringUtils.hasText(name)) {
            return DEFAULT_NAME;
        }
        return name.trim().replaceAll("\\s{2,}", " ");
    }
}
