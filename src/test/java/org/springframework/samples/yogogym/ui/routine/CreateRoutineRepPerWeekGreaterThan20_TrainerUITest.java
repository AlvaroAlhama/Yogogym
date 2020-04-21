package org.springframework.samples.yogogym.ui.routine;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class CreateRoutineRepPerWeekGreaterThan20_TrainerUITest {

	private WebDriver driver;
	private String baseUrl;
	private boolean acceptNextAlert = true;
	private StringBuffer verificationErrors = new StringBuffer();

	@BeforeEach
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		baseUrl = "https://www.google.com/";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testCreateRoutineCorrect() throws Exception {
		driver.get("http://localhost:8080/trainer/trainer1/routines");
		driver.findElement(By.xpath("(//a[contains(text(),'Add Routine')])[3]")).click();
		driver.findElement(By.id("name")).click();
		driver.findElement(By.id("name")).clear();
		driver.findElement(By.id("name")).sendKeys("Routine 10");
		driver.findElement(By.id("description")).click();
		driver.findElement(By.id("description")).clear();
		driver.findElement(By.id("description")).sendKeys("Routine 10 description");
		driver.findElement(By.id("repsPerWeek")).click();
		driver.findElement(By.id("repsPerWeek")).clear();
		driver.findElement(By.id("repsPerWeek")).sendKeys("5");
		driver.findElement(By.xpath("//div/div/div/div/div")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertEquals("Routine 10", driver.findElement(By.linkText("Routine 10")).getText());
		driver.findElement(By.linkText("Routine 10")).click();
		assertEquals("Routine Name: Routine 10", driver.findElement(By.xpath("//body/div/div/p")).getText());
		assertEquals("Description: Routine 10 description",
				driver.findElement(By.xpath("//body/div/div/p[2]")).getText());
		assertEquals("Repetitions Per Week: 5", driver.findElement(By.xpath("//p[3]")).getText());
	}

	@AfterEach
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}

	private boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	private boolean isAlertPresent() {
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}

	private String closeAlertAndGetItsText() {
		try {
			Alert alert = driver.switchTo().alert();
			String alertText = alert.getText();
			if (acceptNextAlert) {
				alert.accept();
			} else {
				alert.dismiss();
			}
			return alertText;
		} finally {
			acceptNextAlert = true;
		}
	}
}
