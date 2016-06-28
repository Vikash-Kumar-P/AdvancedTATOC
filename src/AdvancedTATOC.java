import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;

public class AdvancedTATOC
{

    public static void main(String... s) throws InterruptedException, SQLException, ClassNotFoundException,MalformedURLException, IOException, JSONException
    {
        WebDriver driver = new FirefoxDriver();
        driver.get("http://10.0.1.86/tatoc/advanced/hover/menu");
        
        //Part 1
        List<WebElement> ls = driver.findElements(By.className("menuitem"));
        new Actions(driver).moveToElement(driver.findElement(By.className("menutitle"))).perform();;
        ls.get(3).click();
        
        //Part 2
        String s1 = driver.findElement(By.id("symboldisplay")).getText();
        Connection con = DriverManager.getConnection("jdbc:mysql://10.0.1.86:3306/tatoc", "tatocuser", "tatoc01" );
        Class.forName("com.mysql.jdbc.Driver");
        PreparedStatement x = con.prepareStatement("SELECT * from identity where symbol='"+s1+"'");
        ResultSet rt= x.executeQuery();
        ArrayList<String> id = new ArrayList<String>();
        while (rt.next()) 
        {
            id.add(rt.getString("id"));
        }
        String str = id.get(0);
        PreparedStatement w = con.prepareStatement("SELECT * from credentials where id="+str);
        ResultSet rs= w.executeQuery();
        ArrayList<String> Name = new ArrayList<String>();
        while (rs.next()) 
        {
            Name.add(rs.getString("Name"));
        }
        PreparedStatement p = con.prepareStatement("SELECT * from credentials where id="+str);
        ResultSet rp= p.executeQuery();
        ArrayList<String> Passkey = new ArrayList<String>();
        while (rp.next()) 
        {
            Passkey.add(rp.getString("Passkey"));
        }
        WebElement nametext=driver.findElement(By.id("name"));
        WebElement passtext=driver.findElement(By.id("passkey"));
        nametext.sendKeys(Name.get(0));
        passtext.sendKeys(Passkey.get(0));
        driver.findElement(By.id("submit")).click();
        
        //Part 3
        JavascriptExecutor js = (JavascriptExecutor)driver;
        js.executeScript("player.play()");
        Thread.sleep(26000);
        driver.findElement(By.linkText("Proceed")).click();
        
        //Part 4
        String sessid = driver.findElement(By.id("session_id")).getText();
        sessid = sessid.substring(12,sessid.length());
        String Resturl = "http://10.0.1.86/tatoc/advanced/rest/service/token/"+sessid;

        URL url = new URL(Resturl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) 
        {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) 
        {
            response.append(inputLine);
        }
        in.close();
        String res=new String(response);
        
        JSONObject obj=new JSONObject(res);
        res=(String) obj.get("token");
        
        URL url1 = new URL("http://10.0.1.86/tatoc/advanced/rest/service/register");
        HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
        

        conn1.setRequestMethod("POST");
        conn1.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        String urlParameters = "id="+sessid+"&signature="+res+"&allow_access=1";
        conn1.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(conn1.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        conn1.disconnect();
        driver.findElement(By.cssSelector(".page a")).click();
        
        //Part 5
        driver.findElement(By.linkText("Download File")).click();
        Thread.sleep(5000);
        BufferedReader br = null;
        String strng=null, sCurrentLine;
        try 
        {
            int i=0;
            File homedir = new File(System.getProperty("user.home"));
            File fileToRead = new File(homedir, "Downloads/file_handle_test.dat");
            br = new BufferedReader(new FileReader(fileToRead));
            while ((sCurrentLine = br.readLine()) != null) 
            {
                if(i==2)
                    strng = sCurrentLine;
                i++;
            }
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        } 
        strng = strng.substring(11,strng.length());
        driver.findElement(By.id("signature")).sendKeys(strng);
        driver.findElement(By.className("submit")).click();
        
        driver.close();
    }
    
}