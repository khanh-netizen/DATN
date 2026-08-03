package com.foxstyle.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Integer topicId;

    @Column(name = "topic_name", nullable = false, unique = true, length = 150)
    private String topicName;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Byte status = 1;
}
