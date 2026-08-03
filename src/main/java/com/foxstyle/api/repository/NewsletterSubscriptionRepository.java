package com.foxstyle.api.repository;

import com.foxstyle.api.entity.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Integer> {

    boolean existsByEmail(String email);

    Optional<NewsletterSubscription> findByEmail(String email);
}
