package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.ProjectSecurityAction;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.SecurityRequestContext;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto.ProjectOperationPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class SecurityVerificationProjectServiceProxy implements ProtectedProjectService {

    private final ProtectedProjectService target;
    private final ProjectSecurityVerifier projectSecurityVerifier;

    public SecurityVerificationProjectServiceProxy(
            @Qualifier("protectedProjectServiceTarget") ProtectedProjectService target,
            ProjectSecurityVerifier projectSecurityVerifier
    ) {
        this.target = target;
        this.projectSecurityVerifier = projectSecurityVerifier;
    }

    @Override
    public ProjectOperationPayload viewProject(String projectId, SecurityRequestContext context) {
        return execute(ProjectSecurityAction.VIEW_PROJECT, projectId, context, target::viewProject);
    }

    @Override
    public ProjectOperationPayload rotateSecrets(String projectId, SecurityRequestContext context) {
        return execute(ProjectSecurityAction.ROTATE_SECRETS, projectId, context, target::rotateSecrets);
    }

    private ProjectOperationPayload execute(
            ProjectSecurityAction action,
            String projectId,
            SecurityRequestContext context,
            BiFunction<String, SecurityRequestContext, ProjectOperationPayload> invocation
    ) {
        List<String> securityChecks = new ArrayList<>();
        securityChecks.add("프록시 진입: " + action.getActionLabel());

        projectSecurityVerifier.verifyAuthentication(context, action, securityChecks);
        projectSecurityVerifier.verifyAuthorization(context, action, securityChecks);

        securityChecks.add("대상 서비스 호출 전");
        ProjectOperationPayload payload = invocation.apply(projectId, context);
        securityChecks.add("대상 서비스 호출 후");
        securityChecks.add("사후 보안 로그 기록 완료");

        return payload.withSecurityMetadata(
                context.getUserId(),
                context.getRole().name(),
                action.getRequiredRole().name(),
                true,
                securityChecks
        );
    }
}
