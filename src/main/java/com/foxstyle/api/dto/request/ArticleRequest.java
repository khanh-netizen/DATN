package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {
    private Integer productId;
    @NotBlank private String title;
    @NotBlank private String topicName;
    private String author;
    private String summary;
    @NotBlank private String content;
    private String image;
    private String extraImage1;
    private String extraImage2;
    private String status;
}
