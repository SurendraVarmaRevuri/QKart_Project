package QKART_TESTNG;

import QKART_TESTNG.pages.Checkout;
import QKART_TESTNG.pages.Home;
import QKART_TESTNG.pages.Login;
import QKART_TESTNG.pages.Register;
import QKART_TESTNG.pages.SearchResult;

import static org.testng.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class QKART_Tests {

        static RemoteWebDriver driver;
        public static String lastGeneratedUserName;

        @BeforeSuite(alwaysRun = true)
        public static void createDriver() throws MalformedURLException {
                // Launch Browser using Zalenium
                final DesiredCapabilities capabilities = new DesiredCapabilities();
                capabilities.setBrowserName(BrowserType.CHROME);
                driver = new RemoteWebDriver(new URL("http://localhost:8082/wd/hub"), capabilities);
                System.out.println("createDriver()");
                driver.manage().window().maximize();
        }

        /*
         * Testcase01: Verify a new user can successfully register
         */
        @Test(description = "Verify that a new user can register and login to QKart", priority = 1,
                        groups = "Group_Sanity")
        @Parameters({"TC1_Username", "TC1_Password"})
        public void TestCase01(@Optional("testUser") String TC1_Username,
                        @Optional("abc@123") String TC1_Password) throws InterruptedException {
                Boolean status;
                logStatus("Start TestCase", "Test Case 1: Verify User Registration", "DONE");


                // Visit the Registration page and register a new user
                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);
                assertTrue(status, "Failed to register new user");

                // Save the last generated username
                lastGeneratedUserName = registration.lastGeneratedUsername;

                // Visit the login page and login with the previuosly registered user
                Login login = new Login(driver);
                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");
                logStatus("Test Step", "User Perform Login: ", status ? "PASS" : "FAIL");
                assertTrue(status, "Failed to login with registered user");

                // Visit the home page and log out the logged in user
                Home home = new Home(driver);
                status = home.PerformLogout();

                logStatus("End TestCase", "Test Case 1: Verify user Registration : ",
                                status ? "PASS" : "FAIL");

        }

        @Test(description = "Verify that an existing user is not allowed to re-register on QKart",
                        priority = 2, groups = "Group_Sanity")
        @Parameters({"TC2_Username", "TC2_Password"})
        public static void TestCase02(@Optional("testUser") String TC2_Username,
                        @Optional("abc@123") String TC2_Password) throws InterruptedException {
                Boolean status;
                logStatus("Start Testcase",
                                "Test Case 2: Verify User Registration with an existing username ",
                                "DONE");

                // Visit the Registration page and register a new user
                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser(TC2_Username, TC2_Password, true);
                logStatus("Test Step", "User Registration : ", status ? "PASS" : "FAIL");
                // Save the last generated username
                lastGeneratedUserName = registration.lastGeneratedUsername;

                // Visit the Registration page and try to register using the previously
                // registered user's credentials
                registration.navigateToRegisterPage();
                status = registration.registerUser(lastGeneratedUserName, "abc@123", false);

                assertFalse(status, "Re-registration is happening with exsting creds");
                String actualUrl = driver.getCurrentUrl();
                String expectedUrl = "https://crio-qkart-frontend-qa.vercel.app/login";
                assertNotEquals(actualUrl, expectedUrl,
                                "Re-registration is happening with exsting creds");

                // If status is true, then registration succeeded, else registration has
                // failed. In this case registration failure means Success
                logStatus("End TestCase", "Test Case 2: Verify user Registration : ",
                                status ? "FAIL" : "PASS");

        }


        @Test(description = "Verify the functionality of search text box", priority = 3,
                        groups = "Group_Sanity")
        @Parameters({"TC3_ProductNameToSearchFor"})
        public static void TestCase03(String TC3_ProductNameToSearchFor)
                        throws InterruptedException {
                logStatus("TestCase 3", "Start test case : Verify functionality of search box ",
                                "DONE");
                boolean status;

                // Visit the home page
                Home homePage = new Home(driver);
                homePage.navigateToHome();

                // Search for the "yonex" product
                status = homePage.searchForProduct(TC3_ProductNameToSearchFor);

                assertTrue(status, "Test Case Failure. Unable to search for given product");

                // Fetch the search results
                List<WebElement> searchResults = homePage.getSearchResults();


                assertTrue(searchResults.size() != 0,
                                "Test Case Failure. There were no results for the given search string");

                for (WebElement webElement : searchResults) {
                        // Create a SearchResult object from the parent element
                        SearchResult resultelement = new SearchResult(webElement);

                        // Verify that all results contain the searched text
                        String elementText = resultelement.getTitleofResult();

                        assertTrue(elementText.toUpperCase().contains(TC3_ProductNameToSearchFor),
                                        "Test Case Failure. Test Results contains un-expected values");
                }

                logStatus("Step Success", "Successfully validated the search results ", "PASS");

                // Search for product
                status = homePage.searchForProduct("Gesundheit");

                assertFalse(status, "Test Case Failure. Invalid keyword returned results");

                // Verify no search results are found
                searchResults = homePage.getSearchResults();
                assertTrue(searchResults.size() == 0 && homePage.isNoResultFound(),
                                "Test Case Failure. Expected: no results , actual: Results were available");


        }



        @Test(description = "Verify the existence of size chart for certain items and validate contents of size chart",
                        priority = 4, groups="Group_Regression")
        @Parameters("TC4_ProductNameToSearchFor")
        public static void TestCase04(@Optional("Roadster") String TC4_ProductNameToSearchFor)
                        throws InterruptedException {
                logStatus("TestCase 4", "Start test case : Verify the presence of size Chart",
                                "DONE");
                boolean status = false;

                // Visit home page
                Home homePage = new Home(driver);
                homePage.navigateToHome();

                // Search for product and get card content element of search results
                status = homePage.searchForProduct(TC4_ProductNameToSearchFor);
                List<WebElement> searchResults = homePage.getSearchResults();

                // Create expected values
                List<String> expectedTableHeaders =
                                Arrays.asList("Size", "UK/INDIA", "EU", "HEEL TO TOE");
                List<List<String>> expectedTableBody =
                                Arrays.asList(Arrays.asList("6", "6", "40", "9.8"),
                                                Arrays.asList("7", "7", "41", "10.2"),
                                                Arrays.asList("8", "8", "42", "10.6"),
                                                Arrays.asList("9", "9", "43", "11"),
                                                Arrays.asList("10", "10", "44", "11.5"),
                                                Arrays.asList("11", "11", "45", "12.2"),
                                                Arrays.asList("12", "12", "46", "12.6"));

                // Verify size chart presence and content matching for each search result
                for (WebElement webElement : searchResults) {
                        SearchResult result = new SearchResult(webElement);

                        // Verify if the size chart exists for the search result
                        if (result.verifySizeChartExists()) {
                                logStatus("Step Success",
                                                "Successfully validated presence of Size Chart Link",
                                                "PASS");
                                // Verify if size dropdown exists
                                status = result.verifyExistenceofSizeDropdown(driver);
                                logStatus("Step Success", "Validated presence of drop down",
                                                status ? "PASS" : "FAIL");


                                boolean resultOpenSizeChart = result.openSizechart();
                                assertTrue(resultOpenSizeChart,
                                                "Test Case Fail. Failure to open Size Chart");

                                if (resultOpenSizeChart) {
                                        boolean validateSizeChartContents =
                                                        result.validateSizeChartContents(
                                                                        expectedTableHeaders,
                                                                        expectedTableBody, driver);
                                        assertTrue(validateSizeChartContents,
                                                        "Failure while validating contents of Size Chart Link");
                                        logStatus("Step Success",
                                                        "Successfully validated contents of Size Chart Link",
                                                        "PASS");

                                        boolean closeSizeChart = result.closeSizeChart(driver);
                                        assertTrue(closeSizeChart,
                                                        "Failed to close the size chart modal");
                                } else {
                                        Assert.fail("Test Case Fail. Size Chart Link does not exist");
                                }
                        }
                        logStatus("TestCase 4", "End Test Case: Validated Size Chart Details",
                                        status ? "PASS" : "FAIL");
                }
        }


        @Test(description = "Verify that a new user can add multiple products in to the cart and Checkout",
                        priority = 5, groups = "Group_Sanity")
        @Parameters({"TC5_ProductNameToSearchFor", "TC5_ProductNameToSearchFor2",
                        "TC5_AddressDetails"})
        public static void TestCase05(String TC5_ProductNameToSearchFor,
                        String TC5_ProductNameToSearchFor2, String TC5_AddressDetails)
                        throws InterruptedException {
                Boolean status;
                logStatus("Start TestCase", "Test Case 5: Verify Happy Flow of buying products",
                                "DONE");

                // Go to the Register page
                Register registration = new Register(driver);
                registration.navigateToRegisterPage();

                // Register a new user
                status = registration.registerUser("testUser", "abc@123", true);

                assertTrue(status, "TestCase 5 Test Case Failure. Happy Flow Test Failed");

                // Save the username of the newly registered user
                lastGeneratedUserName = registration.lastGeneratedUsername;

                // Go to the login page
                Login login = new Login(driver);
                login.navigateToLoginPage();

                // Login with the newly registered user's credentials
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");

                assertTrue(status, "User Perform Login Failed");
                logStatus("End TestCase", "Test Case 5: Happy Flow Test Failed : ",
                                status ? "PASS" : "FAIL");

                // Go to the home page
                Home homePage = new Home(driver);
                homePage.navigateToHome();

                // Find required products by searching and add them to the user's cart
                status = homePage.searchForProduct(TC5_ProductNameToSearchFor);
                homePage.addProductToCart(TC5_ProductNameToSearchFor);
                status = homePage.searchForProduct(TC5_ProductNameToSearchFor);
                homePage.addProductToCart(TC5_ProductNameToSearchFor2);

                // Click on the checkout button
                homePage.clickCheckout();

                // Add a new address on the Checkout page and select it
                Checkout checkoutPage = new Checkout(driver);
                checkoutPage.addNewAddress(TC5_AddressDetails);
                checkoutPage.selectAddress(TC5_AddressDetails);

                // Place the order
                checkoutPage.placeOrder();

                WebDriverWait wait = new WebDriverWait(driver, 30);
                wait.until(ExpectedConditions
                                .urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));

                // Check if placing order redirected to the Thansk page
                status = driver.getCurrentUrl().endsWith("/thanks");

                // Go to the home page
                homePage.navigateToHome();

                // Log out the user
                homePage.PerformLogout();

                logStatus("End TestCase", "Test Case 5: Happy Flow Test Completed : ",
                                status ? "PASS" : "FAIL");

        }


        @Test(description = "Verify that the contents of the cart can be edited", priority = 6, groups="Group_Regression")
        @Parameters({"TC6_ProductNameToSearch1", "TC6_ProductNameToSearch2"})
        public void TestCase06(String TC6_ProductNameToSearch1, String TC6_ProductNameToSearch2)
                        throws InterruptedException {
                Boolean status;
                Home homePage = new Home(driver);
                Register registration = new Register(driver);
                Login login = new Login(driver);

                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);
                assertTrue(status, "User Perform Register Failed");
                lastGeneratedUserName = registration.lastGeneratedUsername;

                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");
                assertTrue(status, "User Perform Login Failed");

                homePage.navigateToHome();
                status = homePage.searchForProduct(TC6_ProductNameToSearch1);
                homePage.addProductToCart(TC6_ProductNameToSearch1);

                status = homePage.searchForProduct(TC6_ProductNameToSearch2);
                homePage.addProductToCart(TC6_ProductNameToSearch2);

                // update watch quantity to 2
                homePage.changeProductQuantityinCart(TC6_ProductNameToSearch1, 2);

                // update table lamp quantity to 0
                homePage.changeProductQuantityinCart(TC6_ProductNameToSearch2, 0);

                // update watch quantity again to 1
                homePage.changeProductQuantityinCart(TC6_ProductNameToSearch1, 1);

                homePage.clickCheckout();
                Checkout checkoutPage = new Checkout(driver);
                checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
                checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

                checkoutPage.placeOrder();

                try {
                        WebDriverWait wait = new WebDriverWait(driver, 30);
                        wait.until(ExpectedConditions.urlToBe(
                                        "https://crio-qkart-frontend-qa.vercel.app/thanks"));
                } catch (TimeoutException e) {
                        System.out.println("Error while placing order in: " + e.getMessage());

                }

                status = driver.getCurrentUrl().endsWith("/thanks");
                assertTrue(status, "Test Case 6: Verify that cart can be edited");
                homePage.navigateToHome();
                homePage.PerformLogout();
        }


        @Test(description = "Verify that the contents made to the cart are saved againts the user's login details",
                        priority = 7, groups="Group_Regression")
        @Parameters({"TestCase07_produ", "TestCase07_produ2"})
        public static void TestCase07(String product, String product2) throws InterruptedException {
                Boolean status = false;
                List<String> expectedResult = Arrays.asList("Stylecon 9 Seater RHS Sofa Set ",
                                "Xtend Smart Watch");

                logStatus("Start TestCase",
                                "Test Case 7: Verify that cart contents are persisted after logout",
                                "DONE");

                Register registration = new Register(driver);
                Login login = new Login(driver);
                Home homePage = new Home(driver);

                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);

                assertTrue(status, "Step Failure User Perform Login Failed");


                lastGeneratedUserName = registration.lastGeneratedUsername;

                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");
                assertTrue(status, "Step Failure, User Perform Login Failed");

                homePage.navigateToHome();
                status = homePage.searchForProduct(product);
                homePage.addProductToCart("Stylecon 9 Seater RHS Sofa Set ");

                status = homePage.searchForProduct(product2);
                homePage.addProductToCart("Xtend Smart Watch");

                homePage.PerformLogout();

                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");

                status = homePage.verifyCartContents(expectedResult);

                logStatus("End TestCase",
                                "Test Case 7: Verify that cart contents are persisted after logout: ",
                                status ? "PASS" : "FAIL");

                homePage.PerformLogout();
        }


        @Test(description = "Verify that insufficient balance error is thrown when the wallet balance is not enough",
                        priority = 8,groups = "Group_Sanity")
        @Parameters({"TC8_ProductName", "TC8_Qty"})
        public static void TestCase08(String TC8_ProductName, String TC8_Qty)
                        throws InterruptedException {
                Boolean status;
                logStatus("Start TestCase",
                                "Test Case 8: Verify that insufficient balance error is thrown when the wallet balance is not enough",
                                "DONE");

                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);

                assertTrue(status, "Step Failure, User Perform Registration Failed ");
                lastGeneratedUserName = registration.lastGeneratedUsername;

                Login login = new Login(driver);
                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");

                assertTrue(status, "User Perform Login Failed");

                Home homePage = new Home(driver);
                homePage.navigateToHome();
                status = homePage.searchForProduct(TC8_ProductName);
                homePage.addProductToCart("Stylecon 9 Seater RHS Sofa Set ");

                homePage.changeProductQuantityinCart("Stylecon 9 Seater RHS Sofa Set ",
                                Integer.parseInt(TC8_Qty));

                homePage.clickCheckout();

                Checkout checkoutPage = new Checkout(driver);
                checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
                checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

                checkoutPage.placeOrder();
                Thread.sleep(3000);

                status = checkoutPage.verifyInsufficientBalanceMessage();

                logStatus("End TestCase",
                                "Test Case 8: Verify that insufficient balance error is thrown when the wallet balance is not enough: ",
                                status ? "PASS" : "FAIL");

        }

        @Test(dependsOnMethods = {"TestCase10"},
                        description = "Verify that a product added to a cart is available when a new tab is added",
                        priority = 10, groups="Group_Regression")

        public static void TestCase09() throws InterruptedException {
                Boolean status = false;

                logStatus("Start TestCase",
                                "Test Case 9: Verify that product added to cart is available when a new tab is opened",
                                "DONE");

                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);

                assertTrue(status,
                                "Test Case Failure. Verify that product added to cart is available when a new tab is opened");
                lastGeneratedUserName = registration.lastGeneratedUsername;

                Login login = new Login(driver);
                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");

                assertTrue(status, "User Perform Login Failed");
                logStatus("End TestCase",
                                "Test Case 9:   Verify that product added to cart is available when a new tab is opened",
                                status ? "PASS" : "FAIL");

                Home homePage = new Home(driver);
                homePage.navigateToHome();

                status = homePage.searchForProduct("YONEX");
                homePage.addProductToCart("YONEX Smash Badminton Racquet");

                String currentURL = driver.getCurrentUrl();

                driver.findElement(By.linkText("Privacy policy")).click();
                Set<String> handles = driver.getWindowHandles();
                driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);

                driver.get(currentURL);
                Thread.sleep(2000);

                List<String> expectedResult = Arrays.asList("YONEX Smash Badminton Racquet");
                status = homePage.verifyCartContents(expectedResult);

                driver.close();

                driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);

                logStatus("End TestCase",
                                "Test Case 9: Verify that product added to cart is available when a new tab is opened",
                                status ? "PASS" : "FAIL");
                // takeScreenshot(driver, "EndTestCase", "TestCase09");
        }

        @Test(description = "Verify that privacy policy and about us links are working fine",
                        priority = 9, groups="Group_Regression")
        public static void TestCase10() throws InterruptedException {
                Boolean status = false;

                logStatus("Start TestCase",
                                "Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly ",
                                "DONE");

                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                SoftAssert sa = new SoftAssert();
                status = registration.registerUser("testUser", "abc@123", true);
                sa.assertTrue(status,
                                "Test Case Failure.  Verify that the Privacy Policy, About Us are displayed correctly");
                lastGeneratedUserName = registration.lastGeneratedUsername;

                Login login = new Login(driver);
                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");

                sa.assertTrue(status, "User Perform Login Failed");
                logStatus("End TestCase",
                                "Test Case 10:    Verify that the Privacy Policy, About Us are displayed correctly ",
                                status ? "PASS" : "FAIL");

                Home homePage = new Home(driver);
                homePage.navigateToHome();

                String basePageURL = driver.getCurrentUrl();

                driver.findElement(By.linkText("Privacy policy")).click();
                status = driver.getCurrentUrl().equals(basePageURL);

                sa.assertTrue(status,
                                "Step Failure, Verifying parent page url didn't change on privacy policy link click failed");
                logStatus("End TestCase",
                                "Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly ",
                                status ? "PASS" : "FAIL");

                Set<String> handles = driver.getWindowHandles();
                driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);
                WebElement PrivacyPolicyHeading =
                                driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/h2"));
                status = PrivacyPolicyHeading.getText().equals("Privacy Policy");

                sa.assertTrue(status,
                                "Step Failure, Verifying new tab opened has Privacy Policy page heading failed");
                logStatus("End TestCase",
                                "Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly ",
                                status ? "PASS" : "FAIL");

                driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);
                driver.findElement(By.linkText("Terms of Service")).click();

                handles = driver.getWindowHandles();
                driver.switchTo().window(handles.toArray(new String[handles.size()])[2]);
                WebElement TOSHeading =
                                driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/h2"));
                status = TOSHeading.getText().equals("Terms of Service");

                sa.assertTrue(status,
                                "Step Failure, Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly");
                logStatus("End TestCase",
                                "Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly ",
                                status ? "PASS" : "FAIL");

                driver.close();
                driver.switchTo().window(handles.toArray(new String[handles.size()])[1]).close();
                driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);

                logStatus("End TestCase",
                                "Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly ",
                                "PASS");
                sa.assertAll();

        }

        @Test(description = "Verify that the contact us dialog works fine", priority = 11, groups="Group_Regression")
        @Parameters({"TC11_ContactusUserName", "TC11_ContactUsEmail", "TC11_QueryContent"})
        public static void TestCase11(String TC11_ContactusUserName, String TC11_ContactUsEmail,
                        String TC11_QueryContent) throws InterruptedException {
                logStatus("Start TestCase",
                                "Test Case 11: Verify that contact us option is working correctly ",
                                "DONE");


                Home homePage = new Home(driver);
                homePage.navigateToHome();

                driver.findElement(By.xpath("//*[text()='Contact us']")).click();

                WebElement name = driver.findElement(By.xpath("//input[@placeholder='Name']"));
                name.sendKeys(TC11_ContactusUserName);
                WebElement email = driver.findElement(By.xpath("//input[@placeholder='Email']"));
                email.sendKeys(TC11_ContactUsEmail);
                WebElement message =
                                driver.findElement(By.xpath("//input[@placeholder='Message']"));
                message.sendKeys(TC11_QueryContent);

                WebElement contactUs = driver.findElement(By.xpath(
                                "/html/body/div[2]/div[3]/div/section/div/div/div/form/div/div/div[4]/div/button"));

                contactUs.click();

                WebDriverWait wait = new WebDriverWait(driver, 30);
                wait.until(ExpectedConditions.invisibilityOf(contactUs));

                logStatus("End TestCase",
                                "Test Case 11: Verify that contact us option is working correctly ",
                                "PASS");

        }

        @Test(description = "Ensure that the Advertisement Links on the QKART page are clickable",
                        priority = 12,groups = "Group_Sanity")
        @Parameters({"TC12_ProductNameToSearch", "TC12_AddresstoAdd"})
        public static void TestCase12(String TC12_ProductNameToSearch, String TC12_AddresstoAdd)
                        throws InterruptedException {
                Boolean status = false;
                logStatus("Start TestCase",
                                "Test Case 12: Ensure that the links on the QKART advertisement are clickable",
                                "DONE");

                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);

                assertTrue(status,
                                "Test Case Failure. Ensure that the links on the QKART advertisement are clickable");
                lastGeneratedUserName = registration.lastGeneratedUsername;

                Login login = new Login(driver);
                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");

                assertTrue(status, "Step Failure, User Perform Login Failed");
                logStatus("End TestCase",
                                "Test Case 12:  Ensure that the links on the QKART advertisement are clickable",
                                status ? "PASS" : "FAIL");

                Home homePage = new Home(driver);
                homePage.navigateToHome();

                status = homePage.searchForProduct(TC12_ProductNameToSearch);
                homePage.addProductToCart(TC12_ProductNameToSearch);
                homePage.changeProductQuantityinCart("YONEX Smash Badminton Racquet", 1);
                homePage.clickCheckout();

                Checkout checkoutPage = new Checkout(driver);
                checkoutPage.addNewAddress(TC12_AddresstoAdd);
                checkoutPage.selectAddress(TC12_AddresstoAdd);
                checkoutPage.placeOrder();
                Thread.sleep(3000);

                String currentURL = driver.getCurrentUrl();

                List<WebElement> Advertisements = driver.findElements(By.xpath("//iframe"));

                status = Advertisements.size() == 3;
                logStatus("Step ", "Verify that 3 Advertisements are available",
                                status ? "PASS" : "FAIL");

                WebElement Advertisement1 = driver.findElement(
                                By.xpath("//*[@id=\"root\"]/div/div[2]/div/iframe[1]"));
                driver.switchTo().frame(Advertisement1);
                driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
                driver.switchTo().parentFrame();

                status = !driver.getCurrentUrl().equals(currentURL);
                logStatus("Step ", "Verify that Advertisement 1 is clickable ",
                                status ? "PASS" : "FAIL");

                driver.get(currentURL);
                Thread.sleep(3000);

                WebElement Advertisement2 = driver.findElement(
                                By.xpath("//*[@id=\"root\"]/div/div[2]/div/iframe[2]"));
                driver.switchTo().frame(Advertisement2);
                driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
                driver.switchTo().parentFrame();

                status = !driver.getCurrentUrl().equals(currentURL);
                logStatus("Step ", "Verify that Advertisement 2 is clickable ",
                                status ? "PASS" : "FAIL");

                logStatus("End TestCase",
                                "Test Case 12:  Ensure that the links on the QKART advertisement are clickable",
                                status ? "PASS" : "FAIL");

        }



        @AfterSuite
        public static void quitDriver() {
                System.out.println("quit()");
                driver.quit();
        }

        public static void logStatus(String type, String message, String status) {

                System.out.println(String.format("%s |  %s  |  %s | %s",
                                String.valueOf(java.time.LocalDateTime.now()), type, message,
                                status));
        }

        public static void takeScreenshot(WebDriver driver, String screenshotType,
                        String description) {
                try {
                        File theDir = new File("/screenshots");
                        if (!theDir.exists()) {
                                theDir.mkdirs();
                        }
                        String timestamp = String.valueOf(java.time.LocalDateTime.now());
                        String fileName = String.format("screenshot_%s_%s_%s.png", timestamp,
                                        screenshotType, description);
                        TakesScreenshot scrShot = ((TakesScreenshot) driver);
                        File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
                        File DestFile = new File("screenshots/" + fileName);
                        FileUtils.copyFile(SrcFile, DestFile);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}

