package com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.service.ViewResolverDemoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mission04/task02/view-resolver")
public class ViewResolverController {

    private static final String DEFAULT_NAME = "MVC 학습자";
    private static final String LOGICAL_VIEW_NAME = "mission04/task02/view-resolver-demo";
    private static final String RESOLVED_TEMPLATE_PATH = "classpath:/templates/mission04/task02/view-resolver-demo.html";

    private final ViewResolverDemoService viewResolverDemoService;

    public ViewResolverController(ViewResolverDemoService viewResolverDemoService) {
        this.viewResolverDemoService = viewResolverDemoService;
    }

    @GetMapping
    public String showViewResolverPage(@RequestParam(required = false) String name, Model model) {
        String displayName = normalizeOrDefault(name);

        model.addAttribute("pageTitle", "View Resolver 설정과 활용");
        model.addAttribute("displayName", displayName);
        model.addAttribute("templateEngine", "Thymeleaf");
        model.addAttribute("logicalViewName", LOGICAL_VIEW_NAME);
        model.addAttribute("resolvedTemplatePath", RESOLVED_TEMPLATE_PATH);
        model.addAttribute("welcomeMessage", viewResolverDemoService.welcomeMessage(displayName));
        model.addAttribute("resolverFlow", viewResolverDemoService.resolverFlow());
        model.addAttribute("modelExamples", viewResolverDemoService.modelExamples(displayName));
        model.addAttribute("renderedAt", viewResolverDemoService.renderedAt());
        return LOGICAL_VIEW_NAME;
    }

    private String normalizeOrDefault(String name) {
        if (!StringUtils.hasText(name)) {
            return DEFAULT_NAME;
        }
        return name.trim().replaceAll("\\s{2,}", " ");
    }
}
