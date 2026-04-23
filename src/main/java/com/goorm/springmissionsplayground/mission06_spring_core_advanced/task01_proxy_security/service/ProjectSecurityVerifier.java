package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.ProjectSecurityAction;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.SecurityRequestContext;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception.UnauthenticatedAccessException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception.UnauthorizedProjectAccessException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProjectSecurityVerifier {

    public void verifyAuthentication(
            SecurityRequestContext context,
            ProjectSecurityAction action,
            List<String> securityChecks
    ) {
        securityChecks.add("인증 검사 시작: 사용자=" + context.getUserId());

        if (!context.isAuthenticated()) {
            securityChecks.add("인증 실패: 로그인되지 않은 요청");
            throw new UnauthenticatedAccessException(action.getActionLabel() + " 작업은 로그인 후에만 실행할 수 있습니다.");
        }

        securityChecks.add("인증 성공");
    }

    public void verifyAuthorization(
            SecurityRequestContext context,
            ProjectSecurityAction action,
            List<String> securityChecks
    ) {
        securityChecks.add(
                "권한 검사 시작: 요청 역할=" + context.getRole().name()
                        + ", 필요 역할=" + action.getRequiredRole().name()
        );

        if (!context.getRole().hasAtLeast(action.getRequiredRole())) {
            securityChecks.add("권한 거부");
            throw new UnauthorizedProjectAccessException(
                    action.getActionLabel() + " 작업에는 " + action.getRequiredRole().name() + " 권한이 필요합니다."
            );
        }

        securityChecks.add("권한 승인");
    }
}
