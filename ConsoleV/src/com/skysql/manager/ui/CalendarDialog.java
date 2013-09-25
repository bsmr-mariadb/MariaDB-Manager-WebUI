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

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class CalendarDialog implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Window dialogWindow;
	Button openButton;
	Button closebutton;
	Label explanation;

	private ClickListener dialogOpenListener = new ClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void buttonClick(Button.ClickEvent event) {

			dialogWindow = new ModalWindow("Backups", "680px");
			dialogWindow.setHeight("420px");
			UI.getCurrent().addWindow(dialogWindow);

			Calendar cal = new Calendar("Calendar");
			cal.setWidth("600px");
			cal.setHeight("300px");

			((ComponentContainer) dialogWindow.getContent()).addComponent(cal);
		}
	};

	public CalendarDialog(String label) {

		openButton = new Button(label, dialogOpenListener);

	}

	public CalendarDialog(String label, String mode) {

		this(label);

		// mode: month, week, day

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
