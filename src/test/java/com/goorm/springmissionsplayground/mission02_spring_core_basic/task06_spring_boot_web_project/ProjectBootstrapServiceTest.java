package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectBootstrapResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.service.ProjectBootstrapService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectBootstrapServiceTest {

    private final ProjectBootstrapService projectBootstrapService = new ProjectBootstrapService();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void projectSummary_containsRequiredDependencies() {
        ProjectBootstrapResponse response = projectBootstrapService.projectSummary();

        assertThat(response.getTask()).isEqualTo("mission02 task06 스프링 부트 웹 프로젝트 생성");
        assertThat(response.getBasePackage()).isEqualTo("com.goorm.springmissionsplayground");
        assertThat(response.getDependencies()).hasSize(4);
        assertThat(response.getDependencies())
                .extracting("dependency")
                .contains("spring-boot-starter-web", "spring-boot-starter-validation");
    }

    @Test
    void create_trimsInputValues() {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setProjectName("  mission02-task06-web  ");
        request.setOwner("  kim  ");
        request.setDescription("스프링 부트 웹 프로젝트 생성 실습");

        ProjectCreateResponse response = projectBootstrapService.create(request);

        assertThat(response.getProjectName()).isEqualTo("mission02-task06-web");
        assertThat(response.getOwner()).isEqualTo("kim");
        assertThat(response.getValidation()).isEqualTo("입력값 검증 완료");
    }

    @Test
    void createRequestValidation_rejectsBlankProjectName() {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setProjectName("   ");
        request.setOwner("kim");
        request.setDescription("desc");

        assertThat(validator.validate(request))
                .extracting("message")
                .contains("projectName은 필수입니다.");
    }
}
