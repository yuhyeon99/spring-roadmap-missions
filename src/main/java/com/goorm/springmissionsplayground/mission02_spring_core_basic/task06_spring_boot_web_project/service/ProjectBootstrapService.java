package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.DependencyItem;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectBootstrapResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProjectBootstrapService {

    public ProjectBootstrapResponse projectSummary() {
        return new ProjectBootstrapResponse(
                "mission02 task06 스프링 부트 웹 프로젝트 생성",
                "com.goorm.springmissionsplayground",
                List.of(
                        new DependencyItem("spring-boot-starter-web", "REST API 및 웹 요청 처리"),
                        new DependencyItem("spring-boot-starter-thymeleaf", "서버 사이드 HTML 템플릿 렌더링"),
                        new DependencyItem("spring-boot-starter-validation", "요청 데이터 검증(@Valid)"),
                        new DependencyItem("spring-boot-starter-test", "테스트 코드 실행 환경")
                )
        );
    }

    public ProjectCreateResponse create(ProjectCreateRequest request) {
        return new ProjectCreateResponse(
                request.getProjectName().trim(),
                request.getOwner().trim(),
                "Spring Boot 웹 애플리케이션 초기 구성이 완료되었습니다.",
                "입력값 검증 완료"
        );
    }
}
