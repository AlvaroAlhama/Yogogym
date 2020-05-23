package org.springframework.samples.yogogym.ui.guilds;

import static org.junit.Assert.assertEquals;
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
public class CreateGuildBadURLUITest {

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
	public void testCreateGuildBadUrl() throws Exception {

		as("client4");
		createBadUrl();
		showErrors();
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
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys(username);
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("client1999");
		driver.findElement(By.xpath("//button[@type='submit']")).click();

	}

	private void createBadUrl() {

		driver.findElement(By.linkText("Client")).click();
		driver.findElement(By.xpath("//div[@id='bs-example-navbar-collapse-1']/ul/li[2]/ul/li[6]/a/span[2]")).click();
		driver.findElement(By.linkText("Create a Guild")).click();
		driver.findElement(By.id("logo")).click();
		driver.findElement(By.id("logo")).clear();
		driver.findElement(By.id("logo")).sendKeys("badurl");
		driver.findElement(By.id("name")).clear();
		driver.findElement(By.id("name")).sendKeys("Example");
		driver.findElement(By.id("description")).clear();
		driver.findElement(By.id("description")).sendKeys("Example");
		driver.findElement(By.xpath("//body/div")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
	}

	private void showErrors() {

		try {
			assertEquals("The link must start with https://",
					driver.findElement(By.xpath("//form[@id='guild']/div/div[2]/div/span[2]")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}

	}
}
