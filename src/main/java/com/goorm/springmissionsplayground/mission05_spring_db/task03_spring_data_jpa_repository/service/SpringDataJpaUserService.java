package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.domain.SpringDataJpaUser;
import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.repository.SpringDataJpaUserRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class SpringDataJpaUserService {

    private final SpringDataJpaUserRepository userRepository;

    public SpringDataJpaUserService(SpringDataJpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SpringDataJpaUser create(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        return userRepository.save(new SpringDataJpaUser(name, email));
    }

    @Transactional(readOnly = true)
    public List<SpringDataJpaUser> findAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional(readOnly = true)
    public SpringDataJpaUser findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public SpringDataJpaUser findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    public SpringDataJpaUser update(Long id, String name, String email) {
        SpringDataJpaUser user = findById(id);
        validateDuplicatedEmail(email, id);
        user.updateProfile(name, email);
        return user;
    }

    public void delete(Long id) {
        SpringDataJpaUser user = findById(id);
        userRepository.delete(user);
    }

    private void validateDuplicatedEmail(String email, Long currentUserId) {
        userRepository.findByEmail(email)
                .filter(existingUser -> !existingUser.getId().equals(currentUserId))
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
                });
    }
}
