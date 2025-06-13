package com.sum_news_BE.service.NewsService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sum_news_BE.domain.User;
import com.sum_news_BE.domain.UserNewsLog;
import com.sum_news_BE.repository.UserNewsLogRepository;
import com.sum_news_BE.repository.UserRepository;
import com.sum_news_BE.security.CustomUserDetails;
import org.bson.types.ObjectId;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NewsServiceImpl implements NewsService {

	private final NewsArticleRepository newsArticleRepository;
	private final NewsSummaryRepository newsSummaryRepository;
	private final UserNewsLogRepository userNewsLogRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		try {
			List<String> jsonFiles = Arrays.asList("cluster_0_summary.json", "cluster_1_summary.json", "cluster_2_summary.json", "cluster_3_summary.json", "cluster_4_summary.json");

			for (String jsonFile : jsonFiles) {
				Resource resource = new ClassPathResource(jsonFile);
				String jsonContent = new String(resource.getInputStream().readAllBytes());
				log.info("읽은 JSON 파일: {}, 내용: {}", jsonFile, jsonContent);

				Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, Map.class);

				// summary 객체 전체를 문자열로 저장
				String summaryJson = objectMapper.writeValueAsString(jsonMap.get("summary"));

				NewsArticle newsArticle = NewsArticle.builder()
					.title((String) jsonMap.get("generated_title"))
					.summary(summaryJson)  // summary 객체 전체를 문자열로 저장
					.clusterId(String.valueOf(jsonMap.get("cluster_id")))
					.publishedAt(LocalDateTime.parse((String) jsonMap.get("timestamp")))
					.createdAt(LocalDateTime.now())
					.build();

				NewsArticle saved = newsArticleRepository.save(newsArticle);
				log.info("저장된 기사 ID: {}, 클러스터 ID: {}", saved.getId(), jsonMap.get("cluster_id"));
			}

			log.info("뉴스 데이터 초기화 완료");
		} catch (IOException e) {
			log.error("뉴스 데이터 초기화 실패", e);
			throw new RuntimeException("초기 데이터 로딩에 실패했습니다.", e);
		}
	}

	@Override
	public NewsResponseDTO.NewsListDTO getMain() {
		try {
			List<String> jsonFiles = Arrays.asList("cluster_0_summary.json", "cluster_1_summary.json", "cluster_2_summary.json", "cluster_3_summary.json", "cluster_4_summary.json");
			List<NewsResponseDTO.NewsClusterDTO> newsClusters = new ArrayList<>();
			log.info("Starting to process {} JSON files", jsonFiles.size());

			for (String jsonFile : jsonFiles) {
				log.info("Processing file: {}", jsonFile);
				Resource resource = new ClassPathResource(jsonFile);
				String jsonContent = new String(resource.getInputStream().readAllBytes());
				Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, Map.class);

				String clusterId = String.valueOf(jsonMap.get("cluster_id"));
				log.info("Found cluster_id: {}", clusterId);
				
				List<NewsArticle> articles = newsArticleRepository.findByClusterId(clusterId);
				log.info("Found {} articles for cluster_id: {}", articles.size(), clusterId);

				if (!articles.isEmpty()) {
					NewsArticle firstArticle = articles.get(0);
					log.info("Processing article with ID: {}", firstArticle.getId());

					try {
						// JSON 파일에서 직접 summary 정보 가져오기
						@SuppressWarnings("unchecked")
						Map<String, Object> summaryMap = (Map<String, Object>) jsonMap.get("summary");
						log.info("Successfully got summary map from JSON");
						
						NewsResponseDTO.Summary summary = NewsResponseDTO.Summary.builder()
							.highlight((String) summaryMap.get("highlight"))
							.article((String) summaryMap.get("article"))
							.background((String) summaryMap.get("background"))
							.build();

						@SuppressWarnings("unchecked")
						List<String> keywords = (List<String>) jsonMap.get("generated_keywords");

						@SuppressWarnings("unchecked")
						List<String> titles = (List<String>) jsonMap.get("titles");

						@SuppressWarnings("unchecked")
						List<Integer> ids = ((List<Number>) jsonMap.get("ids")).stream()
							.map(Number::intValue)
							.collect(Collectors.toList());

						NewsResponseDTO.NewsClusterDTO newsCluster = NewsResponseDTO.NewsClusterDTO.builder()
							.generated_title((String) jsonMap.get("generated_title"))
							.generated_keywords(keywords)
							.cluster_id(Integer.parseInt(clusterId))
							.summary(summary)
							.titles(titles)
							.ids(ids)
							.timestamp(firstArticle.getPublishedAt())
							.build();

						newsClusters.add(newsCluster);
						log.info("Added news cluster to list. Current size: {}", newsClusters.size());
					} catch (Exception e) {
						log.error("Error processing article {}: {}", firstArticle.getId(), e.getMessage(), e);
					}
				} else {
					log.warn("No articles found for cluster_id: {}", clusterId);
				}
			}

			log.info("Total news clusters found: {}", newsClusters.size());
			NewsResponseDTO.NewsListDTO response = NewsResponseDTO.NewsListDTO.builder()
				.newsClusters(newsClusters)
				.build();
			log.info("Created response with {} clusters", response.getNewsClusters() != null ? response.getNewsClusters().size() : 0);
			return response;
		} catch (IOException e) {
			log.error("뉴스 데이터 조회 실패", e);
		}

		log.warn("Returning empty response due to error");
		return NewsResponseDTO.NewsListDTO.builder().build();
	}

	@Override
	public NewsSummaryResponseDTO getNewsSummary(String articleId) {
		NewsSummary summary = newsSummaryRepository.findByArticleId(articleId)
			.orElseThrow(() -> new IllegalArgumentException("기사의 요약 정보가 없습니다."));

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == "anonymous") {
			System.out.println("인증되지 않았거나 익명인 사용자입니다.");
		} else {
			String currentUserEmail = null;
			Object principal = authentication.getPrincipal();

			if (principal instanceof CustomUserDetails) {
				currentUserEmail = ((CustomUserDetails) principal).getUsername();
			} else if (principal instanceof String) {
				currentUserEmail = (String) principal;
			}

			if (currentUserEmail != null) {
				Optional<User> optionalUser = userRepository.findByEmail(currentUserEmail);
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
					System.out.println(currentUserEmail + "의 조회 기록이 업데이트 되었습니다.");
				} else {
					System.out.println("email를 가져오는데 실패했습니다.");
				}
			}
		}

		return NewsSummaryResponseDTO.builder()
			.articleId(summary.getArticleId())
			.summary(summary.getSummary())
			.generatedAt(summary.getGeneratedAt())
			.build();
	}
}
