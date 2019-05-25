package de.darlor.dacardconv.panes;

import de.darlor.dacardconv.DaCardConv;
import de.darlor.dacardconv.Settings;
import de.darlor.dacardconv.tasks.CardDAVImporterTask;
import de.darlor.dacardconv.utils.CardDAVServer;
import java.net.MalformedURLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 *
 * @author Vincent Neubauer (v.neubauer@darlor.de)
 */
public class ConnectionPane {

	private static VBox connPane;
	private final ObservableList<String> remoteAddrBooks;
	private CardDAVServer server;

	public ConnectionPane(DaCardConv app) {
		remoteAddrBooks = FXCollections.observableArrayList();

		TextField tfWebdavUrl = new TextField(Settings.getWebdavAddress());
		tfWebdavUrl.textProperty().addListener((obs, oldText, newUrl) -> {
			Settings.setWebdavAddress(newUrl);
		});
		TextField tfWebdavUser = new TextField(Settings.getWebdavUsername());
		tfWebdavUser.textProperty().addListener((obs, oldText, newUsername) -> {
			Settings.setWebdavUsername(newUsername);
		});
		PasswordField tfWebdavPass = new PasswordField();
		tfWebdavPass.textProperty().addListener((obs, oldText, newPassword) -> {
			Settings.setWebdavPassword(newPassword);
		});
		tfWebdavPass.setText(Settings.getwebdavPassword());

		Button btConnect = new Button("Connect");

		//to display available calendars, but no idea how to retrieve this data
		//not used right now
//		ComboBox cbAddrBook = new ComboBox();
//		cbAddrBook.setDisable(true);
//		cbAddrBook.setItems(remoteAddrBooks);

		TextField tfAddrBook = new TextField(Settings.getWebdavAddressBook());  
		tfAddrBook.setDisable(true);
		tfAddrBook.textProperty().addListener((t, oldText, newText) -> {
			Settings.setWebdavAddressBook(newText);
		});
		
		Button btImport = new Button("Import");
		btImport.setDisable(true);

		connPane = new VBox();

		//styles
		connPane.setSpacing(4);
		connPane.widthProperty().addListener((ov, oldWidth, newWidth) -> {
			btConnect.setMinWidth((double) newWidth);
			btImport.setMinWidth((double) newWidth);
//			cbAddrBook.setMinWidth((double) newWidth);
		});

		//button-event-handler
		btConnect.setOnAction((t) -> {
			DaCardConv.LOGGER.info("connecting to CardDAV-Server");
			try {
				server = new CardDAVServer(tfWebdavUrl.getText(),
						tfWebdavUser.getText(), tfWebdavPass.getText());
				remoteAddrBooks.clear();
				remoteAddrBooks.addAll(server.getAddrBooks());
	//			cbAddrBook.setDisable(false); //TODO only on success
				btImport.setDisable(false); //TODO only on success
				tfAddrBook.setDisable(false); //TODO only on success
			} catch (MalformedURLException ex) {
				Dialog dialog = DaCardConv.getDialog("Remote Connection");
				dialog.setContentText(ex.getMessage());
				dialog.show();
				ex.printStackTrace();
			}
		});
		btImport.setOnAction((t) -> {
			DaCardConv.LOGGER.info("downloading address book from remote server");
			CardDAVImporterTask task = new CardDAVImporterTask(this.server, 
					tfAddrBook.getText(), app.getVcardsPane().getDataTableList());
			Thread th = new Thread(task);
			th.start();
		});

		connPane.getChildren().addAll(
				new Label("CardDAV Server URL:"), tfWebdavUrl,
				new Label("Username:"), tfWebdavUser,
				new Label("Password:"), tfWebdavPass,
				btConnect, new Separator(),
				new Label("Address Book Name:"), tfAddrBook, btImport);
	}

	public VBox getPane() {
		return connPane;
	}

}
