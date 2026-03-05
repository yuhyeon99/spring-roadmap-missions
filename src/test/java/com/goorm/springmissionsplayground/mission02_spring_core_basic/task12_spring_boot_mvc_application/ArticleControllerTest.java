package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleListResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.exception.ArticleNotFoundException;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.repository.ArticleRepository;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ArticleControllerTest {

    @Autowired
    ArticleService articleService;

    @Autowired
    ArticleRepository articleRepository;

    @BeforeEach
    void setUp() {
        articleRepository.clear();
    }

    @Test
    @DisplayName("글을 생성하면 ID와 작성 정보가 반환된다")
    void createArticle() {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("첫 글");
        request.setContent("본문 내용");
        request.setAuthor("작성자A");

        ArticleResponse response = articleService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("첫 글");
        assertThat(response.getAuthor()).isEqualTo("작성자A");
    }

    @Test
    @DisplayName("목록 조회는 최근 생성 순으로 반환한다")
    void listArticles() {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("목록 글");
        request.setContent("내용");
        request.setAuthor("작성자B");
        articleService.create(request);

        ArticleListResponse list = articleService.list();

        assertThat(list.getArticles())
            .isNotEmpty()
            .first()
            .extracting(ArticleResponse::getTitle)
            .isEqualTo("목록 글");
    }

    @Test
    @DisplayName("없는 글을 조회하면 예외가 발생한다")
    void getArticle_notFound() {
        assertThatThrownBy(() -> articleService.get(999L))
            .isInstanceOf(ArticleNotFoundException.class);
    }

    @Test
    @DisplayName("삭제 후 조회하면 예외가 발생한다")
    void deleteArticle_thenNotFound() {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("삭제 대상");
        request.setContent("삭제 내용");
        request.setAuthor("작성자C");
        ArticleResponse created = articleService.create(request);

        articleService.delete(created.getId());

        assertThatThrownBy(() -> articleService.get(created.getId()))
            .isInstanceOf(ArticleNotFoundException.class);
    }
}
