package org.springframework.samples.yogogym.ui.client;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DashBoardClientWithExercisesUITest {
	@LocalServerPort
	private int port;

	private WebDriver driver;
	private StringBuffer verificationErrors = new StringBuffer();

	@BeforeEach
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testDashBoardClientUI() throws Exception {
		as("client1");
		initDashboard();
	}

	@AfterEach
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}

	private void as(String username) {
		driver.get("http://localhost:" + port);
		driver.findElement(By.linkText("Login")).click();
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("client1999");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys(username);
		driver.findElement(By.xpath("//button[@type='submit']")).click();
	}

	private void initDashboard() {
		driver.findElement(By.linkText("Client")).click();
		driver.findElement(By.xpath("//div[@id='bs-example-navbar-collapse-1']/ul/li[2]/ul/li[5]/a/span[2]")).click();
		driver.findElement(By.id("canvasBodyPartsMonth")).click();
		driver.findElement(By.id("canvasBodyPartsMonth")).click();
		driver.findElement(By.id("canvasRepititionTypeMonth")).click();
		driver.findElement(By.id("canvasRepititionTypeMonth")).click();
		driver.findElement(By.id("canvasBodyPartsAll")).click();
		driver.findElement(By.id("canvasBodyPartsAll")).click();
		driver.findElement(By.id("canvasRepititionTypeAll")).click();
		driver.findElement(By.id("canvasRepititionTypeAll")).click();
	}
}
