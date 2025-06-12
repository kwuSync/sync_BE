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
			// JSON 파일 목록
			List<String> jsonFiles = Arrays.asList("cluster_2_summary.json", "cluster_3_summary.json");

			for (String jsonFile : jsonFiles) {
				// JSON 파일 읽기
				Resource resource = new ClassPathResource(jsonFile);
				String jsonContent = new String(resource.getInputStream().readAllBytes());
				log.info("읽은 JSON 파일: {}, 내용: {}", jsonFile, jsonContent);

				// JSON을 Map으로 파싱하여 원본 데이터 유지
				Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, Map.class);

				// MongoDB에 저장
				NewsArticle newsArticle = NewsArticle.builder()
					.clusterId(String.valueOf(jsonMap.get("cluster_id")))
					.summary((String) jsonMap.get("summary"))
					.title(String.join("|", (List<String>) jsonMap.get("titles")))
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
			// JSON 파일 목록
			List<String> jsonFiles = Arrays.asList("cluster_2_summary.json", "cluster_3_summary.json");
			List<NewsResponseDTO.NewsClusterDTO> newsClusters = new ArrayList<>();

			for (String jsonFile : jsonFiles) {
				// JSON 파일 읽기
				Resource resource = new ClassPathResource(jsonFile);
				String jsonContent = new String(resource.getInputStream().readAllBytes());

				// JSON을 Map으로 파싱
				Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, Map.class);

				// MongoDB에서 해당 클러스터의 데이터 조회
				String clusterId = String.valueOf(jsonMap.get("cluster_id"));
				NewsArticle article = newsArticleRepository.findByClusterId(clusterId)
					.orElse(null);

				if (article != null) {
					List<String> titles = Arrays.asList(article.getTitle().split("\\|"));

					// ids를 Integer 리스트로 변환
					List<Integer> ids = ((List<Number>) jsonMap.get("ids")).stream()
						.map(Number::intValue)
						.collect(Collectors.toList());

					NewsResponseDTO.NewsClusterDTO newsCluster = NewsResponseDTO.NewsClusterDTO.builder()
						.clusterId(Integer.parseInt(clusterId))
						.summary(article.getSummary())
						.titles(titles)
						.ids(ids)  // 변환된 Integer 리스트 사용
						.timestamp(article.getPublishedAt())
						.build();

					newsClusters.add(newsCluster);
				}
			}

			return NewsResponseDTO.NewsListDTO.builder()
				.newsClusters(newsClusters)
				.build();
		} catch (IOException e) {
			log.error("뉴스 데이터 조회 실패", e);
		}

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
