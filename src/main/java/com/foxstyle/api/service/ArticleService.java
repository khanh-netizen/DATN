package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.ArticleRequest;
import com.foxstyle.api.dto.response.ArticleResponse;
import java.util.List;

public interface ArticleService {
    List<ArticleResponse> getPublished();
    List<ArticleResponse> getAll();
    ArticleResponse create(ArticleRequest request);
    ArticleResponse update(Integer id, ArticleRequest request);
    void delete(Integer id);
}
