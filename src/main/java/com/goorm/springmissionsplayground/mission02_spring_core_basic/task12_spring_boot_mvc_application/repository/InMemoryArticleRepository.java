package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.repository;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.domain.Article;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryArticleRepository implements ArticleRepository {

    private final Map<Long, Article> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public Article save(Article article) {
        Article saved = new Article(sequence.incrementAndGet(),
            article.getTitle(),
            article.getContent(),
            article.getAuthor(),
            article.getCreatedAt());
        store.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public Optional<Article> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Article> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public void clear() {
        store.clear();
        sequence.set(0L);
    }
}
