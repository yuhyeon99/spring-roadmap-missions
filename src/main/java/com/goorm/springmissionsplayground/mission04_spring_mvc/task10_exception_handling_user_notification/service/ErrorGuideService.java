package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.domain.ErrorGuide;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.exception.ErrorGuideNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ErrorGuideService {

    private final Map<Long, ErrorGuide> guides = new LinkedHashMap<>();

    public ErrorGuideService() {
        guides.put(1L, new ErrorGuide(
                1L,
                "404 에러 페이지가 왜 필요한가",
                "사용자가 없는 자원에 접근했을 때 현재 요청이 실패했음을 분명하게 알려주고, 다음 행동을 안내합니다.",
                "목록 페이지로 돌아가거나 올바른 식별자를 다시 입력하도록 유도합니다."
        ));
        guides.put(2L, new ErrorGuide(
                2L,
                "예외를 컨트롤러 밖으로 보내는 이유",
                "비즈니스 계층은 자원 조회 실패를 예외로 표현하고, 웹 계층은 이를 화면 응답으로 변환하는 역할만 담당합니다.",
                "서비스는 찾기/검증 책임에 집중하고, 사용자 알림 화면은 별도 예외 처리기로 분리합니다."
        ));
        guides.put(3L, new ErrorGuide(
                3L,
                "사용자 친화적인 알림 메시지 구성",
                "상태 코드만 노출하면 사용자는 다음 행동을 알기 어렵기 때문에 요청 경로와 복귀 경로를 함께 보여줍니다.",
                "실패 원인, 요청 경로, 돌아갈 링크를 한 화면에 제공해 재시도 흐름을 단순하게 만듭니다."
        ));
    }

    public List<ErrorGuide> findAll() {
        return new ArrayList<>(guides.values());
    }

    public ErrorGuide findById(Long id) {
        ErrorGuide guide = guides.get(id);
        if (guide == null) {
            throw new ErrorGuideNotFoundException(id);
        }
        return guide;
    }
}
