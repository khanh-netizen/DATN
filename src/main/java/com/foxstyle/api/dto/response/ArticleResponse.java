package com.foxstyle.api.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {
    private Integer id;
    private Integer productId;
    private String title;
    private String slug;
    private String topicName;
    private String author;
    private String summary;
    private String content;
    private String image;
    private String extraImage1;
    private String extraImage2;
    private Integer views;
    private String status;
    private LocalDateTime publishDate;
    private LocalDateTime createdAt;
}
