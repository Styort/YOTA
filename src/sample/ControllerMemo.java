package sample;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.printing.PDFPageable;

import java.awt.*;
import java.awt.print.*;
import java.io.*;

public class ControllerMemo {
    @FXML
    TextField url, iccidMemo,iccidSt, address, data, fioMemo, fioSt, phoneMemo, phoneSt, comment, dataBdayAndLocation;
    @FXML
    CheckBox fillCommentCheckBox;
    @FXML
    Tab memoTab,declarationTab;
    @FXML
    TabPane tabPane;
    WebClient webClient = new WebClient(BrowserVersion.CHROME);
    HtmlTextInput Iccid, Address, Data, TimeBegin, TimeEnd, ClientName, ClientPhoneNumber,DateBdayAndLocation;
    HtmlTextArea Comment;
    String defaultURL = "https://partner.yota.ru/rd/vox/order/edit/";
    Thread timeredLogin;
    boolean info = true;
    boolean check = false;
    boolean clearAllOrNotAll = false;
    boolean loopLogin = false;

    public void LoopAuth() throws InterruptedException { //автологин каждые 15 мин.
            timeredLogin = new Thread(() -> {
                try {
                    while (true) {
                        Platform.runLater(() -> {
                            try {
                                info = false;
                                LogInYOTA();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        Thread.sleep(1000 * 60 * 15);
                    }
                } catch (Exception e) {
                    System.out.print("End...");
                }
            });
            timeredLogin.start();
        if (timeredLogin != null) {
            timeredLogin.interrupt();
            info = true;
        }
    }

    public void GetData() throws IOException { //получаем данные с портала
        clearAllOrNotAll = true;
        ClearAll();
        try {
            if (url.getText() == null || url.getText().equals("")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ошибка!");
                alert.setHeaderText("Недостаточно данных");
                alert.setContentText("Вставьте ссылку!");
                alert.showAndWait();
            }
            else
            {
                if (url.getText().contains(defaultURL))
                {
                    HtmlPage page2 = webClient.getPage(url.getText());
                    Iccid = page2.getFirstByXPath("//input[@id='order_sim_cards_0_iccid']");
                    Data = page2.getFirstByXPath("//input[@name='order[expected_delivery_date]']");
                    TimeBegin = page2.getFirstByXPath("//input[@name='order[expected_delivery_time_begin]']");
                    TimeEnd = page2.getFirstByXPath("//input[@name='order[expected_delivery_time_end]']");
                    ClientName = page2.getFirstByXPath("//input[@name='order[name_data_raw]']");
                    DateBdayAndLocation = page2.getFirstByXPath("//input[@id='order_birth_data_raw']");
                    ClientPhoneNumber = page2.getFirstByXPath("//input[@name='order[contact_phone]']");
                    Address = page2.getFirstByXPath("//input[@name='order[delivery_address]']");
                    Comment = page2.getFirstByXPath("//textarea[@name='order[comment]']");
                    iccidMemo.appendText(Iccid.getDefaultValue());
                    iccidSt.appendText(Iccid.getDefaultValue());
                    data.appendText(Data.getDefaultValue() + " c " + TimeBegin.getDefaultValue() + " по " + TimeEnd.getDefaultValue());
                    fioMemo.appendText(ClientName.getDefaultValue());
                    fioSt.appendText(ClientName.getDefaultValue());
                    String data = DateBdayAndLocation.getDefaultValue().replaceAll("[\\.]","").substring(0,8);
                    String location = DateBdayAndLocation.getDefaultValue().substring(11,DateBdayAndLocation.getDefaultValue().length());
                    dataBdayAndLocation.appendText(data+" "+location);
                    phoneMemo.appendText(ClientPhoneNumber.getDefaultValue());
                    String ph = ClientPhoneNumber.getDefaultValue().replaceAll("[\\+\\(\\)]",""); //убираем из номера телефона символы '+','(',')'
                    phoneSt.appendText(ph.substring(1,ph.length())); //добавляем в текстбокс номер телефона без 7-ки.
                    address.appendText(Address.getDefaultValue());
                    comment.appendText(Comment.getDefaultValue());
                }
                else
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Ошибка!");
                    alert.setHeaderText("Неверный формат ссылки.");
                    alert.setContentText("Проверьте правильность введеной ссылки.");
                    alert.showAndWait();
                }
            }
        } catch (NullPointerException npe) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Данные не считаны.");
            alert.setContentText("Необходимо перезайти в портал.");

            alert.showAndWait();
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Проверьте интернет соединение");
            alert.setContentText("Похоже у вас оборвалось соединение с интернетом...");
            alert.showAndWait();
        }
    }


    public void ClearAll() { //стереть все данные
        if (!clearAllOrNotAll) {
            url.clear();
        }
        iccidSt.clear();
        iccidMemo.clear();
        address.clear();
        data.clear();
        fioSt.clear();
        phoneMemo.clear();
        comment.clear();
        fioMemo.clear();
        dataBdayAndLocation.clear();
        phoneSt.clear();
        clearAllOrNotAll = false;
    }

    public void WebBrowserSettings() { //настраиваем клиент
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(false);
    }

    public void LogInYOTA() throws IOException, InterruptedException { //авторизируемся в портале
        WebBrowserSettings();
        try {
            HtmlPage page1 = webClient.getPage("https://partner.yota.ru/rd/login");
            HtmlForm form = page1.getFirstByXPath("//form[@action='/rd/login_check']");

            HtmlTextInput login = form.getInputByName("_username");
            HtmlPasswordInput password = form.getInputByName("_password");
            login.setValueAttribute("login");
            password.setValueAttribute("password");
            HtmlSubmitInput button = page1.getFirstByXPath("//input[@id='send_id' and @type='submit']");
            button.click();

            if (info) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Информация");
                alert.setHeaderText(null);
                alert.setContentText("Авторизация прошла успешно!");
                alert.showAndWait();
            }
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Проверьте интернет соединение");
            alert.setContentText("Похоже у вас оборвалось соединение с интернетом...");
            alert.showAndWait();
        }
        if(!loopLogin){
            LoopAuth();
            loopLogin=true;
        }
    }

