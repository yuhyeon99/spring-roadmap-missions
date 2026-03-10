package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.controller;

import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.domain.HttpMethodNote;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.dto.HttpMethodNoteRequest;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.service.HttpMethodNoteService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/mission03/task01/notes")
public class HttpMethodsDemoController {

    private final HttpMethodNoteService httpMethodNoteService;

    public HttpMethodsDemoController(HttpMethodNoteService httpMethodNoteService) {
        this.httpMethodNoteService = httpMethodNoteService;
    }

    @GetMapping
    public List<HttpMethodNote> list() {
        return httpMethodNoteService.listNotes();
    }

    @GetMapping("/{id}")
    public HttpMethodNote find(@PathVariable Long id) {
        return httpMethodNoteService.find(id);
    }

    @PostMapping
    public ResponseEntity<HttpMethodNote> create(@RequestBody HttpMethodNoteRequest request) {
        HttpMethodNote created = httpMethodNoteService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HttpMethodNote> replace(@PathVariable Long id, @RequestBody HttpMethodNoteRequest request) {
        HttpMethodNote replaced = httpMethodNoteService.replace(id, request);
        return ResponseEntity.ok(replaced);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        httpMethodNoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
