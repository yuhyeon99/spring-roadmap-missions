package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain.RegisteredMemberProfile;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain.StudyTrackOption;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.dto.MemberRegistrationForm;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MemberRegistrationValidationService {

    private static final List<StudyTrackOption> STUDY_TRACK_OPTIONS = List.of(
            new StudyTrackOption("mvc", "Spring MVC", "컨트롤러, 모델, 뷰 흐름을 먼저 이해하는 학습 경로"),
            new StudyTrackOption("validation", "입력값 검증", "폼 데이터 검증과 에러 메시지 처리 흐름을 집중 학습하는 경로"),
            new StudyTrackOption("data-binding", "데이터 바인딩", "요청 파라미터가 객체로 묶이는 과정을 연습하는 경로")
    );

    private static final Map<String, String> TRACK_LABELS = Map.of(
            "mvc", "Spring MVC",
            "validation", "입력값 검증",
            "data-binding", "데이터 바인딩"
    );

    public List<StudyTrackOption> studyTrackOptions() {
        return STUDY_TRACK_OPTIONS;
    }

    public List<String> validationRules() {
        return List.of(
                "@NotBlank: 이름, 이메일, 비밀번호, 학습 트랙처럼 비어 있으면 안 되는 문자열을 검증합니다.",
                "@Size: 이름 길이, 비밀번호 길이, 자기소개 최대 길이처럼 문자열 길이 조건을 검증합니다.",
                "@NotNull: 나이처럼 값이 반드시 있어야 하는 숫자 필드를 검증합니다.",
                "@Email, @Min, @Max: 형식과 숫자 범위를 함께 제한해 잘못된 입력을 빠르게 걸러냅니다."
        );
    }

    public RegisteredMemberProfile register(MemberRegistrationForm form) {
        String normalizedName = normalize(form.getName());
        String normalizedEmail = normalize(form.getEmail()).toLowerCase();
        String studyTrackLabel = TRACK_LABELS.getOrDefault(form.getStudyTrack(), "알 수 없는 트랙");
        String introduction = StringUtils.hasText(form.getIntroduction())
                ? normalize(form.getIntroduction())
                : "자기소개를 아직 작성하지 않았습니다.";

        return new RegisteredMemberProfile(
                normalizedName,
                normalizedEmail,
                form.getAge(),
                studyTrackLabel,
                introduction,
                normalizedName + "님, 회원 가입 폼 검증을 모두 통과했습니다.",
                studyTrackLabel + " 학습 트랙으로 다음 화면 구현 실습을 이어갈 수 있습니다."
        );
    }

    private String normalize(String value) {
        return value.trim().replaceAll("\\s{2,}", " ");
    }
}
