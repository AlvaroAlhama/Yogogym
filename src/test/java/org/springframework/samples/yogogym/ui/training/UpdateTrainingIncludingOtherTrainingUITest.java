package org.springframework.samples.yogogym.ui.training;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateTrainingIncludingOtherTrainingUITest {
	
  @LocalServerPort
  private int port;
  
  private WebDriver driver;
  private StringBuffer verificationErrors = new StringBuffer();
  private Calendar calInit = Calendar.getInstance();
  private Calendar calEnd = Calendar.getInstance();
  private Calendar calAux = Calendar.getInstance();
  private SimpleDateFormat formatterDetails = new SimpleDateFormat("yyyy-MM-dd");
  private SimpleDateFormat formatterInput = new SimpleDateFormat("yyyy/MM/dd");

  @BeforeEach
  public void setUp() throws Exception {
    driver = new FirefoxDriver();
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test
  public void testUpdateTrainingIncludingOtherTrainingUI() throws Exception {
    as("trainer1");
    accessUpdateView();
    updateTrainingIncludingFutureTraining();
    errorsShown();
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
	  String pass = username.replaceAll("\\d", "");
	  driver.findElement(By.id("password")).sendKeys(pass+"1999");
	  driver.findElement(By.xpath("//button[@type='submit']")).click();
	  try {
		  assertEquals(username, driver.findElement(By.xpath("//div[@id='bs-example-navbar-collapse-1']/ul[2]/li/a/strong")).getText());
	  } catch (Error e) {
		  verificationErrors.append(e.toString());
	  }
  }
  
  private void accessUpdateView() {
	  driver.findElement(By.linkText("Trainer")).click();
	  driver.findElement(By.linkText("Training Management")).click();
	  driver.findElement(By.xpath("(//a[contains(text(),'Entrenamiento1')])[3]")).click();
	  driver.findElement(By.linkText("Edit Training")).click();
  }
  
  private void updateTrainingIncludingFutureTraining() {
	  driver.findElement(By.id("name")).clear();
	  driver.findElement(By.id("name")).sendKeys("Entrenamiento1");
	  calAux.add(Calendar.DAY_OF_MONTH, 22);
	  driver.findElement(By.id("endDate")).clear();
	  driver.findElement(By.id("endDate")).sendKeys(formatterInput.format(calAux.getTime()));
	  driver.findElement(By.xpath("//button[@type='submit']")).click();
  }
  
  private void errorsShown() {
	  calInit.add(Calendar.DAY_OF_MONTH, 14);
	  calEnd.add(Calendar.DAY_OF_MONTH, 21);
	  try {
	      assertEquals("The training cannot be in a period which includes another training (The other training is from " + formatterDetails.format(calInit.getTime()) + " to " + formatterDetails.format(calEnd.getTime()) + ")", driver.findElement(By.xpath("//form[@id='trainingForm']/div/div[3]/div/span[2]")).getText());
	    } catch (Error e) {
	      verificationErrors.append(e.toString());
	    }
  }
}
