package com.sum_news_BE.service.NewsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sum_news_BE.domain.User;
import com.sum_news_BE.domain.UserNewsLog;
import com.sum_news_BE.repository.UserNewsLogRepository;
import com.sum_news_BE.repository.UserRepository;
import com.sum_news_BE.security.CustomUserDetails;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sum_news_BE.domain.NewsArticle;
import com.sum_news_BE.domain.NewsSummary;
import com.sum_news_BE.repository.NewsArticleRepository;
import com.sum_news_BE.repository.NewsSummaryRepository;
import com.sum_news_BE.web.dto.NewsResponseDTO;
import com.sum_news_BE.web.dto.NewsSummaryResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsServiceImpl implements NewsService {

	private final NewsArticleRepository newsArticleRepository;
	private final NewsSummaryRepository newsSummaryRepository;
	private final UserNewsLogRepository userNewsLogRepository;
	private final UserRepository userRepository;


	@Override
	public NewsResponseDTO.NewsListDTO getMain() { //뉴스 목록
		List<NewsArticle> sortNews = newsArticleRepository.findAllByOrderByCreatedAtDesc();

		List<NewsResponseDTO.NewsArticleDTO> newsArticleDTO = sortNews.stream()
				.map(article -> {
					String summaryText = newsSummaryRepository.findByArticleId(article.getId().toString())
							.map(NewsSummary::getSummaryText)
							.orElse("요약 없음");

					return NewsResponseDTO.NewsArticleDTO.builder()
							.id(article.getId().toString())
							.title(article.getTitle())
							.summaryText(summaryText)
							.source(article.getSource())
							.publishedAt(article.getPublishedAt())
							.build();
				}).collect(Collectors.toList());
		return NewsResponseDTO.NewsListDTO.builder()
				.newsList(newsArticleDTO)
				.build();
	}

	@Override
	public NewsSummaryResponseDTO getNewsSummary(String articleId) {
		NewsSummary summary = newsSummaryRepository.findByArticleId(articleId)
				.orElseThrow(() -> new IllegalArgumentException("기사의 요약 정보가 없습니다."));

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == "anonymous") {
			System.out.println("인증되지 않았거나 익명인 사용자입니다.");
		} else {
			String currentUserId = null;
			Object principal = authentication.getPrincipal();

			if (principal instanceof CustomUserDetails) {
				currentUserId = ((CustomUserDetails) principal).getUsername();
			} else if (principal instanceof String) {
				currentUserId = (String) principal;
			}

			if (currentUserId != null) {
				Optional<User> optionalUser = userRepository.findByUserid(currentUserId);
				Optional<NewsArticle> optionalNewsArticle = newsArticleRepository.findById(new ObjectId(articleId));

				if (optionalUser.isPresent() && optionalNewsArticle.isPresent()) {
					User currentUser = optionalUser.get();
					NewsArticle currentArticle = optionalNewsArticle.get();

					Optional<UserNewsLog> optionalUserNewsLog = userNewsLogRepository.findByUserAndArticle(currentUser, currentArticle);
					optionalUserNewsLog.ifPresentOrElse(
							log -> {
								log.setRead(true);
								log.setReadAt(LocalDateTime.now());
							}, () -> {
								UserNewsLog userNewsLog = new UserNewsLog();
								userNewsLog.setUser(currentUser);
								userNewsLog.setArticle(currentArticle);
								userNewsLog.setRead(true);
								userNewsLog.setReadAt(LocalDateTime.now());
								userNewsLogRepository.save(userNewsLog);
							}
					);
					System.out.println(currentUserId + "의 조회 기록이 업데이트 되었습니다.");
				} else {
					System.out.println("userId를 가져오는데 실패했습니다.");
				}
			}
		}

		return NewsSummaryResponseDTO.builder()
				.articleId(summary.getArticleId())
				.summaryText(summary.getSummaryText())
				.generatedAt(summary.getGeneratedAt())
				.build();
	}
}
