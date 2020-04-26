package org.springframework.samples.yogogym.ui.routine;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateCorrectRoutine_TrainerUITest {

	@LocalServerPort
	private int port;

	private WebDriver driver;
	private StringBuffer verificationErrors = new StringBuffer();

	@BeforeEach
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	@Test
	public void testCreateRoutineCorrect() throws Exception {
		
		String username = "trainer1";
		String password = "trainer1999";
		
		String newRoutineName = "Routine New";
		String newRoutineDescription = "Routine Description New";
		//No poner un numero superior a 20, si no saltaría excepción
		int newRoutineRepsPerWeek = 15;
		
		as(username,password,port);
		createCorrectRoutine(newRoutineName, newRoutineDescription, newRoutineRepsPerWeek);
		
	}

	@AfterEach
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}
	
	//Derived Methods
	public void as(String username, String password, int port)
	{
		driver.get("http://localhost:" + port);
		driver.findElement(By.linkText("Login")).click();
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys(password);
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys(username);
		driver.findElement(By.xpath("//button[@type='submit']")).click();
	}
	
	public void createCorrectRoutine(String newRoutineName,String newRoutineDescription,int newRoutineRepsPerWeek)
	{
		driver.findElement(By.linkText("Trainer")).click();
	    driver.findElement(By.xpath("//div[@id='bs-example-navbar-collapse-1']/ul/li[2]/ul/li[3]/a/span[2]")).click();
	    driver.findElement(By.xpath("(//a[contains(text(),'Add Routine')])[3]")).click();
	    driver.findElement(By.id("name")).click();
	    driver.findElement(By.id("name")).clear();
	    driver.findElement(By.id("name")).sendKeys(newRoutineName);
	    driver.findElement(By.id("description")).click();
	    driver.findElement(By.id("description")).clear();
	    driver.findElement(By.id("description")).sendKeys(newRoutineDescription);
	    driver.findElement(By.id("repsPerWeek")).click();
	    driver.findElement(By.id("repsPerWeek")).clear();
	    driver.findElement(By.id("repsPerWeek")).sendKeys(String.valueOf(newRoutineRepsPerWeek));
	    driver.findElement(By.xpath("//body/div/div")).click();
	    assertEquals(newRoutineName, driver.findElement(By.id("name")).getAttribute("value"));
	    assertEquals(newRoutineDescription, driver.findElement(By.id("description")).getAttribute("value"));
	    assertEquals(String.valueOf(newRoutineRepsPerWeek), driver.findElement(By.id("repsPerWeek")).getAttribute("value"));
	    driver.findElement(By.xpath("//button[@type='submit']")).click();
	    assertEquals(newRoutineName, driver.findElement(By.linkText(newRoutineName)).getText());
	    driver.findElement(By.linkText(newRoutineName)).click();
	    assertEquals("Routine Name: " + newRoutineName, driver.findElement(By.xpath("//body/div/div/p")).getText());
	    assertEquals("Description: " + newRoutineDescription, driver.findElement(By.xpath("//body/div/div/p[2]")).getText());
	    assertEquals("Repetitions Per Week: " + newRoutineRepsPerWeek, driver.findElement(By.xpath("//p[3]")).getText());
		
	}
}
