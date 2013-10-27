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
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class SettingsDialog implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	ModalWindow dialogWindow;
	Button openButton;
	Button closebutton;
	Label explanation;
	private TabSheet tabsheet;
	private String selectedTab;
	private boolean refresh = false;
	private boolean switchAllowed = true;
	SettingsDialog settingsDialog;

	private ClickListener settingsDialogOpenListener = new ClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void buttonClick(Button.ClickEvent event) {

			dialogWindow = new ModalWindow("Settings", "500px");
			dialogWindow.addCloseListener(settingsDialog);
			UI.getCurrent().addWindow(dialogWindow);

			tabsheet = new TabSheet();
			tabsheet.setImmediate(true);

			Tab tab;

			// General Tab
			GeneralSettings backupsTab = new GeneralSettings(settingsDialog);
			tab = tabsheet.addTab(backupsTab, "General");
			if (selectedTab != null && selectedTab.equals("General")) {
				tabsheet.setSelectedTab(tab);
			}

			// Users Tab
			UsersSettings usersTab = new UsersSettings();
			tab = tabsheet.addTab(usersTab, "Users");
			if (selectedTab != null && selectedTab.equals("Users")) {
				tabsheet.setSelectedTab(tab);
			}

			// Monitors Tab
			SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
			String systemID = systemInfo.getCurrentID();
			String systemType = null;
			if (systemID.equals(SystemInfo.SYSTEM_ROOT)) {
				ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
				if (clusterComponent != null) {
					systemType = clusterComponent.getSystemType();
					systemID = clusterComponent.getID();
				}
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

			// Handling tab changes
			tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
				private static final long serialVersionUID = -2358653511430014752L;

				Component selected = tabsheet.getSelectedTab();
				boolean preventEvent = false;

				public void selectedTabChange(SelectedTabChangeEvent event) {
					if (preventEvent) {
						preventEvent = false;
						return;
					}
					// Check the previous tab
					if (switchAllowed) {
						selected = tabsheet.getSelectedTab();
					} else {
						// Revert the tab change
						preventEvent = true; // Prevent secondary change event
						tabsheet.setSelectedTab(selected);
					}
				}
			});

		}
	};

	public SettingsDialog(String label) {

		openButton = new Button(label, settingsDialogOpenListener);
		settingsDialog = this;

	}

	public SettingsDialog(String label, String selectedTab) {

		this(label);
		this.selectedTab = selectedTab;

	}

	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	public void setClose(boolean close) {
		dialogWindow.setClose(close);
		switchAllowed = close;
		//dialogWindow.setClosable(close);
	}

	public Button getButton() {
		return (openButton);
	}

	public void windowClose(CloseEvent e) {
		if (refresh) {
			OverviewPanel overviewPanel = VaadinSession.getCurrent().getAttribute(OverviewPanel.class);
			overviewPanel.refresh();
		}

	}

}
