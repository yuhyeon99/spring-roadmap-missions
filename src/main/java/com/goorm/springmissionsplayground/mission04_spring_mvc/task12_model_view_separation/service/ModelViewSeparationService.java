package com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.domain.MvcStudySession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ModelViewSeparationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MvcStudySession createSession() {
        return new MvcStudySession(
                "Model과 View 분리하기",
                "스프링 MVC 코치",
                "컨트롤러는 데이터를 준비하고, 뷰는 받은 데이터를 화면으로 표현하는 구조를 익힙니다.",
                "mission04/task12/model-view-demo"
        );
    }

    public List<String> keyPoints() {
        return List.of(
                "컨트롤러는 요청을 해석하고 필요한 데이터를 Model에 담습니다.",
                "뷰 템플릿은 Model 값을 읽어 HTML을 렌더링합니다.",
                "HTML 구조를 바꿔도 컨트롤러의 비즈니스 준비 로직은 그대로 둘 수 있습니다."
        );
    }

    public List<String> checklist() {
        return List.of(
                "컨트롤러가 뷰 이름을 반환하는지 확인",
                "Model 속성이 템플릿에서 올바르게 출력되는지 확인",
                "동일한 템플릿이 데이터만 바꿔 재사용될 수 있는지 확인"
        );
    }

    public String coachMessage(String learnerName) {
        return learnerName + "님, 컨트롤러는 데이터를 준비하고 View는 표현을 담당합니다.";
    }

    public String preparedDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
}
