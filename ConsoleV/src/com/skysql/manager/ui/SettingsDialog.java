/*
 * This file is distributed asimport com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
SS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright 2012-2013 SkySQL Ab
 */

package com.skysql.manager.ui;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.api.SystemInfo;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class SettingsDialog implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	Window dialogWindow;
	Button openButton;
	Button closebutton;
	Label explanation;
	private TabSheet tabsheet;
	private String selectedTab;

	private ClickListener settingsDialogOpenListener = new ClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void buttonClick(Button.ClickEvent event) {

			dialogWindow = new DialogWindow("Settings");
			dialogWindow.setWidth("500px");
			UI.getCurrent().addWindow(dialogWindow);

			tabsheet = new TabSheet();
			tabsheet.setImmediate(true);

			// Account Tab
			AccountSettings accountTab = new AccountSettings();
			Tab tab = tabsheet.addTab(accountTab, "Accounts");
			if (selectedTab != null && selectedTab.equals("Accounts")) {
				tabsheet.setSelectedTab(tab);
			}

			// Backups Tab
			BackupSettings backupsTab = new BackupSettings();
			tab = tabsheet.addTab(backupsTab, "Backups");
			if (selectedTab != null && selectedTab.equals("Backups")) {
				tabsheet.setSelectedTab(tab);
			}

			// Monitors Tab
			SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
			String systemID = systemInfo.getCurrentID();
			String systemType;
			if (systemID.equals(SystemInfo.SYSTEM_ROOT)) {
				ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
				systemType = clusterComponent.getSystemType();
			} else {
				systemType = systemInfo.getCurrentSystem().getSystemType();
			}
			if (systemType != null) {
				MonitorsSettings monitorsTab = new MonitorsSettings(systemID, systemType);
				tab = tabsheet.addTab(monitorsTab, "Monitors");
				if (selectedTab != null && selectedTab.equals("Monitors")) {
					tabsheet.setSelectedTab(tab);
				}
			}

			((ComponentContainer) dialogWindow.getContent()).addComponent(tabsheet);
		}
	};

	public SettingsDialog(String label) {

		openButton = new Button(label, settingsDialogOpenListener);

	}

	public SettingsDialog(String label, String selectedTab) {

		this(label);
		this.selectedTab = selectedTab;

	}

	public Button getButton() {
		return (openButton);
	}

	/** Handle Close button click and close the window. */
	public void closeButtonClick(Button.ClickEvent event) {
		/* Windows are managed by the application object. */
		dialogWindow.close();
	}

	/** In case the window is closed otherwise. */
	public void windowClose(CloseEvent e) {
		// anything special goes here
	}
}

class DialogWindow extends Window {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public DialogWindow(String caption) {
		setModal(true);
		center();
		setCaption(caption);
		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);
	}
}
