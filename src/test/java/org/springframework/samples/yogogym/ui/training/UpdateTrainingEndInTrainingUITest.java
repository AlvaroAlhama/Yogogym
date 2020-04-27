package org.springframework.samples.yogogym.ui.training;

import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateTrainingEndInTrainingUITest {
	
  @LocalServerPort
  private int port;
  
  private WebDriver driver;
  private String baseUrl;
  private boolean acceptNextAlert = true;
  private StringBuffer verificationErrors = new StringBuffer();
  private Calendar calInit = Calendar.getInstance();
  private Calendar calEnd = Calendar.getInstance();
  private Calendar calAux = Calendar.getInstance();
  private SimpleDateFormat formatterDetails = new SimpleDateFormat("yyyy-MM-dd");
  private SimpleDateFormat formatterInput = new SimpleDateFormat("yyyy/MM/dd");

  @BeforeEach
  public void setUp() throws Exception {
    driver = new FirefoxDriver();
    baseUrl = "https://www.google.com/";
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @Test
  public void testUpdateTrainingEndInTrainingUI() throws Exception {
    as("trainer1");
    createFutureTraining();
    accessUpdateView();
    updateTrainingEndInFutureTraining();
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
  
  private void createFutureTraining() {
	  driver.findElement(By.linkText("Trainer")).click();
	  driver.findElement(By.linkText("Training Management")).click();
	  driver.findElement(By.xpath("(//a[contains(text(),'Add Training')])[4]")).click();
	  driver.findElement(By.id("name")).clear();
	  driver.findElement(By.id("name")).sendKeys("Entrenamiento nuevo");
	  calInit.add(Calendar.DAY_OF_MONTH, 8);
	  driver.findElement(By.id("initialDate")).clear();
	  driver.findElement(By.id("initialDate")).sendKeys(formatterInput.format(calInit.getTime()));
	  calEnd.add(Calendar.DAY_OF_MONTH, 15);
	  driver.findElement(By.id("endDate")).clear();
	  driver.findElement(By.id("endDate")).sendKeys(formatterInput.format(calEnd.getTime()));
	  driver.findElement(By.xpath("//button[@type='submit']")).click();
  }
  
  private void accessUpdateView() {
	  driver.findElement(By.linkText("Trainer")).click();
	  driver.findElement(By.linkText("Training Management")).click();
	  driver.findElement(By.xpath("(//a[contains(text(),'Entrenamiento1')])[2]")).click();
	  driver.findElement(By.linkText("Edit Training")).click();
  }
  
  private void updateTrainingEndInFutureTraining() {
	  driver.findElement(By.id("name")).clear();
	  driver.findElement(By.id("name")).sendKeys("Entrenamiento1");
	  calAux.add(Calendar.DAY_OF_MONTH, 8);
	  driver.findElement(By.id("endDate")).clear();
	  driver.findElement(By.id("endDate")).sendKeys(formatterInput.format(calAux.getTime()));
	  driver.findElement(By.xpath("//button[@type='submit']")).click();
  }
  
  private void errorsShown() {
	  try {
	      assertEquals("The training cannot end in a period with other training (The other training is from " + formatterDetails.format(calInit.getTime()) + " to " + formatterDetails.format(calEnd.getTime()) + ")", driver.findElement(By.xpath("//form[@id='trainingForm']/div/div[3]/div/span[2]")).getText());
	    } catch (Error e) {
	      verificationErrors.append(e.toString());
	    }
  }
}
