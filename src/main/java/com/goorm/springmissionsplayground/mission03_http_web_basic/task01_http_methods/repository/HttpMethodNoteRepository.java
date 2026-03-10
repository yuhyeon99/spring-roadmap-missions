package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.repository;

import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.domain.HttpMethodNote;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class HttpMethodNoteRepository {

    private final ConcurrentMap<Long, HttpMethodNote> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public List<HttpMethodNote> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparingLong(HttpMethodNote::id))
                .toList();
    }

    public Optional<HttpMethodNote> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public HttpMethodNote create(String title, String content) {
        long id = sequence.incrementAndGet();
        HttpMethodNote note = new HttpMethodNote(id, title, content, LocalDateTime.now());
        store.put(id, note);
        return note;
    }

    public HttpMethodNote upsert(Long id, String title, String content) {
        sequence.updateAndGet(current -> Math.max(current, id));
        HttpMethodNote note = new HttpMethodNote(id, title, content, LocalDateTime.now());
        store.put(id, note);
        return note;
    }

    public void delete(Long id) {
        store.remove(id);
    }

    public void clear() {
        store.clear();
        sequence.set(0);
    }
}
