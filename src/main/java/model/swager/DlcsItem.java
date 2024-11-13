package model.swager;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DlcsItem{

	@JsonProperty("dlcName")
	private String dlcName;

	@JsonProperty("similarDlc")
	private SimilarDlc similarDlc;

	@JsonProperty("price")
	private int price;

	@JsonProperty("rating")
	private int rating;

	@JsonProperty("description")
	private String description;

	@JsonProperty("isDlcFree")
	private boolean isDlcFree;
}