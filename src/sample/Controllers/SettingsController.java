package sample.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by Виктор on 22.03.2016.
 */
public class SettingsController implements Initializable {
    @FXML
    private TextField folderTF,loginTF,passwordTF;

    Preferences preferences = Preferences.userNodeForPackage(SettingsController.class);
    DirectoryChooser chooser = new DirectoryChooser();

    //Выбор папки с заявлением
    public void setFolder(ActionEvent actionEvent) throws BackingStoreException, IOException {
        File file = chooser.showDialog(null);
        folderTF.setText(file.getPath() + "\\");
        preferences.put("folder", folderTF.getText());
        preferences.flush();
        preferences.exportSubtree(new BufferedOutputStream(
                new FileOutputStream("preferences.xml")));
    }

    //Считывание настроек
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File f = new File("preferences.xml");
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(f);
            NodeList nodeList = document.getElementsByTagName("entry");
            folderTF.setText(nodeList.item(2).getAttributes().getNamedItem("value").getNodeValue());
            loginTF.setText(nodeList.item(0).getAttributes().getNamedItem("value").getNodeValue());
            passwordTF.setText(nodeList.item(1).getAttributes().getNamedItem("value").getNodeValue());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAuthSettings(ActionEvent actionEvent) throws BackingStoreException, IOException {
        preferences.put("login", loginTF.getText());
        preferences.put("password", passwordTF.getText());
        preferences.flush();
        preferences.exportSubtree(new BufferedOutputStream(
                new FileOutputStream("preferences.xml")));
    }
}
