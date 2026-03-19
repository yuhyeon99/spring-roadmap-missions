package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain.RegisteredMemberProfile;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain.StudyTrackOption;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.dto.MemberRegistrationForm;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.service.MemberRegistrationValidationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/mission04/task03/members")
public class MemberRegistrationController {

    private static final String FORM_VIEW_NAME = "mission04/task03/member-registration-form";
    private static final String SUCCESS_VIEW_NAME = "mission04/task03/member-registration-success";

    private final MemberRegistrationValidationService memberRegistrationValidationService;

    public MemberRegistrationController(MemberRegistrationValidationService memberRegistrationValidationService) {
        this.memberRegistrationValidationService = memberRegistrationValidationService;
    }

    @ModelAttribute("studyTrackOptions")
    public List<StudyTrackOption> studyTrackOptions() {
        return memberRegistrationValidationService.studyTrackOptions();
    }

    @ModelAttribute("validationRules")
    public List<String> validationRules() {
        return memberRegistrationValidationService.validationRules();
    }

    @GetMapping("/new")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("memberRegistrationForm")) {
            model.addAttribute("memberRegistrationForm", new MemberRegistrationForm());
        }
        model.addAttribute("pageTitle", "검증 애노테이션으로 회원 가입 폼 검증");
        model.addAttribute("pageDescription", "폼 입력값을 DTO에 바인딩한 뒤 @NotNull, @Size 같은 검증 애노테이션으로 필수값과 길이 조건을 확인합니다.");
        model.addAttribute("formAction", "/mission04/task03/members/new");
        return FORM_VIEW_NAME;
    }

    @PostMapping("/new")
    public String register(
            @Valid @ModelAttribute("memberRegistrationForm") MemberRegistrationForm memberRegistrationForm,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("pageTitle", "검증 애노테이션으로 회원 가입 폼 검증");
        model.addAttribute("pageDescription", "입력값 검증에 실패하면 같은 폼 뷰를 다시 렌더링하고, 통과하면 성공 화면으로 이동합니다.");
        model.addAttribute("formAction", "/mission04/task03/members/new");

        if (bindingResult.hasErrors()) {
            model.addAttribute("submissionStatus", "입력값을 다시 확인해 주세요.");
            return FORM_VIEW_NAME;
        }

        RegisteredMemberProfile registeredMember = memberRegistrationValidationService.register(memberRegistrationForm);
        model.addAttribute("pageTitle", "회원 가입 검증 통과 결과");
        model.addAttribute("registeredMember", registeredMember);
        return SUCCESS_VIEW_NAME;
    }
}
