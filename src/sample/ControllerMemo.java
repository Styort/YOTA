package sample;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ControllerMemo {
    @FXML
    TextField url, iccidMemo,iccidSt, address, data, fioMemo, fioSt, phoneMemo, phoneSt
            , comment, dataBdayAndLocation, passportID, dateOfIssue,issueBy, registration,delivData,transferNumber;
    @FXML
    CheckBox fillCommentCheckBox;
    @FXML
    Tab memoTab,declarationTab;
    @FXML
    TabPane tabPane;
    @FXML
    ComboBox docTypeCB,operatorCB;
    WebClient webClient = new WebClient(BrowserVersion.CHROME);
    HtmlTextInput Iccid, Address, Data, TimeBegin, TimeEnd, ClientName, ClientPhoneNumber
            ,DateBdayAndLocation,PassportID,DateOfIssueAndIssueBy,Registration;
    HtmlTextArea Comment;
    String defaultURL = "https://partner.yota.ru/rd/vox/order/edit/";
    String pathToDesk = System.getProperty("user.home") + "/Desktop";
    Thread timeredLogin;
    boolean info = true;
    boolean check = false;
    boolean checkLineIssue = false;
    boolean clearAllOrNotAll = false;
    boolean loopLogin = false;

    public void loopAuth() { //Авторизация каждые 15мин.
        timeredLogin = new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000*60*15);
                        info=false;
                        LogInYOTA();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timeredLogin.start();
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
                    ClientName = page2.getFirstByXPath("//input[@id='order_personal_data_name_data_raw']");
                    DateBdayAndLocation = page2.getFirstByXPath("//input[@id='order_personal_data_birth_data_raw']");
                    ClientPhoneNumber = page2.getFirstByXPath("//input[@id='order_personal_data_contact_phone']");
                    Address = page2.getFirstByXPath("//input[@id='order_delivery_address']");
                    Comment = page2.getFirstByXPath("//textarea[@name='order[comment]']");
                    PassportID = page2.getFirstByXPath("//input[@id='order_personal_data_document_number_raw']");
                    DateOfIssueAndIssueBy = page2.getFirstByXPath("//input[@id='order_personal_data_document_issue_raw']");
                    Registration = page2.getFirstByXPath("//input[@id='order_personal_data_address']");
                    iccidMemo.appendText(Iccid.getDefaultValue());
                    iccidSt.appendText(Iccid.getDefaultValue());
                    data.appendText(Data.getDefaultValue() + " c " + TimeBegin.getDefaultValue() + " по " + TimeEnd.getDefaultValue());
                    delivData.appendText(Data.getDefaultValue());
                    fioMemo.appendText(ClientName.getDefaultValue());
                    fioSt.appendText(ClientName.getDefaultValue());
                    String dl = DateBdayAndLocation.getDefaultValue();
                    String data = dl.substring(0, 10);
                    String location = dl.substring(11, dl.length());
                    dataBdayAndLocation.appendText(data+" "+location);
                    phoneMemo.appendText(ClientPhoneNumber.getDefaultValue());
                    String ph = ClientPhoneNumber.getDefaultValue().replaceAll("[\\+\\(\\)]",""); //убираем из номера телефона символы '+','(',')'
                    phoneSt.appendText(ph.substring(1,ph.length())); //добавляем в текстбокс номер телефона без 7-ки.
                    address.appendText(Address.getDefaultValue());
                    comment.appendText(Comment.getDefaultValue());
                    if(!PassportID.getDefaultValue().isEmpty()){
                        passportID.appendText(PassportID.getDefaultValue());
                    }
                    String passDateAndIssueBy = DateOfIssueAndIssueBy.getDefaultValue();
                    if(!passDateAndIssueBy.isEmpty()){
                        dateOfIssue.appendText(passDateAndIssueBy.substring(0,10));
                        issueBy.appendText(passDateAndIssueBy.substring(11,passDateAndIssueBy.length()));
                    }
                    if(!Registration.getDefaultValue().isEmpty()){
                        registration.appendText(Registration.getDefaultValue());
                    }
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
        passportID.clear();
        dateOfIssue.clear();
        issueBy.clear();
        registration.clear();
        delivData.clear();
        transferNumber.clear();
        operatorCB.setPromptText("Выберите оператора(для MNP)");
        docTypeCB.setPromptText("Выберите тип документа(для MNP)");
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
        if(!loopLogin){
            loopAuth(); //Запуск авторизации каждые 15 мин.
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


    public void PrintStatement(ActionEvent actionEvent) throws IOException, PrinterException { //выводим на печать заявление
        PDDocument doc;
        if(actionEvent.getTarget().toString().contains("Печать MNP")){
            if (iccidSt.getText().isEmpty() || fioSt.getText().isEmpty() || dataBdayAndLocation.getText().isEmpty() || phoneSt.getText().isEmpty()
                    || passportID.getText().isEmpty() ||dateOfIssue.getText().isEmpty() || issueBy.getText().isEmpty()
                    || registration.getText().isEmpty()|| delivData.getText().isEmpty() || transferNumber.getText().isEmpty())
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ошибка!");
                alert.setHeaderText("Не все данные получены.");
                alert.setContentText("Все данные обязательны к заполнению!");
                alert.showAndWait();
            }
            else {
                try{
                    doc = PDDocument.load(new File(pathToDesk + "\\mnp_contract.pdf"));
                    PDDocumentCatalog cat = doc.getDocumentCatalog();
                    PDPage page = cat.getPages().get(0);
                    doc.addPage(page);
                    PDType0Font font = PDType0Font.load(doc, new File("C:/Windows/Fonts/times.ttf"));

                    PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);
                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    //contentStream.appendRawCommands("0.1 Tc\n"); //межбуквенный интервал

                    contentStream.moveTextPositionByAmount(53, 720);
                    contentStream.drawString("Москва, Московская область");

                    String delivDate = data.getText().substring(0, 10).replaceAll("[\\.]", "");
                    String dayDeliv = delivDate.substring(0, 2); //разделяем дату доставку на 3 части (xx-xx-xxxx)
                    String mounthDeliv = delivDate.substring(2, 4);
                    String yearDeliv = delivDate.substring(6, 8);
                    //Добавляем дату доставки
                    contentStream.moveTextPositionByAmount(332, 0);
                    contentStream.drawString(dayDeliv);
                    contentStream.moveTextPositionByAmount(63, 0);
                    contentStream.drawString(mounthDeliv);
                    contentStream.moveTextPositionByAmount(81, 0);
                    contentStream.drawString(yearDeliv);

                    //Добавляем фио
                    contentStream.moveTextPositionByAmount(-447, -33);
                    contentStream.drawString(fioSt.getText());
                    //Добавляем тип документа
                    contentStream.moveTextPositionByAmount(12, -23);
                    contentStream.drawString(docTypeCB.getValue().toString());
                    //Добавляем серию и номер документа
                    String passID = passportID.getText().replaceAll("\\s+", "");
                    if(docTypeCB.getValue().equals("Паспорт гражданина РФ")){

                        contentStream.moveTextPositionByAmount(210, 0);
                        contentStream.drawString(passID.substring(0, 4));
                        contentStream.moveTextPositionByAmount(90,0);
                        contentStream.drawString(passID.substring(4, passID.length()));
                    }
                    else {
                        contentStream.moveTextPositionByAmount(210, 0);
                        contentStream.drawString(passID.substring(0, 2));
                        contentStream.moveTextPositionByAmount(90,0);
                        contentStream.drawString(passID.substring(2, passID.length()));
                    }
                    //Добавляем кем выдан документ.
                    if(issueBy.getText().length()<75){
                        contentStream.moveTextPositionByAmount(-295, -22);
                        contentStream.drawString(issueBy.getText());
                    }
                    else {
                        contentStream.moveTextPositionByAmount(-295, -22);
                        contentStream.drawString(issueBy.getText().substring(0,74));
                        contentStream.moveTextPositionByAmount(-59, -23);
                        contentStream.drawString(issueBy.getText().substring(74,issueBy.getText().length()));
                    }
                    //Добавляем дату выдачи документа
                    if(issueBy.getText().length()<75){
                        contentStream.moveTextPositionByAmount(350, -23);
                        contentStream.drawString(dateOfIssue.getText());
                    }
                    else {
                        contentStream.moveTextPositionByAmount(409, 0);
                        contentStream.drawString(dateOfIssue.getText());
                    }
                    //Добавляем адрес регистрации
                    contentStream.moveTextPositionByAmount(-310, -23);
                    contentStream.drawString(registration.getText());
                    //Добавляем номер телефона
                    contentStream.moveTextPositionByAmount(10, -23);
                    contentStream.drawString(phoneSt.getText());
                    //Добавляем переносимый номер тел.
                    contentStream.moveTextPositionByAmount(153, -34);
                    contentStream.drawString(transferNumber.getText());
                    //Добавляем id сим-карты
                    contentStream.moveTextPositionByAmount(-20, -37);
                    contentStream.drawString(iccidSt.getText());
                    //Добавляем дату переноса номера
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH);
                    String dateTr = delivData.getText().substring(0,10);
                    LocalDate ld = LocalDate.parse(dateTr,formatter);

                    dateTr = ld.plusDays(12).toString().replaceAll("-", ".");
                    contentStream.moveTextPositionByAmount(-20, -37);
                    contentStream.drawString(dateTr.substring(8,10)+dateTr.substring(4,7)+"."+dateTr.substring(0,4)+":00");
                    //Добавляем оператора
                    contentStream.moveTextPositionByAmount(-125, -113);
                    contentStream.drawString(operatorCB.getValue().toString());

                    contentStream.endText();
                    contentStream.close();
                    if(docTypeCB.getValue().toString().equals("Выберите тип документа(для MNP)")
                            ||operatorCB.getValue().toString().equals("Выберите оператора(для MNP)")){
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Ошибка!");
                        alert.setHeaderText("Недостаточно данных");
                        alert.setContentText("Выберите тип документа и оператора!");
                        alert.showAndWait();
                    }
                    else {
                        try{
                            doc.save(pathToDesk + "\\mnp_contractZap.pdf");
                            PrinterJob job = PrinterJob.getPrinterJob();
                            job.setPageable(new PDFPageable(doc));
                            if (job.printDialog()) {
                                job.print();
                            }
                            doc.close();
                        }
                        catch (FileNotFoundException ex){
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Ошибка");
                            alert.setHeaderText(null);
                            alert.setContentText("Закройте PDF-файл с MNP!");
                            alert.showAndWait();
                        }
                    }
                }
                catch (FileNotFoundException ex){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText(null);
                    alert.setContentText("      Файл с заявлением не найден. \n Переместите файл на рабочий стол.");
                    alert.showAndWait();
                }
            }
        }
        if(actionEvent.getTarget().toString().contains("Печать заявления"))
        {
            if (iccidSt.getText().isEmpty() || fioSt.getText().isEmpty() || dataBdayAndLocation.getText().isEmpty()
                    || phoneSt.getText().isEmpty()||delivData.getText().isEmpty())
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ошибка!");
                alert.setHeaderText("Не все данные получены.");
                alert.setContentText("                                             Поля : " +
                        "\n'ICCID', 'ФИО', 'Дата и место рождения', 'Номер телефона', 'Дата доставки'" +
                        " \n                         Обязательны к заполнению!");
                alert.showAndWait();
            }
            else {
                try {
                    doc = PDDocument.load(new File(pathToDesk + "\\DocZ.pdf"));

                    PDDocumentCatalog cat = doc.getDocumentCatalog();
                    PDPage page = cat.getPages().get(0);
                    doc.addPage(page);
                    PDType0Font font = PDType0Font.load(doc, new File("C:/Windows/Fonts/cour.ttf"));

                    PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.appendRawCommands("5.2 Tc\n"); //межбуквенный интервал
                    //Добавляем ID
                    contentStream.moveTextPositionByAmount(139, 708);
                    contentStream.drawString(iccidSt.getText());
                    //Добавляем номер телефона
                    String phone = phoneSt.getText().replaceAll("\\s+", "");
                    String ph1 = phone.substring(0, 3); //разделяем номер телефона на 3 части (xxx-xxx-xxxx)
                    String ph2 = phone.substring(3, 6);
                    String ph3 = phone.substring(6, phone.length());
                    contentStream.moveTextPositionByAmount(0, -632);
                    contentStream.drawString(ph1);
                    contentStream.moveTextPositionByAmount(50, 0);
                    contentStream.drawString(ph2);
                    contentStream.moveTextPositionByAmount(49, 0);
                    contentStream.drawString(ph3);
                    //Добавляем дату рождения
                    String birthD = dataBdayAndLocation.getText().substring(0, 10).replaceAll("[\\.]", "");
                    String day = birthD.substring(0, 2); //разделяем дату рождения на 3 части (xx-xx-xxxx)
                    String mounth = birthD.substring(2, 4);
                    String year = birthD.substring(4, 8);
                    contentStream.moveTextPositionByAmount(-137, 109);
                    contentStream.drawString(day);
                    contentStream.moveTextPositionByAmount(37, 0);
                    contentStream.drawString(mounth);
                    contentStream.moveTextPositionByAmount(37, 0);
                    contentStream.drawString(year);
                    contentStream.appendRawCommands("5.25 Tc\n");
                    //Добавляем ФИО
                    if (fioSt.getText().length() < 34) { //проверка на количество символов в строке, если >34, то после 34 перенос на след.строку.
                        contentStream.moveTextPositionByAmount(-48, 31);
                        contentStream.drawString(fioSt.getText());
                        check = true;
                    } else {
                        contentStream.moveTextPositionByAmount(-48, 31);
                        contentStream.drawString(fioSt.getText().substring(0, 35));
                        contentStream.moveTextPositionByAmount(0, -15);
                        contentStream.drawString(fioSt.getText().substring(35, fioSt.getText().length()));
                        check = false;
                    }
                    //Добавляем место рождения
                    String location = dataBdayAndLocation.getText().substring(11, dataBdayAndLocation.getText().length());
                    if (location.length() < 21) { //проверка на количество символов в строке, если >21, то после 21 перенос на след.строку.
                        if (check) {
                            contentStream.moveTextPositionByAmount(175, -30);
                            contentStream.drawString(location);
                        } else {
                            contentStream.moveTextPositionByAmount(175, -15);
                            contentStream.drawString(location);
                            check = true;
                        }
                    } else {
                        if (check) {
                            contentStream.moveTextPositionByAmount(175, -30);
                            contentStream.drawString(location.substring(0, 21));
                            contentStream.moveTextPositionByAmount(-200, -16);
                            contentStream.drawString(location.substring(21, location.length()));
                            check = false;
                        } else {
                            contentStream.moveTextPositionByAmount(175, -15);
                            contentStream.drawString(location.substring(0, 21));
                            contentStream.moveTextPositionByAmount(-200, -16);
                            contentStream.drawString(location.substring(21, location.length()));
                            check = false;
                        }

                    }
                    //Добавляем дату доставки
                    String delivDate = delivData.getText().substring(0, 10).replaceAll("[\\.]", "");
                    String dayDeliv = delivDate.substring(0, 2); //разделяем дату доставку на 3 части (xx-xx-xxxx)
                    String mounthDeliv = delivDate.substring(2, 4);
                    String yearDeliv = delivDate.substring(4, 8);
                    if (check) {
                        contentStream.moveTextPositionByAmount(-200, -126);
                        contentStream.drawString(dayDeliv);
                        contentStream.moveTextPositionByAmount(37, 0);
                        contentStream.drawString(mounthDeliv);
                        contentStream.moveTextPositionByAmount(37, 0);
                        contentStream.drawString(yearDeliv);
                    } else {
                        contentStream.moveTextPositionByAmount(0, -109);
                        contentStream.drawString(dayDeliv);
                        contentStream.moveTextPositionByAmount(37, 0);
                        contentStream.drawString(mounthDeliv);
                        contentStream.moveTextPositionByAmount(37, 0);
                        contentStream.drawString(yearDeliv);
                    }
                    //Добавляем серию и номер паспорта
                    if (!passportID.getText().isEmpty()) {
                        String passID = passportID.getText().replaceAll("\\s+", "");
                            contentStream.moveTextPositionByAmount(-75, 94);
                            contentStream.drawString(passID.substring(0, 4));
                            contentStream.moveTextPositionByAmount(88, 0);
                            contentStream.drawString(passID.substring(4, passID.length()));
                    }
                    //Добавляем дату выдачи документа
                    if (!dateOfIssue.getText().isEmpty()) {
                        String dateIssue = dateOfIssue.getText().replaceAll("[\\.]", "");
                        String dayIssue = dateIssue.substring(0, 2); //разделяем дату рождения на 3 части (xx-xx-xxxx)
                        String mounthIssue = dateIssue.substring(2, 4);
                        String yearIssue = dateIssue.substring(4, 8);
                        contentStream.moveTextPositionByAmount(137, 0);
                        contentStream.drawString(dayIssue);
                        contentStream.moveTextPositionByAmount(37, 0);
                        contentStream.drawString(mounthIssue);
                        contentStream.moveTextPositionByAmount(37, 0);
                        contentStream.drawString(yearIssue);
                    }
                    //Добавляем кем выдан документ
                    if (!issueBy.getText().isEmpty()) {
                        if (issueBy.getText().length() < 37) {
                            contentStream.moveTextPositionByAmount(-299, -16);
                            contentStream.drawString(issueBy.getText());
                            checkLineIssue = false;
                        } else {
                            contentStream.moveTextPositionByAmount(-299, -16);
                            contentStream.drawString(issueBy.getText().substring(0, 37));
                            contentStream.moveTextPositionByAmount(0, -15);
                            contentStream.drawString(issueBy.getText().substring(37, issueBy.getText().length()));
                            checkLineIssue = true;
                        }
                    }
                    //Добавляем адрес места жительства
                    if (!registration.getText().isEmpty()) {
                        if (registration.getText().length() < 34) {
                            if (checkLineIssue) {
                                contentStream.moveTextPositionByAmount(37, -14);
                                contentStream.drawString(registration.getText());
                            } else {
                                contentStream.moveTextPositionByAmount(37, -30);
                                contentStream.drawString(registration.getText());
                            }
                        } else {
                            if (checkLineIssue) {
                                contentStream.moveTextPositionByAmount(37, -15);
                                contentStream.drawString(registration.getText().substring(0, 34));
                                contentStream.moveTextPositionByAmount(0, -15);
                                contentStream.drawString(registration.getText().substring(34, registration.getText().length()));
                            } else {
                                contentStream.moveTextPositionByAmount(37, -30);
                                contentStream.drawString(registration.getText().substring(0, 34));
                                contentStream.moveTextPositionByAmount(0, -15);
                                contentStream.drawString(registration.getText().substring(34, registration.getText().length()));
                            }
                        }
                    }


                    contentStream.endText();
                    contentStream.close();

                    try{
                        doc.save(pathToDesk + "\\zayavlenie.pdf");
                        PrinterJob job = PrinterJob.getPrinterJob();
                        job.setPageable(new PDFPageable(doc));
                        if (job.printDialog()) {
                            job.print();
                        }
                        doc.close();
                    }
                    catch (FileNotFoundException ex){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Закройте PDF-файл с заявлением!");
                        alert.showAndWait();
                    }
                } catch (FileNotFoundException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText(null);
                    alert.setContentText("      Файл с заявлением не найден. \n Переместите файл на рабочий стол.");
                    alert.showAndWait();
                }
            }
        }
    }

    public void AboutShow() throws IOException { //показываем окно "О программе"
        Parent root = FXMLLoader.load(getClass().getResource("About.fxml"));
        Stage stage = new Stage();
        stage.setTitle("О программе");
        stage.setResizable(false);
        stage.setScene(new Scene(root, 450, 200));
        stage.show();
        stage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }
}