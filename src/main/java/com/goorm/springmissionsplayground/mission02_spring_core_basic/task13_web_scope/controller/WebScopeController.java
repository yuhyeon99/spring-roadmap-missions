package com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope.dto.ScopeSnapshot;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope.scope.ApplicationStats;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope.scope.RequestTrace;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope.scope.SessionCart;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task13/scopes")
public class WebScopeController {

    private final RequestTrace requestTrace;
    private final SessionCart sessionCart;
    private final ApplicationStats applicationStats;

    public WebScopeController(RequestTrace requestTrace, SessionCart sessionCart, ApplicationStats applicationStats) {
        this.requestTrace = requestTrace;
        this.sessionCart = sessionCart;
        this.applicationStats = applicationStats;
    }

    @GetMapping
    public ScopeSnapshot snapshot() {
        applicationStats.increase();
        return toSnapshot();
    }

    @PostMapping("/cart")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addCartItem() {
        applicationStats.increase();
        sessionCart.addItem();
    }

    private ScopeSnapshot toSnapshot() {
        return new ScopeSnapshot(
            new ScopeSnapshot.RequestScopeInfo(requestTrace.getRequestId(), requestTrace.getCreatedAt()),
            new ScopeSnapshot.SessionScopeInfo(sessionCart.getSessionId(), sessionCart.getItemCount()),
            new ScopeSnapshot.ApplicationScopeInfo(applicationStats.getTotalHits())
        );
    }
}
