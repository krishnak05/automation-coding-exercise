package Test.CheckerGame;

import org.openqa.selenium.*;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.Assert;
import org.testng.annotations.*;

/*
 * 
Here are a few recommendations for this exercise:

1. It would be ideal to include a "data-*" attribute for accessing various elements. 
For example, the "Restarting..." link is currently accessible only via its text, 
but this approach may not scale well for a bilingual application.

2. Some checks currently rely on the source file name, and it would be 
better to introduce an additional attribute for these checks. For instance, 
in a production application, file names or URLs may vary depending on the environment or contain version information.

3. The two-hour time limit for these two exercises was insufficient. For someone 
unfamiliar with these two games, more time is needed to understand the rules and implement the test suites. 
I spent a significant amount of time just understanding the rules and creating the test cases.

4. There are a couple of test cases that have not been implemented yet, including the double jump 
scenario and the restriction of only five moves. Due to the time limit, I chose to leave these out for now.
*/

public class CheckerGameTest {

	private WebDriver driver;
	private int numberOfMoves = 0;
	private boolean hasJumped = false;

	@BeforeClass
	public void setup() {
		driver = new SafariDriver();
		driver.manage().window().maximize();
		driver.get("https://www.gamesforthebrain.com/game/checkers/");
	}

	@AfterClass
	public void teardown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test(priority = 1, description = "Test Case 1: This test verifies that the site is up and running")
	public void testSiteIsUp() {
		String pageTitle = driver.getTitle();
		Assert.assertEquals(pageTitle, "Checkers - Games for the Brain");
	}

	@Test(priority = 2, description = "Test Case 2: This test completes at least 5 moves including taking a blue piece.")
	public void testMakeMovesAndRestartGame() {
		makeMoveAndConfirm();
	}

	@Test(priority = 3, description = "Test Case 3: This test verifies that the restart link reset the game board.")
	public void testRestartGameAndConfirm() {
		restartGameAndConfirm();
	}

	private void makeMoveAndConfirm() {
		// Test data to make at least 5 moves. Each co-ordinate refer to a initial
		// position of the orange piece.
		int[][] movePositions = { { 6, 2 }, { 4, 2 }, { 3, 1 }, { 7, 1 }, { 2, 2 } };

		for (int[] item : movePositions) {
			int[] nextMove = makeMove(item);
			if (nextMove != null) {
				numberOfMoves++;
				try {
					// Sleep for 5s to let the computer make a move.
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		Assert.assertTrue(numberOfMoves >= 5);
		Assert.assertTrue(hasJumped);
	}

	private void restartGameAndConfirm() {
		WebElement restartButton = driver.findElement(By.linkText("Restart..."));
		if (restartButton.isDisplayed()) {
			restartButton.click();
		}

		// Verify that the game is successfully restarted by checking a piece on the
		// board
		WebElement pieceElement = driver.findElement(By.name("space62"));
		String srcAttribute = pieceElement.getAttribute("src");
		Assert.assertEquals(srcAttribute, "https://www.gamesforthebrain.com/game/checkers/gray.gif");
	}

	private int[] makeMove(int[] currentPosition) {
		WebElement sourceElement = findElementByPosition(currentPosition);
		if (sourceElement == null) {
			return null;
		}

		sourceElement.click();
		int[] nextPosition = getNextMove(currentPosition);
		if (nextPosition != null) {
			numberOfMoves++;
			findElementByPosition(nextPosition).click();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return nextPosition;
	}

	private WebElement findElementByPosition(int[] position) {
		String elementName = "space" + position[0] + position[1];
		return driver.findElement(By.name(elementName));
	}

	private int[] getNextMove(int[] currentPosition) {
		int[][] moves = { { 1, 1 }, { -1, 1 } };

		for (int[] move : moves) {
			int[] newPosition = { currentPosition[0] + move[0], currentPosition[1] + move[1] };
			if (isValidPosition(newPosition)) {
				String src = findElementByPosition(newPosition).getDomAttribute("src");
				if ("me1.gif".equals(src)) {
					int[] jumpPosition = canJump(newPosition);
					if (jumpPosition != null) {
						hasJumped = true;
					}
					return jumpPosition;
				} else {
					return newPosition;
				}
			}
		}
		return null;
	}

	private boolean isValidPosition(int[] position) {
		return position[0] >= 0 && position[0] <= 7 && position[1] >= 0 && position[1] <= 7;
	}

	private int[] canJump(int[] currentPosition) {
		int[][] moves = { { -1, 1 }, { 1, 1 } };

		for (int[] move : moves) {
			int[] newPosition = { currentPosition[0] + move[0], currentPosition[1] + move[1] };
			String src = findElementByPosition(newPosition).getDomAttribute("src");
			if ("gray.gif".equals(src) || "me1.gif".equals(src)) {
				return newPosition;
			}
		}
		return null;
	}
}
