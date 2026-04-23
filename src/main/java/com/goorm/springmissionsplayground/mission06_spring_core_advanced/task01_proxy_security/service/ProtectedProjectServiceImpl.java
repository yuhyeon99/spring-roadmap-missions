package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.ProjectSecurityAction;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.SecurityRequestContext;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto.ProjectOperationPayload;
import org.springframework.stereotype.Service;

@Service("protectedProjectServiceTarget")
public class ProtectedProjectServiceImpl implements ProtectedProjectService {

    @Override
    public ProjectOperationPayload viewProject(String projectId, SecurityRequestContext context) {
        return new ProjectOperationPayload(
                projectId,
                ProjectSecurityAction.VIEW_PROJECT.getActionLabel(),
                "프로젝트 " + projectId + "의 운영 상태와 배포 이력을 조회했습니다."
        );
    }

    @Override
    public ProjectOperationPayload rotateSecrets(String projectId, SecurityRequestContext context) {
        return new ProjectOperationPayload(
                projectId,
                ProjectSecurityAction.ROTATE_SECRETS.getActionLabel(),
                "프로젝트 " + projectId + "의 배포 시크릿을 재발급하고 감사 로그를 남겼습니다."
        );
    }
}
