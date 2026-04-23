package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.SecurityRequestContext;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto.ProjectOperationPayload;

public interface ProtectedProjectService {

    ProjectOperationPayload viewProject(String projectId, SecurityRequestContext context);

    ProjectOperationPayload rotateSecrets(String projectId, SecurityRequestContext context);
}
