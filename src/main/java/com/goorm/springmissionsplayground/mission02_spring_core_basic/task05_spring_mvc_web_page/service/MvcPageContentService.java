package com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.service;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MvcPageContentService {

    public String welcomeMessage(String name, String topic) {
        return name + "님, " + topic + " 학습을 위한 MVC 데모 페이지입니다.";
    }

    public List<String> learningChecklist(String topic) {
        return List.of(
                "Controller: 요청 URL을 받아 Model과 View를 연결",
                "Model: 뷰 렌더링에 필요한 데이터 전달",
                "View(Thymeleaf): 서버 데이터를 HTML로 출력",
                "현재 학습 주제: " + topic
        );
    }
}
