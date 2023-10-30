package DeckOfCardAPIAutomation.Krishna;

import io.restassured.response.Response;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/*
 * 
Here are a few recommendations for this exercise:

1. The two-hour time limit for these two exercises was insufficient. For someone 
unfamiliar with these two games, more time is needed to understand the rules and implement the test suites. 
I spent a significant amount of time just understanding the rules and creating the test cases.


*/

public class CardsGameTest {

	private String deckId;
	private Map<Integer, Integer> playerScores;

	@BeforeClass
	public void setup() {
		RestAssured.baseURI = "https://deckofcardsapi.com";

		// Step 1: Get a new deck
		Response newDeckResponse = given().when().get("/api/deck/new/");
		newDeckResponse.then().statusCode(200);

		JsonPath jsonPath = newDeckResponse.jsonPath();
		deckId = jsonPath.get("deck_id");

		// Step 2: Confirm the deck is shuffled
		Assert.assertFalse(jsonPath.getBoolean("shuffled"));

		playerScores = new HashMap<>();
	}

	@Test(description = "Shuffle a fresh deck of cards, draw three cards for each of two players, and determine the winner in a game of Blackjack.")
	public void testCardGame() {
		// Step 3: Shuffle the deck
		shuffleDeck();

		// Step 4: Deal three cards to each of two players
		dealCards(2, 3);

		// Step 5: Check for blackjack and find the winner
		int winner = findWinner();

		if (winner == 0) {
			System.out.println("First player wins!");
		} else if (winner == 1) {
			System.out.println("Second player wins!");
		} else {
			System.out.println("It's a tie!");
		}
	}

	private void shuffleDeck() {
		Response shuffleResponse = given().when().get("/api/deck/" + deckId + "/shuffle/?deck_count=6");
		shuffleResponse.then().statusCode(200);

		JsonPath shuffleJsonPath = shuffleResponse.jsonPath();
		Assert.assertTrue(shuffleJsonPath.getBoolean("shuffled"));
	}

	private void dealCards(int numPlayers, int cardsPerPlayer) {
		for (int i = 0; i < numPlayers; i++) {
			Response drawResponse = given().when().get("/api/deck/" + deckId + "/draw/?count=" + cardsPerPlayer);
			drawResponse.then().statusCode(200);

			JsonPath drawJsonPath = drawResponse.jsonPath();
			List<String> cardValues = drawJsonPath.getList("cards.value");
			int playerScore = calculateScore(cardValues);

			playerScores.put(i, playerScore);
		}
	}

	private int calculateScore(List<String> cardValues) {
		int score = 0;
		for (String value : cardValues) {
			score += getCardValue(value, score);
		}
		return score;
	}

	private int getCardValue(String card, int currentScore) {
		switch (card) {
		case "2":
		case "3":
		case "4":
		case "5":
		case "6":
		case "7":
		case "8":
		case "9":
		case "10":
			return Integer.parseInt(card);
		case "J":
		case "Q":
		case "K":
			return 10;
		case "A":
			return currentScore + 11 > 21 ? 1 : 11;
		default:
			return 0; // Invalid card
		}
	}

	private int findWinner() {
		// Step 6: Check whether either has blackjack
		// Compare playerScores to determine the winner

		if (playerScores.get(0) == 21 && playerScores.get(1) == 21) {
			// Both players have blackjack
			return -1; // Tie
		} else if (playerScores.get(0) == 21) {
			// First player has blackjack
			return 0;
		} else if (playerScores.get(1) == 21) {
			// Second player has blackjack
			return 1;
		} else {
			// Compare the scores to determine the winner
			return (playerScores.get(0) > playerScores.get(1)) ? 0 : 1;
		}
	}
}
