package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.service;

import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.domain.HttpMethodNote;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.dto.HttpMethodNoteRequest;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.repository.HttpMethodNoteRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HttpMethodNoteService {

    private final HttpMethodNoteRepository repository;

    public HttpMethodNoteService(HttpMethodNoteRepository repository) {
        this.repository = repository;
    }

    public List<HttpMethodNote> listNotes() {
        return repository.findAll();
    }

    public HttpMethodNote find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다."));
    }

    public HttpMethodNote create(HttpMethodNoteRequest request) {
        request.validate();
        return repository.create(request.title().trim(), request.content().trim());
    }

    public HttpMethodNote replace(Long id, HttpMethodNoteRequest request) {
        request.validate();
        if (id < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id는 1 이상의 값이어야 합니다.");
        }
        return repository.upsert(id, request.title().trim(), request.content().trim());
    }

    public void delete(Long id) {
        repository.delete(id);
    }
}
