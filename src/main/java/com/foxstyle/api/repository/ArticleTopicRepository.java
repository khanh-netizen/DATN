package com.foxstyle.api.repository;

import com.foxstyle.api.entity.ArticleTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArticleTopicRepository extends JpaRepository<ArticleTopic, Integer> {
    Optional<ArticleTopic> findByTopicNameIgnoreCase(String topicName);
    boolean existsBySlug(String slug);
    List<ArticleTopic> findAllByOrderByTopicNameAsc();
}
