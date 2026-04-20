package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.controller;

import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.domain.SpringDataJpaUser;
import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.dto.SpringDataJpaUserRequest;
import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.dto.SpringDataJpaUserResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.service.SpringDataJpaUserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission05/task03/users")
public class SpringDataJpaUserController {

    private final SpringDataJpaUserService userService;

    public SpringDataJpaUserController(SpringDataJpaUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<SpringDataJpaUserResponse> create(@RequestBody @Valid SpringDataJpaUserRequest request) {
        SpringDataJpaUser created = userService.create(request.getName(), request.getEmail());
        return ResponseEntity
                .created(URI.create("/mission05/task03/users/" + created.getId()))
                .body(SpringDataJpaUserResponse.from(created));
    }

    @GetMapping
    public List<SpringDataJpaUserResponse> list() {
        return userService.findAll().stream()
                .map(SpringDataJpaUserResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public SpringDataJpaUserResponse get(@PathVariable Long id) {
        return SpringDataJpaUserResponse.from(userService.findById(id));
    }

    @GetMapping("/search")
    public SpringDataJpaUserResponse getByEmail(@RequestParam String email) {
        return SpringDataJpaUserResponse.from(userService.findByEmail(email));
    }

    @PutMapping("/{id}")
    public SpringDataJpaUserResponse update(
            @PathVariable Long id,
            @RequestBody @Valid SpringDataJpaUserRequest request
    ) {
        SpringDataJpaUser updated = userService.update(id, request.getName(), request.getEmail());
        return SpringDataJpaUserResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
