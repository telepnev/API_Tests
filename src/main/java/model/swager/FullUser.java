package model.swager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)   // игнорируем все что будет нулем
@JsonIgnoreProperties(ignoreUnknown = true)  // игнорируем низвестные поля, те заполнит те поля которые нашел, иначе игнарируем
public class FullUser {
	@JsonProperty("login")
	private String login;
	@JsonProperty("pass")
	private String pass;
	@JsonProperty("games")
	private List<GamesItem> games;
}