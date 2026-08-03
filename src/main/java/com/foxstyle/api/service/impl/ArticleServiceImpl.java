package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.ArticleRequest;
import com.foxstyle.api.dto.response.ArticleResponse;
import com.foxstyle.api.entity.Article;
import com.foxstyle.api.entity.ArticleTopic;
import com.foxstyle.api.entity.Product;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.ArticleRepository;
import com.foxstyle.api.repository.ArticleTopicRepository;
import com.foxstyle.api.repository.ProductRepository;
import com.foxstyle.api.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleTopicRepository topicRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponse> getPublished() {
        return articleRepository.findByStatusOrderByPublishedAtDesc("published")
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponse> getAll() {
        return articleRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    @Override
    public ArticleResponse create(ArticleRequest request) {
        Article article = new Article();
        article.setSlug(uniqueSlug(request.getTitle(), null));
        apply(article, request);
        return toResponse(articleRepository.save(article));
    }

    @Override
    public ArticleResponse update(Integer id, ArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết: " + id));
        article.setSlug(uniqueSlug(request.getTitle(), article));
        apply(article, request);
        return toResponse(articleRepository.save(article));
    }

    @Override
    public void delete(Integer id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết: " + id));
        articleRepository.delete(article);
    }

    private void apply(Article article, ArticleRequest request) {
        article.setTopic(findOrCreateTopic(request.getTopicName()));
        article.setTitle(request.getTitle().trim());
        article.setAuthorName(request.getAuthor());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setImageUrl(request.getImage());
        article.setExtraImage1(request.getExtraImage1());
        article.setExtraImage2(request.getExtraImage2());
        String status = request.getStatus() == null ? "draft" : request.getStatus().toLowerCase(Locale.ROOT);
        article.setStatus(status);
        if ("published".equals(status) && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        LinkedHashSet<Product> products = new LinkedHashSet<>();
        if (request.getProductId() != null) {
            products.add(productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy sản phẩm: " + request.getProductId())));
        }
        article.setProducts(products);
    }

    private ArticleTopic findOrCreateTopic(String name) {
        String topicName = name.trim();
        return topicRepository.findByTopicNameIgnoreCase(topicName).orElseGet(() -> {
            String base = slugify(topicName);
            String slug = base;
            int suffix = 2;
            while (topicRepository.existsBySlug(slug)) slug = base + "-" + suffix++;
            return topicRepository.save(ArticleTopic.builder()
                    .topicName(topicName)
                    .slug(slug)
                    .description("Chủ đề được tạo tự động từ bài viết.")
                    .build());
        });
    }

    private String uniqueSlug(String title, Article current) {
        String base = slugify(title);
        if (current != null && base.equals(current.getSlug())) return base;
        String slug = base;
        int suffix = 2;
        while (articleRepository.existsBySlug(slug)) slug = base + "-" + suffix++;
        return slug;
    }

    private String slugify(String value) {
        String slug = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "bai-viet-" + System.currentTimeMillis() : slug;
    }

    private ArticleResponse toResponse(Article article) {
        Integer productId = article.getProducts().stream()
                .findFirst().map(Product::getProductId).orElse(null);
        return ArticleResponse.builder()
                .id(article.getArticleId())
                .productId(productId)
                .title(article.getTitle())
                .slug(article.getSlug())
                .topicName(article.getTopic().getTopicName())
                .author(article.getAuthorName())
                .summary(article.getSummary())
                .content(article.getContent())
                .image(article.getImageUrl())
                .extraImage1(article.getExtraImage1())
                .extraImage2(article.getExtraImage2())
                .views(article.getViewCount())
                .status(article.getStatus())
                .publishDate(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .build();
    }
}
