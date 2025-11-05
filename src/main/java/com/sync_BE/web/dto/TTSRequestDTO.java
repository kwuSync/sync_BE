package com.sync_BE.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TTSRequestDTO {

	@Schema(description = "(예: 'Achernar', 'ko-KR-Wavenet-A')." + "지정하지 않을 시 기본 설정을 따릅니다.")
	private String voiceName;

	@Schema(description = "-20.0 ~ 20.0")
	private Double pitch;

	@Schema(description = "0.25 ~ 4.0")
	private Double speakingRate;
}