    public void PrintMemo() { //выводим на печать памятку
        PrinterJob pjob = PrinterJob.getPrinterJob();
        pjob.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex == 0) {
                // Рисуем на graphics то, что должно быть отпечатано.
                double pageWidth = pageFormat.getImageableWidth();
                FontMetrics fm = graphics.getFontMetrics(graphics.getFont());
                int start = (int) pageWidth / 2;
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("Памятка", start - fm.stringWidth("Памятка") / 2, 50);
                graphics.drawString("ICCID:", start - fm.stringWidth("ICCID:") / 2, 65);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(iccidMemo.getText(), start - fm.stringWidth(iccidMemo.getText()) / 2, 80);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("Адрес доставки:", start - fm.stringWidth("Адрес доставки:") / 2, 95);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(address.getText(), start - fm.stringWidth(address.getText()) / 2, 110);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("Дата и время доставки:", start - fm.stringWidth("Дата и время доставки:") / 2, 125);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(data.getText(), start - fm.stringWidth(data.getText()) / 2, 140);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("ФИО Клиента:", start - fm.stringWidth("ФИО Клиента:") / 2, 155);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(fioMemo.getText(), start - fm.stringWidth(fioMemo.getText()) / 2, 170);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("Номер телефона:", start - fm.stringWidth("Номер телефона:") / 2, 185);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(phoneMemo.getText(), start - fm.stringWidth(phoneMemo.getText()) / 2, 200);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("Комментарий:", start - fm.stringWidth("Комментарий:") / 2, 215);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                if (fillCommentCheckBox.isSelected()) {
                    graphics.drawRect(190, 219, 200, 13);
                    graphics.setColor(Color.lightGray);
                    graphics.fillRect(190, 219, 200, 13);
                    graphics.setColor(Color.black);
                }
                graphics.drawString(comment.getText(), start - fm.stringWidth(comment.getText()) / 2, 230);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawRect(236, 234, 100, 13);
                graphics.setColor(Color.lightGray);
                graphics.fillRect(236, 234, 100, 13);
                graphics.setColor(Color.black);
                graphics.drawString("Заполнить ПД!", start - fm.stringWidth("Заполнить ПД!") / 2, 245);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString("Уважаемые партнеры и клиенты! Просим Вас заполнять «Заявление о присоединении к",
                        start - fm.stringWidth("Уважаемые партнеры и клиенты! Просим Вас заполнять «Заявление о присоединении к") / 2, 260);
                graphics.drawString("публичному договору» печатными буквами. В случае некорректного заполнения",
                        start - fm.stringWidth("публичному договору» печатными буквами. В случае некорректного заполнения") / 2, 275);
                graphics.drawString("(неполная, неразборчиво указанная информация) могут возникнуть сложности c обслуживанием",
                        start - fm.stringWidth("неполная, неразборчиво указанная информация) могут возникнуть сложности c обслуживанием") / 2, 290);
                graphics.drawString("sim-карты. Написать сотруднику Контактного центра можно через мобильное приложение",
                        start - fm.stringWidth("sim-карты. Написать сотруднику Контактного центра можно через мобильное приложение") / 2, 305);
                graphics.drawString("или через чат на сайте www.yota.ru. Приложение «YOTA» доступно для скачивания",
                        start - fm.stringWidth("или через чат на сайте www.yota.ru. Приложение «YOTA» доступно для скачивания") / 2, 320);
                graphics.drawString("на Google Play (Android) или на AppStore (IOS).",
                        start - fm.stringWidth("на Google Play (Android) или на AppStore (IOS).") / 2, 335);
                graphics.drawString("USSD-команды: 100# — проверить баланс; 101# — остаток минут; *103# — уточнить свой номер",
                        start - fm.stringWidth("USSD-команды: 100# — проверить баланс; 101# — остаток минут; *103# — уточнить свой номер") / 2, 350);

                return Printable.PAGE_EXISTS;
            }

