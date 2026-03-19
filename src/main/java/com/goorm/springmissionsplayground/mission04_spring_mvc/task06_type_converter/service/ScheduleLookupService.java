package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.dto.ScheduleLookupResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ScheduleLookupService {

    private static final Map<DayOfWeek, String> DAY_NAMES = Map.of(
            DayOfWeek.MONDAY, "월요일",
            DayOfWeek.TUESDAY, "화요일",
            DayOfWeek.WEDNESDAY, "수요일",
            DayOfWeek.THURSDAY, "목요일",
            DayOfWeek.FRIDAY, "금요일",
            DayOfWeek.SATURDAY, "토요일",
            DayOfWeek.SUNDAY, "일요일"
    );

    private static final Map<DayOfWeek, String> AGENDAS = Map.of(
            DayOfWeek.MONDAY, "주간 계획을 정리하는 날입니다.",
            DayOfWeek.TUESDAY, "컨트롤러 요청 흐름을 점검하는 날입니다.",
            DayOfWeek.WEDNESDAY, "데이터 바인딩과 검증을 복습하는 날입니다.",
            DayOfWeek.THURSDAY, "타입 변환기 설정을 실습하는 날입니다.",
            DayOfWeek.FRIDAY, "예외 처리와 응답 형식을 정리하는 날입니다.",
            DayOfWeek.SATURDAY, "개인 학습 과제를 진행하는 날입니다.",
            DayOfWeek.SUNDAY, "이번 주 학습 내용을 회고하는 날입니다."
    );

    public ScheduleLookupResponse findSchedule(LocalDate requestedDate) {
        DayOfWeek dayOfWeek = requestedDate.getDayOfWeek();
        return new ScheduleLookupResponse(
                requestedDate,
                DAY_NAMES.get(dayOfWeek),
                AGENDAS.get(dayOfWeek),
                "query parameter 문자열이 LocalDate로 변환된 뒤 일정 조회에 사용되었습니다."
        );
    }
}
