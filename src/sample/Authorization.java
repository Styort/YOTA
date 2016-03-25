package sample;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import javafx.scene.control.Alert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Created by Виктор on 24.02.2016.
 */
public class Authorization {
    String loginPref, passwordPref;

    boolean info = true;
    boolean loop = true;

    public WebClient webClient = new WebClient(BrowserVersion.CHROME);

    public void LoadPreferences() throws ParserConfigurationException, IOException, SAXException {
        File f = new File("preferences.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(f);
        NodeList nodeList = document.getElementsByTagName("entry");
        loginPref = nodeList.item(0).getAttributes().getNamedItem("value").getNodeValue();
        passwordPref = nodeList.item(1).getAttributes().getNamedItem("value").getNodeValue();
    }

    public void WebBrowserSettings() { //настраиваем клиент
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(false);
    }

    public void LoopAuth() { //Авторизация каждые 15мин.
        Thread timeredLogin = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000 * 60 * 15);
                        LoginInWebsite();
                    } catch (InterruptedException | SAXException | ParserConfigurationException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timeredLogin.start();
    }

    public void LoginInWebsite() throws IOException, SAXException, ParserConfigurationException {
        if (info) {
            LoadPreferences();
            WebBrowserSettings();
        }
        try {
            HtmlPage page1 = webClient.getPage("https://partner.yota.ru/rd/login");
            HtmlForm form = page1.getFirstByXPath("//form[@action='/rd/login_check']");

            //Заполняем форму
            HtmlTextInput login = form.getInputByName("_username");
            HtmlPasswordInput password = form.getInputByName("_password");
            login.setValueAttribute(loginPref);
            password.setValueAttribute(passwordPref);
            HtmlSubmitInput button = page1.getFirstByXPath("//input[@id='send_id' and @type='submit']");
            button.click();

            page1 = webClient.getPage("https://partner.yota.ru/rd/vox/order/search");
            //Проверяем прошла ли авторизация.
            if (page1.getUrl().toString().contains("https://partner.yota.ru/rd/vox/order/search")) {
                if (info) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Информация");
                    alert.setHeaderText(null);
                    alert.setContentText("Авторизация прошла успешно!");
                    alert.showAndWait();
                    info = false;
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Произошла ошибка при авторизации!");
                alert.showAndWait();
            }

        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Проверьте интернет соединение");
            alert.setContentText("Похоже у вас оборвалось соединение с интернетом...");
            alert.showAndWait();
        }
        if (loop) {
            LoopAuth();
            loop = false;
        }
    }
}
