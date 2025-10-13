package com.sync_BE.service.NewsService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sync_BE.domain.User;
import com.sync_BE.domain.UserNewsLog;
import com.sync_BE.repository.UserNewsLogRepository;
import com.sync_BE.repository.UserRepository;
import com.sync_BE.security.CustomUserDetails;
import org.bson.types.ObjectId;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sync_BE.domain.NewsArticle;
import com.sync_BE.domain.NewsSummary;
import com.sync_BE.repository.NewsArticleRepository;
import com.sync_BE.repository.NewsSummaryRepository;
import com.sync_BE.web.dto.newsDTO.NewsResponseDTO;
import com.sync_BE.web.dto.newsDTO.NewsSummaryResponseDTO;

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
		// 환경 변수로 초기화 여부 제어 (기본값: false, 개발환경에서만 true로 설정)
		String enableInit = System.getenv("ENABLE_DATA_INIT");
		if (!"true".equals(enableInit)) {
			log.info("===== ENABLE_DATA_INIT이 true가 아니므로 데이터 초기화를 건너뜁니다. =====");
			return;
		}
		
		try {
			// 개발 환경에서 데이터 초기화를 위해 기존 데이터 모두 삭제
			newsArticleRepository.deleteAll();
			newsSummaryRepository.deleteAll();
			log.info("===== 기존 뉴스 데이터베이스 초기화 완료. =====");

			List<String> jsonFiles = Arrays.asList("cluster_0_summary.json", "cluster_1_summary.json", "cluster_2_summary.json", "cluster_3_summary.json", "cluster_4_summary.json");

			for (String jsonFile : jsonFiles) {
				log.info("---------- 처리 중인 JSON 파일: {} ----------", jsonFile);
				Resource resource = new ClassPathResource(jsonFile);
				String jsonContent = new String(resource.getInputStream().readAllBytes());
				log.info("읽은 JSON 파일 내용 (처음 100자): {}", jsonContent.substring(0, Math.min(jsonContent.length(), 100)) + "...");

				Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, Map.class);

				@SuppressWarnings("unchecked")
				Map<String, String> summaryMapFromJson = (Map<String, String>) jsonMap.get("summary");

				String generatedTitle = (String) jsonMap.get("generated_title");
				String clusterIdJson = String.valueOf(jsonMap.get("cluster_id"));
				LocalDateTime timestampJson = LocalDateTime.parse((String) jsonMap.get("timestamp"));
				String articleSummaryContent = summaryMapFromJson.get("article");
				String highlightSummaryContent = summaryMapFromJson.get("highlight");

				NewsArticle newsArticle = NewsArticle.builder()
						.title(generatedTitle)
						.summary(articleSummaryContent)
						.clusterId(clusterIdJson)
						.publishedAt(timestampJson)
						.createdAt(LocalDateTime.now())
						.source(null)
						.build();

				NewsArticle savedArticle = newsArticleRepository.save(newsArticle);
				log.info("저장된 NewsArticle 확인: ID={}, Title=\"{}\", ClusterId={}, PublishedAt={}, Summary (짧게)=\"{}\", Source={}",
						savedArticle.getId(), savedArticle.getTitle(), savedArticle.getClusterId(), savedArticle.getPublishedAt(),
						savedArticle.getSummary() != null ? savedArticle.getSummary().substring(0, Math.min(savedArticle.getSummary().length(), 50)) + "..." : "null",
						savedArticle.getSource());

				NewsSummary newsSummary = NewsSummary.builder()
						.summaryText(highlightSummaryContent)
						.generatedAt(timestampJson)
						.article(savedArticle)
						.build();
				newsSummaryRepository.save(newsSummary);
				log.info("저장된 NewsSummary 확인: ID={}, ArticleId={}, SummaryText (짧게)=\"{}\", GeneratedAt={}",
						newsSummary.getSummaryText() != null ? newsSummary.getSummaryText().substring(0, Math.min(newsSummary.getSummaryText().length(), 50)) + "..." : "null",
						newsSummary.getGeneratedAt());
			}

			log.info("===== 뉴스 데이터 초기화 완료. =====");
		} catch (IOException e) {
			log.error("뉴스 데이터 초기화 실패: {}", e.getMessage(), e);
			throw new RuntimeException("초기 데이터 로딩에 실패했습니다.", e);
		} catch (Exception e) {
			log.error("예상치 못한 초기화 오류 발생: {}", e.getMessage(), e);
			throw new RuntimeException("뉴스 데이터 초기화 중 예상치 못한 오류가 발생했습니다.", e);
		}
	}

	@Override
	public NewsResponseDTO.NewsListDTO getMain() {
		log.info("##### 메인 화면 뉴스 목록 조회 요청 시작 #####");
		List<NewsArticle> sortNews = newsArticleRepository.findAllByOrderByCreatedAtDesc();
		log.info("데이터베이스에서 총 {}개의 NewsArticle 기사 발견.", sortNews.size());

		List<NewsResponseDTO.NewsArticleDTO> newsArticleDTO = sortNews.stream()
				.map(article -> {
					log.info("--- NewsArticle 매핑 중: ID={}, Title=\"{}\", ClusterId={}, PublishedAt={}, Source={}",
							article.getId(), article.getTitle(), article.getClusterId(), article.getPublishedAt(), article.getSource());

					String summaryText = newsSummaryRepository.findByArticle(article)
							.map(NewsSummary::getSummaryText)
							.orElseGet(() -> {
								log.warn("NewsSummary를 찾을 수 없음 (articleId: {}). '요약 없음' 반환.", article.getId());
								return "요약 없음";
							});
					log.info("NewsArticle ID {}에 대한 최종 요약 텍스트: {}", article.getId(), summaryText);

					return NewsResponseDTO.NewsArticleDTO.builder()
							.id(article.getId().toString())
							.title(article.getTitle())
							.summaryText(summaryText)
							.clusterId(article.getClusterId())
							.timestamp(article.getPublishedAt())
							.source(article.getSource())
							.publishedAt(article.getPublishedAt())
							.build();
				}).collect(Collectors.toList());

		log.info("##### NewsListDTO 생성 완료. 총 {}개의 기사 DTO. #####", newsArticleDTO.size());
		return NewsResponseDTO.NewsListDTO.builder()
				.newsList(newsArticleDTO)
				.build();
	}

	@Override
	public NewsResponseDTO.NewsClusterDTO getNewsSummaryByClusterId(String clusterId) {
		try {
			log.info("##### 클러스터 ID로 뉴스 상세 조회 요청 시작: clusterId={} #####", clusterId);

			String jsonFile = "cluster_" + clusterId + "_summary.json";
			log.info("관련 JSON 파일 경로: {}", jsonFile);

			Resource resource = new ClassPathResource(jsonFile);
			String jsonContent = new String(resource.getInputStream().readAllBytes());
			log.info("JSON 파일 내용 성공적으로 읽음. (처음 100자): {}", jsonContent.substring(0, Math.min(jsonContent.length(), 100)) + "...");

			Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, Map.class);

			@SuppressWarnings("unchecked")
			Map<String, Object> summaryMap = (Map<String, Object>) jsonMap.get("summary");

			NewsResponseDTO.Summary summary = NewsResponseDTO.Summary.builder()
					.highlight((String) summaryMap.get("highlight"))
					.article((String) summaryMap.get("article"))
					.background((String) summaryMap.get("background"))
					.build();
			log.info("Summary DTO 생성 완료: Highlight={}, Article={}, Background={}",
					summary.getHighlight(), summary.getArticle(), summary.getBackground());

			@SuppressWarnings("unchecked")
			List<String> keywords = (List<String>) jsonMap.get("generated_keywords");

			@SuppressWarnings("unchecked")
			List<String> titles = (List<String>) jsonMap.get("titles");

			@SuppressWarnings("unchecked")
			List<Integer> ids = ((List<Number>) jsonMap.get("ids")).stream()
					.map(Number::intValue)
					.collect(Collectors.toList());

			LocalDateTime timestamp = LocalDateTime.parse((String) jsonMap.get("timestamp"));

			NewsResponseDTO.NewsClusterDTO newsClusterDTO = NewsResponseDTO.NewsClusterDTO.builder()
					.generatedTitle((String) jsonMap.get("generated_title"))
					.generatedKeywords(keywords)
					.clusterId(clusterId)
					.summary(summary)
					.titles(titles)
					.ids(ids)
					.timestamp(timestamp)
					.build();
			log.info("##### NewsClusterDTO 생성 완료: Title=\"{}\", ClusterId={} #####", newsClusterDTO.getGeneratedTitle(), newsClusterDTO.getClusterId());
			return newsClusterDTO;

		} catch (IOException e) {
			log.error("뉴스 상세 정보 조회 실패 (IO 오류): {}", e.getMessage(), e);
			throw new RuntimeException("뉴스 상세 정보 로딩에 실패했습니다.", e);
		} catch (IllegalArgumentException e) {
			log.error("뉴스 상세 정보 조회 실패 (잘못된 인자): {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			log.error("뉴스 상세 정보 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
			throw new RuntimeException("뉴스 상세 정보 조회 중 예상치 못한 오류가 발생했습니다.", e);
		}
	}

	@Override
	public NewsResponseDTO.NewsClusterDTO getNewsSummary(String articleId) {
		try {
			log.info("##### 뉴스 상세 조회 요청 시작: articleId={} #####", articleId);
			NewsArticle article = newsArticleRepository.findById(new ObjectId(articleId))
					.orElseThrow(() -> new IllegalArgumentException("기사를 찾을 수 없습니다."));
			log.info("NewsArticle 조회 성공: ID={}, Title=\"{}\", ClusterId={}", article.getId(), article.getTitle(), article.getClusterId());

			String clusterId = article.getClusterId();
			String jsonFile = "cluster_" + clusterId + "_summary.json";
			log.info("관련 JSON 파일 경로: {}", jsonFile);

			Resource resource = new ClassPathResource(jsonFile);
			String jsonContent = new String(resource.getInputStream().readAllBytes());
			log.info("JSON 파일 내용 성공적으로 읽음. (처음 100자): {}", jsonContent.substring(0, Math.min(jsonContent.length(), 100)) + "...");

			Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, Map.class);

			@SuppressWarnings("unchecked")
			Map<String, Object> summaryMap = (Map<String, Object>) jsonMap.get("summary");

			NewsResponseDTO.Summary summary = NewsResponseDTO.Summary.builder()
					.highlight((String) summaryMap.get("highlight"))
					.article((String) summaryMap.get("article"))
					.background((String) summaryMap.get("background"))
					.build();
			log.info("Summary DTO 생성 완료: Highlight={}, Article={}, Background={}",
					summary.getHighlight(), summary.getArticle(), summary.getBackground());

			@SuppressWarnings("unchecked")
			List<String> keywords = (List<String>) jsonMap.get("generated_keywords");

			@SuppressWarnings("unchecked")
			List<String> titles = (List<String>) jsonMap.get("titles");

			@SuppressWarnings("unchecked")
			List<Integer> ids = ((List<Number>) jsonMap.get("ids")).stream()
					.map(Number::intValue)
					.collect(Collectors.toList());

			NewsResponseDTO.NewsClusterDTO newsClusterDTO = NewsResponseDTO.NewsClusterDTO.builder()
					.generatedTitle((String) jsonMap.get("generated_title"))
					.generatedKeywords(keywords)
					.clusterId(clusterId)
					.summary(summary)
					.titles(titles)
					.ids(ids)
					.timestamp(article.getPublishedAt())
					.build();
			log.info("##### NewsClusterDTO 생성 완료: Title=\"{}\", ClusterId={} #####", newsClusterDTO.getGeneratedTitle(), newsClusterDTO.getClusterId());
			return newsClusterDTO;

		} catch (IOException e) {
			log.error("뉴스 상세 정보 조회 실패 (IO 오류): {}", e.getMessage(), e);
			throw new RuntimeException("뉴스 상세 정보 로딩에 실패했습니다.", e);
		} catch (IllegalArgumentException e) {
			log.error("뉴스 상세 정보 조회 실패 (잘못된 인자): {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			log.error("뉴스 상세 정보 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
			throw new RuntimeException("뉴스 상세 정보 조회 중 예상치 못한 오류가 발생했습니다.", e);
		}
	}

	@Transactional
	public NewsSummaryResponseDTO getNewsSummaryForUserLog(NewsArticle article) {
		NewsSummary summary = newsSummaryRepository.findByArticle(article)
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

				if (optionalUser.isPresent()) {
					User currentUser = optionalUser.get();
					NewsArticle currentArticle = article;

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
				.summary(summary.getSummaryText())
				.generatedAt(summary.getGeneratedAt())
				.build();
	}
}
