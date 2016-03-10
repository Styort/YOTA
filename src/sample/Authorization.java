package sample;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import javafx.scene.control.Alert;

import java.io.IOException;

/**
 * Created by Виктор on 24.02.2016.
 */
public class Authorization {


    boolean info = true;
    boolean loop=true;

    WebClient webClient = new WebClient(BrowserVersion.CHROME);

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
                while(true) {
                    try {
                        Thread.sleep(1000*60*15);
                        LoginInWebsite();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timeredLogin.start();
    }
    public void LoginInWebsite(){
        if(info){
            WebBrowserSettings();
        }
        try {
            HtmlPage page1 = webClient.getPage("https://partner.yota.ru/rd/login");
            HtmlForm form = page1.getFirstByXPath("//form[@action='/rd/login_check']");

            //Заполняем форму
            HtmlTextInput login = form.getInputByName("_username");
            HtmlPasswordInput password = form.getInputByName("_password");
            login.setValueAttribute("login");
            password.setValueAttribute("password");
            HtmlSubmitInput button = page1.getFirstByXPath("//input[@id='send_id' and @type='submit']");
            button.click();

            page1 = webClient.getPage("https://partner.yota.ru/rd/vox/order/search");
            //Проверяем прошла ли авторизация.
            if (page1.getUrl().toString().contains("https://partner.yota.ru/rd/vox/order/search")){
                if (info) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Информация");
                    alert.setHeaderText(null);
                    alert.setContentText("Авторизация прошла успешно!");
                    alert.showAndWait();
                    info=false;
                }
            }
            else {
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
        if(loop){
            LoopAuth();
            loop=false;
        }
    }
}
