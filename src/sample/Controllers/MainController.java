package sample.Controllers;

import com.gargoylesoftware.htmlunit.html.*;

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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sample.Authorization;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.print.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MainController {
    Authorization authorization = new Authorization();
    @FXML
    TextField url, iccidMemo, iccidSt, address, data, fioMemo, fioSt, phoneMemo, phoneSt, comment, dataBdayAndLocation, passportID, dateOfIssue, issueBy, registration, delivData, transferNumber;
    @FXML
    CheckBox fillCommentCheckBox;
    @FXML
    Tab memoTab, declarationTab;
    @FXML
    TabPane tabPane;
    @FXML
    ComboBox docTypeCB, operatorCB;
    HtmlTextInput Iccid, Address, Data, TimeBegin, TimeEnd, ClientName, ClientPhoneNumber, DateBdayAndLocation, PassportID, DateOfIssueAndIssueBy, Registration;
    HtmlTextArea Comment;
    String defaultURL = "https://partner.yota.ru/rd/vox/order/edit/";
    String pathToFolder = System.getProperty("user.home") + "/Desktop";
    boolean check = false;
    boolean checkLineIssue = false;
    boolean clearAllOrNotAll = false;
    boolean passportCheck = false;

    public void LoadSettings() throws ParserConfigurationException, IOException, SAXException {
        File f = new File("preferences.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(f);
        NodeList nodeList = document.getElementsByTagName("entry");
        pathToFolder = nodeList.item(2).getAttributes().getNamedItem("value").getNodeValue();
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
            } else {
                if (url.getText().contains(defaultURL)) {
                    HtmlPage page2 = authorization.webClient.getPage(url.getText());
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
                    dataBdayAndLocation.appendText(data + " " + location);

                    phoneMemo.appendText(ClientPhoneNumber.getDefaultValue());
                    String ph = ClientPhoneNumber.getDefaultValue().replaceAll("[\\+\\(\\)]", ""); //убираем из номера телефона символы '+','(',')'
                    phoneSt.appendText(ph.substring(1, ph.length())); //добавляем в текстбокс номер телефона без 7-ки.
                    address.appendText(Address.getDefaultValue());
                    comment.appendText(Comment.getDefaultValue());
                    if (!PassportID.getDefaultValue().isEmpty()) {
                        passportID.appendText(PassportID.getDefaultValue());
                    }
                    String passDateAndIssueBy = DateOfIssueAndIssueBy.getDefaultValue();
                    if (!passDateAndIssueBy.isEmpty()) {
                        dateOfIssue.appendText(passDateAndIssueBy.substring(0, 10));
                        issueBy.appendText(passDateAndIssueBy.substring(11, passDateAndIssueBy.length()));
                    }
                    if (!Registration.getDefaultValue().isEmpty()) {
                        registration.appendText(Registration.getDefaultValue());
                    }
                } else {
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
        operatorCB.setValue("Выберите оператора(для MNP)");
        docTypeCB.setValue("Выберите тип документа");
        clearAllOrNotAll = false;
    }


    public void LogInYOTA() throws ParserConfigurationException, SAXException, IOException { //авторизируемся в портале
        authorization.LoginInWebsite();
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
            if (pjob.printDialog()) {
                try {
                    pjob.print();
                } catch (PrinterException pe) {
                    System.out.println("Error printing: " + pe);
                }
            }
        }
    }

    public void PrintStatement(ActionEvent actionEvent) throws IOException, PrinterException, ParserConfigurationException, SAXException { //выводим на печать заявление
        LoadSettings();
        PDDocument doc;
        if (actionEvent.getTarget().toString().contains("Печать MNP")) {
            if (iccidSt.getText().isEmpty() || fioSt.getText().isEmpty() || dataBdayAndLocation.getText().isEmpty() || phoneSt.getText().isEmpty()
                    || delivData.getText().isEmpty() || transferNumber.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ошибка!");
                alert.setHeaderText("Не все данные получены.");
                alert.setContentText("Заполните минимальные данные!");
                alert.showAndWait();
            } else try {
                doc = PDDocument.load(new File(pathToFolder + "\\contract_mnp.pdf"));
                PDDocumentCatalog cat = doc.getDocumentCatalog();
                PDPage page = cat.getPages().get(0);
                doc.addPage(page);
                PDType0Font font = PDType0Font.load(doc, new File("C:/Windows/Fonts/timesbd.ttf"));

                PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);
                contentStream.beginText();
                contentStream.setFont(font, 10);
                //contentStream.appendRawCommands("0.1 Tc\n"); //межбуквенный интервал

                contentStream.moveTextPositionByAmount(53, 720);
                contentStream.drawString("Москва, Московская область");

                String dayDeliv = delivData.getText().substring(0, 2); //разделяем дату доставку на 3 части (xx-xx-xxxx)
                String mounthDeliv = delivData.getText().substring(3, 5);
                switch (mounthDeliv) {
                    case "01":
                        mounthDeliv = "Января";
                        break;
                    case "02":
                        mounthDeliv = "Февраля";
                        break;
                    case "03":
                        mounthDeliv = "Марта";
                        break;
                    case "04":
                        mounthDeliv = "Апреля";
                        break;
                    case "05":
                        mounthDeliv = "Мая";
                        break;
                    case "06":
                        mounthDeliv = "Июня";
                        break;
                    case "07":
                        mounthDeliv = "Июля";
                        break;
                    case "08":
                        mounthDeliv = "Августа";
                        break;
                    case "09":
                        mounthDeliv = "Сентября";
                        break;
                    case "10":
                        mounthDeliv = "Октября";
                        break;
                    case "11":
                        mounthDeliv = "Ноября";
                        break;
                    case "12":
                        mounthDeliv = "Декабря";
                        break;
                }
                String yearDeliv = delivData.getText().substring(8, 10);
                //Добавляем дату доставки
                contentStream.moveTextPositionByAmount(332, 0);
                contentStream.drawString(dayDeliv);
                contentStream.moveTextPositionByAmount(50, 0);
                contentStream.drawString(mounthDeliv);
                contentStream.moveTextPositionByAmount(94, 0);
                contentStream.drawString(yearDeliv);

                //Добавляем фио
                contentStream.moveTextPositionByAmount(-447, -33);
                contentStream.drawString(fioSt.getText());
                //Добавляем номер телефона
                contentStream.moveTextPositionByAmount(70, -134);
                contentStream.drawString(phoneSt.getText());
                //Добавляем переносимый номер тел.
                contentStream.moveTextPositionByAmount(153, -47);
                contentStream.drawString(transferNumber.getText());
                //Добавляем id сим-карты
                contentStream.moveTextPositionByAmount(-20, -36);
                contentStream.drawString(iccidSt.getText());
                //Добавляем дату переноса номера
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH);
                String dateTr = delivData.getText().substring(0, 10);
                LocalDate ld = LocalDate.parse(dateTr, formatter);

                dateTr = ld.plusDays(12).toString().replaceAll("-", ".");
                contentStream.moveTextPositionByAmount(-20, -37);
                contentStream.drawString(dateTr.substring(8, 10) + dateTr.substring(4, 7) + "." + dateTr.substring(0, 4) + ":00");
                //Добавляем оператора
                contentStream.moveTextPositionByAmount(-125, -113);
                contentStream.drawString(operatorCB.getValue().toString());

                if (!docTypeCB.getValue().equals("Не полные ПД")) {
                    //Добавляем тип документа
                    contentStream.moveTextPositionByAmount(-46, 344);
                    contentStream.drawString(docTypeCB.getValue().toString());
                    //Добавляем серию и номер документа
                    String passID = passportID.getText().replaceAll("\\s+", "");
                    if (docTypeCB.getValue().equals("Паспорт гражданина РФ")) {

                        contentStream.moveTextPositionByAmount(215, 0);
                        contentStream.drawString(passID.substring(0, 4));
                        contentStream.moveTextPositionByAmount(98, 0);
                        contentStream.drawString(passID.substring(4, passID.length()));
                    } else {
                        contentStream.moveTextPositionByAmount(215, 0);
                        contentStream.drawString(passID.substring(0, 2));
                        contentStream.moveTextPositionByAmount(98, 0);
                        contentStream.drawString(passID.substring(2, passID.length()));
                    }
                    //Добавляем кем выдан документ.
                    if (issueBy.getText().length() < 66) {
                        contentStream.moveTextPositionByAmount(-306, -22);
                        contentStream.drawString(issueBy.getText());
                    } else {
                        contentStream.moveTextPositionByAmount(-305, -22);
                        contentStream.drawString(issueBy.getText().substring(0, 65));
                        contentStream.moveTextPositionByAmount(-59, -23);
                        contentStream.drawString(issueBy.getText().substring(65, issueBy.getText().length()));
                    }
                    //Добавляем дату выдачи документа
                    if (issueBy.getText().length() < 66) {
                        contentStream.moveTextPositionByAmount(350, -23);
                        contentStream.drawString(dateOfIssue.getText());
                    } else {
                        contentStream.moveTextPositionByAmount(409, 0);
                        contentStream.drawString(dateOfIssue.getText());
                    }
                    //Добавляем адрес регистрации
                    if (registration.getText().length() < 62) {
                        contentStream.moveTextPositionByAmount(-313, -23);
                        contentStream.drawString(registration.getText());
                    } else {
                        contentStream.moveTextPositionByAmount(-313, -23);
                        contentStream.drawString(registration.getText().substring(0, 61));
                        contentStream.moveTextPositionByAmount(-100, -20);
                        contentStream.drawString(registration.getText().substring(61, registration.getText().length()));
                    }
                }

                contentStream.endText();
                contentStream.close();
                if (docTypeCB.getValue().toString().equals("Выберите тип документа")
                        || operatorCB.getValue().toString().equals("Выберите оператора(для MNP)")) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Ошибка!");
                    alert.setHeaderText("Недостаточно данных");
                    alert.setContentText("Выберите тип документа и оператора!");
                    alert.showAndWait();
                } else {
                    try {
                        doc.save(pathToFolder + "\\mnp_complite.pdf");
                        PrinterJob job = PrinterJob.getPrinterJob();
                        job.setPageable(new PDFPageable(doc));
                        if (job.printDialog()) {
                            job.print();
                        }
                        doc.close();
                    } catch (FileNotFoundException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Закройте PDF-файл с MNP!");
                        alert.showAndWait();
                    }
                }
            } catch (FileNotFoundException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("      Файл с заявлением не найден. \n Переместите файл в указанную в настройках папку.");
                alert.showAndWait();
            }
        }
        if (actionEvent.getTarget().toString().contains("Печать заявления")) {
            if (iccidSt.getText().isEmpty() || fioSt.getText().isEmpty() || dataBdayAndLocation.getText().isEmpty()
                    || phoneSt.getText().isEmpty() || delivData.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ошибка!");
                alert.setHeaderText("Не все данные получены.");
                alert.setContentText("                                             Поля : " +
                        "\n'ICCID', 'ФИО', 'Дата и место рождения', 'Номер телефона', 'Дата доставки'" +
                        " \n                         Обязательны к заполнению!");
                alert.showAndWait();
            } else {
                try {
                    if (docTypeCB.getValue().toString().equals("Выберите тип документа") ||
                            docTypeCB.getValue().toString().equals("Паспорт гражданина РФ")) {
                        doc = PDDocument.load(new File(pathToFolder + "\\contract_rf.pdf"));
                        passportCheck = true;
                    } else {
                        doc = PDDocument.load(new File(pathToFolder + "\\contract_ino.pdf"));
                        passportCheck = false;
                    }

                    PDDocumentCatalog cat = doc.getDocumentCatalog();
                    PDPage page = cat.getPages().get(0);
                    doc.addPage(page);
                    PDType0Font font = PDType0Font.load(doc, new File("C:/Windows/Fonts/courbd.ttf"));
                    font.getFontDescriptor().setForceBold(true);

                    PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.appendRawCommands("5.2 Tc\n"); //межбуквенный интервал
                    int iccidLen = iccidSt.getText().length();
                    int moveAmountHeight = 0, moveAmountWidth = 0;
                    //Добавляем ID
                    try {
                        if (iccidLen < 11) {
                            contentStream.moveTextPositionByAmount(139, 708);
                            contentStream.drawString(iccidSt.getText());
                            moveAmountHeight = -632;
                            moveAmountWidth = 0;
                        } else if (iccidLen > 10 & iccidLen < 22) {
                            contentStream.moveTextPositionByAmount(139, 708);
                            contentStream.drawString(iccidSt.getText().substring(0, 10));
                            contentStream.moveTextPositionByAmount(-100, -462);
                            contentStream.drawString(iccidSt.getText().substring(11, 21));
                            moveAmountWidth = 100;
                            moveAmountHeight = -170;
                        } else if (iccidLen > 21 & iccidLen < 33) {
                            contentStream.moveTextPositionByAmount(139, 708);
                            contentStream.drawString(iccidSt.getText().substring(0, 10));
                            contentStream.moveTextPositionByAmount(-100, -462);
                            contentStream.drawString(iccidSt.getText().substring(11, 21));
                            contentStream.moveTextPositionByAmount(133, 0);
                            contentStream.drawString(iccidSt.getText().substring(22, 32));
                            moveAmountWidth = -33;
                            moveAmountHeight = -170;
                        } else if (iccidLen > 32 & iccidLen < 44) {
                            contentStream.moveTextPositionByAmount(139, 708);
                            contentStream.drawString(iccidSt.getText().substring(0, 10));
                            contentStream.moveTextPositionByAmount(-100, -462);
                            contentStream.drawString(iccidSt.getText().substring(11, 21));
                            contentStream.moveTextPositionByAmount(133, 0);
                            contentStream.drawString(iccidSt.getText().substring(22, 32));
                            contentStream.moveTextPositionByAmount(133, 0);
                            contentStream.drawString(iccidSt.getText().substring(33, 43));
                            moveAmountWidth = -166;
                            moveAmountHeight = 170;
                        } else if (iccidLen > 43 & iccidLen < 55) {
                            contentStream.moveTextPositionByAmount(139, 708);
                            contentStream.drawString(iccidSt.getText().substring(0, 10));
                            contentStream.moveTextPositionByAmount(-100, -462);
                            contentStream.drawString(iccidSt.getText().substring(11, 21));
                            contentStream.moveTextPositionByAmount(133, 0);
                            contentStream.drawString(iccidSt.getText().substring(22, 32));
                            contentStream.moveTextPositionByAmount(133, 0);
                            contentStream.drawString(iccidSt.getText().substring(33, 43));
                            contentStream.moveTextPositionByAmount(133, 0);
                            contentStream.drawString(iccidSt.getText().substring(44, 54));
                            moveAmountWidth = -299;
                            moveAmountHeight = -170;
                        }
                    } catch (Exception ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Введено неверное количество сим-карт.");
                        alert.showAndWait();
                    }

                    //Добавляем номер телефона
                    String phone = phoneSt.getText().replaceAll("\\s+", "");
                    String ph1 = phone.substring(0, 3); //разделяем номер телефона на 3 части (xxx-xxx-xxxx)
                    String ph2 = phone.substring(3, 6);
                    String ph3 = phone.substring(6, phone.length());
                    contentStream.moveTextPositionByAmount(moveAmountWidth, moveAmountHeight);
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
                    if (fioSt.getText().length() < 35) { //проверка на количество символов в строке, если >34, то после 34 перенос на след.строку.
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
                    int xAmountDataIs = 0;
                    int xAmountIsBy = 0;
                    int yAmountPassID = 0;
                    //Добавляем адрес места жительства
                    if (!registration.getText().isEmpty()) {
                        if (registration.getText().length() < 34) {
                            contentStream.moveTextPositionByAmount(-37, 47);
                            contentStream.drawString(registration.getText());
                            yAmountPassID = 46;
                        } else {
                            contentStream.moveTextPositionByAmount(-37, 47);
                            contentStream.drawString(registration.getText().substring(0, 34));
                            contentStream.moveTextPositionByAmount(0, -15);
                            contentStream.drawString(registration.getText().substring(34, registration.getText().length()));
                            yAmountPassID = 61;
                        }
                    }
                    //Добавляем серию и номер паспорта
                    if (!passportID.getText().isEmpty()) {
                        if (passportCheck) {
                            String passID = passportID.getText().replaceAll("\\s+", "");
                            contentStream.moveTextPositionByAmount(-37, yAmountPassID);
                            contentStream.drawString(passID.substring(0, 4));
                            contentStream.moveTextPositionByAmount(88, 0);
                            contentStream.drawString(passID.substring(4, passID.length()));
                            xAmountDataIs = 137;
                            xAmountIsBy = -299;

                        } else {
                            contentStream.moveTextPositionByAmount(-37, yAmountPassID);
                            contentStream.drawString(passportID.getText());
                            xAmountDataIs = 250;
                            xAmountIsBy = -324;
                        }
                    }
                    //Добавляем дату выдачи документа
                    if (!dateOfIssue.getText().isEmpty()) {
                        String dateIssue = dateOfIssue.getText().replaceAll("[\\.]", "");
                        String dayIssue = dateIssue.substring(0, 2); //разделяем дату рождения на 3 части (xx-xx-xxxx)
                        String mounthIssue = dateIssue.substring(2, 4);
                        String yearIssue = dateIssue.substring(4, 8);
                        contentStream.moveTextPositionByAmount(xAmountDataIs, 0);
                        contentStream.drawString(dayIssue);
                        contentStream.moveTextPositionByAmount(37, 0);
                        contentStream.drawString(mounthIssue);
                        contentStream.moveTextPositionByAmount(37, 0);
                        contentStream.drawString(yearIssue);
                    }
                    //Добавляем кем выдан документ
                    if (!issueBy.getText().isEmpty()) {
                        if (issueBy.getText().length() < 37) {
                            contentStream.moveTextPositionByAmount(xAmountIsBy, -16);
                            contentStream.drawString(issueBy.getText());
                            checkLineIssue = false;
                        } else {
                            contentStream.moveTextPositionByAmount(xAmountIsBy, -16);
                            contentStream.drawString(issueBy.getText().substring(0, 37));
                            contentStream.moveTextPositionByAmount(0, -15);
                            contentStream.drawString(issueBy.getText().substring(37, issueBy.getText().length()));
                            checkLineIssue = true;
                        }
                    }

                    contentStream.endText();
                    contentStream.close();

                    try {
                        doc.save(pathToFolder + "\\contract_complite.pdf");
                        PrinterJob job = PrinterJob.getPrinterJob();
                        job.setPageable(new PDFPageable(doc));
                        if (job.printDialog()) {
                            job.print();
                        }
                        doc.close();
                    } catch (FileNotFoundException ex) {
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
                    alert.setContentText("      Файл с заявлением не найден. \n Переместите файл в указанную в настройках папку..");
                    alert.showAndWait();
                }
            }
        }

    }

    public void ExcelSave() throws IOException { //Сохранение ПД в Excel
        try {
            FileInputStream inputStream = new FileInputStream(pathToFolder + "\\contactYota.xls");
            Workbook wb = new HSSFWorkbook(inputStream); //Создаем книгу
            Sheet sheet = wb.getSheet("ПД"); //создаем лист

            Row row = sheet.createRow(sheet.getLastRowNum() + 1); //находим последний заполненный ряд и записываем данные на следующий.

            //Указываем какие ячейки в ряду row заполнять данными.
            Cell iccidCell = row.createCell(0);
            Cell deliveryDataCell = row.createCell(1);
            Cell fioCell = row.createCell(2);
            Cell dataBdayBornPlaceCell = row.createCell(3);
            Cell passportidCell = row.createCell(4);
            Cell dateOfIssueCell = row.createCell(5);
            Cell issueByCell = row.createCell(6);
            Cell registrationCell = row.createCell(7);
            Cell phoneCell = row.createCell(8);
            Cell deliveryPlaceCell = row.createCell(9);
            Cell commentCell = row.createCell(10);
            inputStream.close();

            //Заполняем данными указанные ячейки
            iccidCell.setCellValue(iccidSt.getText());
            deliveryDataCell.setCellValue(data.getText());
            fioCell.setCellValue(fioMemo.getText());
            dataBdayBornPlaceCell.setCellValue(dataBdayAndLocation.getText());
            passportidCell.setCellValue(passportID.getText());
            dateOfIssueCell.setCellValue(dateOfIssue.getText());
            issueByCell.setCellValue(issueBy.getText());
            registrationCell.setCellValue(registration.getText());
            phoneCell.setCellValue(phoneSt.getText());
            deliveryPlaceCell.setCellValue(address.getText());
            commentCell.setCellValue(comment.getText());

            FileOutputStream fos = new FileOutputStream(pathToFolder + "\\contactYota.xls");

            wb.write(fos);
            fos.close();
        } catch (FileNotFoundException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Закройте Excel файл.");
            alert.showAndWait();
        }
    }

    public void AboutShow() throws IOException { //показываем окно "О программе"
        Parent root = FXMLLoader.load(getClass().getResource("/sample/View/About.fxml"));
        Stage stage = new Stage();
        stage.setTitle("О программе");
        stage.setResizable(false);
        stage.setScene(new Scene(root, 450, 200));
        stage.show();
    }

    public void OpenSettings(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/sample/View/Settings.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Настройки");
        stage.setResizable(false);
        stage.setScene(new Scene(root, 350, 350));
        stage.show();
    }
}