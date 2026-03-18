package com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.domain.ViewResolverStudyItem;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ViewResolverDemoService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String welcomeMessage(String displayName) {
        return displayName + "님, 컨트롤러가 반환한 논리 뷰 이름을 View Resolver가 실제 템플릿으로 연결합니다.";
    }

    public List<ViewResolverStudyItem> resolverFlow() {
        return List.of(
                new ViewResolverStudyItem(
                        "1. Controller 반환",
                        "컨트롤러는 `mission04/task02/view-resolver-demo` 같은 논리 뷰 이름만 반환합니다."
                ),
                new ViewResolverStudyItem(
                        "2. View Resolver 선택",
                        "Thymeleaf View Resolver가 prefix와 suffix 설정을 조합해 실제 템플릿 경로를 찾습니다."
                ),
                new ViewResolverStudyItem(
                        "3. 템플릿 렌더링",
                        "Model에 담긴 데이터를 HTML에 바인딩해 브라우저로 최종 응답을 보냅니다."
                )
        );
    }

    public List<String> modelExamples(String displayName) {
        return List.of(
                "displayName = " + displayName,
                "templateEngine = Thymeleaf",
                "resolvedTemplatePath = classpath:/templates/mission04/task02/view-resolver-demo.html"
        );
    }

    public String renderedAt() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }
}
