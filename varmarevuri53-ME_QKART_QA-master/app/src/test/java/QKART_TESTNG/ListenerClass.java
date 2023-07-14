package QKART_TESTNG;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ListenerClass implements ITestListener{
    //WebDriver driver;
    public void onTestStart(ITestResult result) {
        System.out.println("Test Case Start: " + result.getName());
        QKART_Tests.takeScreenshot(QKART_Tests.driver, "Test Case Start", result.getName());
        
    }
    public void onTestSuccess(ITestResult result) {
        System.out.println("Test Case End: " + result.getName());
        QKART_Tests.takeScreenshot(QKART_Tests.driver, "Test Case Success", result.getName());
       

    }
    public void onTestFailure(ITestResult result) {
        System.out.println("Test Case Failure: " + result.getName());
        QKART_Tests.takeScreenshot(QKART_Tests.driver, "Test Failed", result.getName());
        
    }
}