            return Printable.NO_SUCH_PAGE;
        });
        //проверяем, заполнены ли все поля
        if (iccidMemo.getText().isEmpty() || address.getText().isEmpty() || data.getText().isEmpty() || phoneMemo.getText().isEmpty()
                || phoneMemo.getText().isEmpty() || comment.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Не все данные получены.");
            alert.setContentText("Заполните все поля!");
            alert.showAndWait();
        } else {
            if (pjob.printDialog()){
                try {
                    pjob.print();
                } catch (PrinterException pe) {
                    System.out.println("Error printing: " + pe);
                }
            }
        }
    }


    public void PrintStatement() throws IOException, PrinterException { //выводим на печать заявление
        PDDocument doc = null;
        doc = PDDocument.load(new File("C:/DocZ.pdf"));
        PDDocumentCatalog cat = doc.getDocumentCatalog();
        PDPage page = (PDPage)cat.getPages().get( 0 );
        doc.addPage(page);
        PDType0Font font = PDType0Font.load(doc, new File("C:/Windows/Fonts/cour.ttf"));

        PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);
        contentStream.beginText();
        contentStream.setFont(font,12);
        contentStream.appendRawCommands("5.2 Tc\n");
        contentStream.moveTextPositionByAmount(139, 708);
        contentStream.drawString(iccidSt.getText());
        String ph1 = phoneSt.getText().substring(0,3); //разделяем номер телефона на 3 части (xxx-xxx-xxxx)
        String ph2 = phoneSt.getText().substring(4,7);
        String ph3 = phoneSt.getText().substring(8,phoneSt.getText().length());
        contentStream.moveTextPositionByAmount(0,-632);
        contentStream.drawString(ph1);
        contentStream.moveTextPositionByAmount(50,0);
        contentStream.drawString(ph2);
        contentStream.moveTextPositionByAmount(49,0);
        contentStream.drawString(ph3);
        String day = dataBdayAndLocation.getText().substring(0,2); //разделяем дату рождения на 3 части (xx-xx-xxxx)
        String mounth = dataBdayAndLocation.getText().substring(2,4);
        String year = dataBdayAndLocation.getText().substring(4,8);
        contentStream.moveTextPositionByAmount(-137, 109 );
        contentStream.drawString(day);
        contentStream.moveTextPositionByAmount(37, 0 );
        contentStream.drawString(mounth);
        contentStream.moveTextPositionByAmount(37, 0 );
        contentStream.drawString(year);
        contentStream.appendRawCommands("5.25 Tc\n");
        if (fioSt.getText().length()<34){ //проверка на количество символов в строке, если >34, то после 34 перенос на след.строку.
            contentStream.moveTextPositionByAmount(-48,31 );
            contentStream.drawString(fioSt.getText());
            check = true;
        }
        else {
            contentStream.moveTextPositionByAmount(-48,31 );
            contentStream.drawString(fioSt.getText().substring(0,35));
            contentStream.moveTextPositionByAmount(0,-15 );
            contentStream.drawString(fioSt.getText().substring(35,fioSt.getText().length()));
            check = false;
        }
        String location = dataBdayAndLocation.getText().substring(8,dataBdayAndLocation.getText().length());
        if (location.length()<21){ //проверка на количество символов в строке, если >21, то после 21 перенос на след.строку.
            if(check){
                contentStream.moveTextPositionByAmount(162,-30 );
                contentStream.drawString(location);
            }
            else {
                contentStream.moveTextPositionByAmount(162,-15 );
                contentStream.drawString(location);
            }
        } else {
            if(check){
                contentStream.moveTextPositionByAmount(162,-30 );
                contentStream.drawString(location.substring(0, 22));
                contentStream.moveTextPositionByAmount(-187, -15);
                contentStream.drawString(location.substring(22,location.length()));
            } else {
                contentStream.moveTextPositionByAmount(162,-15 );
                contentStream.drawString(location.substring(0, 22));
                contentStream.moveTextPositionByAmount(-187,-15 );
                contentStream.drawString(location.substring(22,location.length()));
            }

        }
        contentStream.endText();
        contentStream.close();


        doc.save("C:/test.pdf");
        if (iccidSt.getText().isEmpty() || fioSt.getText().isEmpty() || dataBdayAndLocation.getText().isEmpty() || phoneSt.getText().isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Не все данные получены.");
            alert.setContentText("Заполните все поля!");
            alert.showAndWait();
        }
        else
        {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(doc));
            if (job.printDialog()) {
                job.print();
            }
        }
        doc.close();
    }

    public void AboutShow() throws IOException { //показываем окно "О программе"
        Parent root = FXMLLoader.load(getClass().getResource("About.fxml"));
        Stage stage = new Stage();
        stage.setTitle("О программе");
        stage.setResizable(false);
        stage.setScene(new Scene(root, 450, 200));
        stage.show();
    }

}