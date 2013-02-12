package com.skysql.consolev.ui;

import java.util.ArrayList;

import com.skysql.consolev.UserRecord;
import com.skysql.consolev.api.CreateUser;
import com.skysql.consolev.api.DeleteUser;
import com.skysql.consolev.api.UpdateUser;
import com.skysql.consolev.api.UserInfo;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.CloseEvent;


public class AccountSettings implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;
	
	private TextField userName = new TextField("User Name");
	private TextField newUserName = new TextField("New User Name");
	private PasswordField oldPassword = new PasswordField("Your Current Password");
	private PasswordField newPassword;
	private PasswordField newPassword2;
	private Button addUser = new Button("Add", this, "addUser");
	private Button removeUser = new Button("Remove", this, "removeUser");
	private Button changePassword = new Button("Change Password", this, "changePassword");
	private String currentUserID, selectedUserID;
	private ListSelect select;
	private Button okButton;
	private boolean firstPWValidation;
	
	private HorizontalLayout accountTab;
	private Window secondaryDialog;
	private ArrayList<UserRecord> userRecords;
	private UserInfo userInfo;
	private final String systemID;
	
	AccountSettings(final HorizontalLayout accountTab, final String systemID, String userID) {
		this.accountTab = accountTab;
		currentUserID = userID;
		this.systemID = systemID;

		accountTab.setWidth(Sizeable.SIZE_UNDEFINED, 0); // Default
		accountTab.setHeight(Sizeable.SIZE_UNDEFINED, 0); // Default
    	
		accountTab.addStyleName("accountTab");
		accountTab.setSpacing(true);

		VerticalLayout usersLayout = new VerticalLayout();
		accountTab.addComponent(usersLayout);
		usersLayout.setSpacing(true);
		usersLayout.setMargin(true);

		userInfo = new UserInfo("dummy");
    	userRecords = userInfo.getUsersList();
		
    	select = new ListSelect("Users");
    	select.setImmediate(true);
    	for (UserRecord user : userRecords) {
    	    String id = user.getID();
    	    String name = user.getName();
			select.addItem(name);
			if (id.equalsIgnoreCase(currentUserID)) {
				select.select(name);
				userName.setValue(name);
				selectedUserID = id;
			}
    	}
        select.setNullSelectionAllowed(false);
        select.setColumns(10);
		select.setRows(7);   // Show a few items and a scrollbar if there are more
		usersLayout.addComponent(select);

		select.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void valueChange(ValueChangeEvent event) {
    			String name = (String)event.getProperty().getValue();
    			if (name != null) {
    				selectedUserID = userInfo.findIDByName(name);
    				if (selectedUserID.equalsIgnoreCase(currentUserID)) {
    					removeUser.setEnabled(false);
    				} else {
    					removeUser.setEnabled(true);
    				}
					changePassword.setEnabled(true);
        			userName.setValue(name);
        			userName.setEnabled(true);

    			} else {
        			selectedUserID = null;
					removeUser.setEnabled(false);
					changePassword.setEnabled(false);
        			userName.setValue("");
        			userName.setEnabled(false);
    			}
				userName.removeAllValidators();
            }
        });

		HorizontalLayout userButtonsLayout = new HorizontalLayout();
		usersLayout.addComponent(userButtonsLayout);

    	userButtonsLayout.addComponent(addUser);
    	userButtonsLayout.setComponentAlignment(addUser, Alignment.MIDDLE_CENTER);    	

    	removeUser.setEnabled(false);
    	userButtonsLayout.addComponent(removeUser);
    	userButtonsLayout.setComponentAlignment(removeUser, Alignment.MIDDLE_CENTER);    	
    	
		VerticalLayout userLayout = new VerticalLayout();
		accountTab.addComponent(userLayout);
		userLayout.setSpacing(true);
		userLayout.setMargin(true);
    	
		userLayout.addComponent(userName);
		userName.setImmediate(true);
		userName.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void valueChange(ValueChangeEvent event) {
    			String newUserName = (String)event.getProperty().getValue();
    			String userID = userInfo.findIDByName(newUserName);
    			if (userID != null && !userID.equalsIgnoreCase(currentUserID)) {
    				// duplicate name is not allowed
    				Validator userNameValidator = new UserNameValidator();
    				userName.addValidator(userNameValidator);
    			} else if (userID == null && !newUserName.equalsIgnoreCase("")) {
                	String name = (String)userName.getValue();
                	UpdateUser newUser = new UpdateUser(systemID, selectedUserID, name, null);
    				userName.removeAllValidators();
    				String oldName = userInfo.findNameByID(selectedUserID);
    				UserRecord user = userInfo.findRecordByID(selectedUserID);
    				user.setName(newUserName);
                	select.removeItem(oldName);
                	select.addItem(newUserName);
                	select.select(newUserName);
    			}
            }
        });
	
    	userLayout.addComponent(changePassword);
    	userLayout.setComponentAlignment(changePassword, Alignment.MIDDLE_CENTER);
  	
	}
    
	public void windowClose(CloseEvent e) {
    	newUserName.setValue("");
    	oldPassword.setValue("");
    	if (newPassword != null)
    		newPassword.setValue("");
    	if (newPassword2 != null)
    		newPassword2.setValue("");
    }

    public void addUser(Button.ClickEvent event) {
    	secondaryDialog = new DialogWindow("Add User");
    	accountTab.getWindow().getParent().addWindow(secondaryDialog);
    	secondaryDialog.addListener(this);
   	
        final Form form = new Form();
        //form.setImmediate(true);
    	form.setDescription("Create a new administrator user.");
		form.addField("newUserName", newUserName);
		form.getField("newUserName").setRequired(true);
		form.getField("newUserName").setRequiredError("User Name is missing");
		newUserName.focus();
		newPassword = new PasswordField("New Password");
		newPassword2 = new PasswordField("Verify Password");
        form.addField("newPassword", newPassword);
        form.addField("newPassword2", newPassword2);
        newPassword2.setImmediate(true);
        newPassword2.addListener(new ValueChangeListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
	        public void valueChange(ValueChangeEvent event) {
	        	okButton.focus();
	        }
	    });

        HorizontalLayout buttonsBar = new HorizontalLayout();
        buttonsBar.setSpacing(true);
        buttonsBar.setHeight("25px"); 
        form.getFooter().addComponent(buttonsBar);
        
        Button cancelButton = new Button("Cancel");
        buttonsBar.addComponent(cancelButton);
        cancelButton.addListener(new Button.ClickListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void buttonClick(ClickEvent event) {
            	form.discard();
            	accountTab.getWindow().getParent().removeWindow(secondaryDialog);
            } 
        });
        
        okButton = new Button("Create User"); 
        okButton.addListener(new Button.ClickListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void buttonClick(ClickEvent event) {
                try {
                	form.setComponentError(null);
                	firstPWValidation = false;
                	form.commit();
                	String name = (String)newUserName.getValue();
                	String password = (String)newPassword.getValue();
                	CreateUser newUser = new CreateUser(systemID, name, password);
                	userRecords.add(new UserRecord(newUser.getUserID(), name));
                	select.addItem(name);
                } catch (Exception e) {
                	return;
                }
            	accountTab.getWindow().getParent().removeWindow(secondaryDialog);
            } 
        });
        buttonsBar.addComponent(okButton); 
        buttonsBar.setComponentAlignment(okButton, Alignment.TOP_RIGHT); 
        
        secondaryDialog.addComponent(form);
        
        // attach the validators
        Validator userNameValidator = new UserNameValidator();
        newUserName.addValidator(userNameValidator);

        Validator passwordValidator = new Password1Validator();
        newPassword.addValidator(passwordValidator);

        Validator password2Validator = new Password2Validator();
        newPassword2.addValidator(password2Validator);
        
	}

    public void removeUser(Button.ClickEvent event) {
    	secondaryDialog = new DialogWindow("Remove User");
    	accountTab.getWindow().getParent().addWindow(secondaryDialog);
    	secondaryDialog.addListener(this);

        final Form form = new Form();
        //form.setImmediate(true);
    	form.setDescription("Remove user " + userName.getValue() + " from the system.");

		{
	        Label spacer = new Label();
	        spacer.setWidth("20px");
	        form.getLayout().addComponent(spacer);
		}

        HorizontalLayout buttonsBar = new HorizontalLayout();
        buttonsBar.setSpacing(true);
        buttonsBar.setHeight("25px"); 
        form.getFooter().addComponent(buttonsBar);
        
        Button cancelButton = new Button("Cancel");
        buttonsBar.addComponent(cancelButton);
        cancelButton.addListener(new Button.ClickListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void buttonClick(ClickEvent event) {
            	form.discard();
            	accountTab.getWindow().getParent().removeWindow(secondaryDialog);
            } 
        });
        
        okButton = new Button("Remove User"); 
    	okButton.setImmediate(true);
    	okButton.focus();
        okButton.addListener(new Button.ClickListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void buttonClick(ClickEvent event) {
                try {
                	form.setComponentError(null);
                	form.commit();
                	DeleteUser goneUser = new DeleteUser(systemID, selectedUserID);
                	userRecords.remove(userInfo.findRecordByID(selectedUserID));
                	String name = (String)userName.getValue();
                	select.removeItem(name);
                } catch (Exception e) {
                	return;
                }
            	accountTab.getWindow().getParent().removeWindow(secondaryDialog);
            } 
        });
        buttonsBar.addComponent(okButton); 
        buttonsBar.setComponentAlignment(okButton, Alignment.TOP_RIGHT); 
        
        secondaryDialog.addComponent(form);

	
	}

	public void changePassword() {
    	secondaryDialog = new DialogWindow("Change Password");
    	accountTab.getWindow().getParent().addWindow(secondaryDialog);
    	secondaryDialog.addListener(this);
    	
        final Form form = new Form();
    	form.setDescription("Change Password for user " + userName.getValue() + ".");
		//form.addField("oldPassword", oldPassword);
		//oldPassword.focus();
		newPassword = new PasswordField((currentUserID.equalsIgnoreCase(selectedUserID)) ? "New Password" : "New Password for " + userName.getValue());
		newPassword2 = new PasswordField((currentUserID.equalsIgnoreCase(selectedUserID)) ? "Verify Password" : "Verify Password for " + userName.getValue());
		form.addField("newPassword", newPassword);
        form.addField("newPassword2", newPassword2);
        newPassword.focus();
        newPassword2.setImmediate(true);
        newPassword2.addListener(new ValueChangeListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
	        public void valueChange(ValueChangeEvent event) {
	        	okButton.focus();
	        }
	    });

        HorizontalLayout buttonsBar = new HorizontalLayout();
        buttonsBar.setSpacing(true);
        buttonsBar.setHeight("25px"); 
        form.getFooter().addComponent(buttonsBar);
        
        Button cancelButton = new Button("Cancel");
        buttonsBar.addComponent(cancelButton);
        cancelButton.addListener(new Button.ClickListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void buttonClick(ClickEvent event) {
            	form.discard();
            	accountTab.getWindow().getParent().removeWindow(secondaryDialog);
            } 
        });
        
        okButton = new Button("Change Password"); 
        okButton.addListener(new Button.ClickListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void buttonClick(ClickEvent event) {
                try {
                	form.setComponentError(null);
                	firstPWValidation = false;
                	form.commit();
                	String password = (String)newPassword.getValue();
                	UpdateUser newUser = new UpdateUser(systemID, selectedUserID, null, password);
                } catch (Exception e) {
                	return;
                }
            	accountTab.getWindow().getParent().removeWindow(secondaryDialog);
            } 
        });
        buttonsBar.addComponent(okButton); 
        buttonsBar.setComponentAlignment(okButton, Alignment.TOP_RIGHT); 
        
        secondaryDialog.addComponent(form);

        // attach the validators
        Validator passwordValidator = new Password1Validator();
        newPassword.addValidator(passwordValidator);

        Validator password2Validator = new Password2Validator();
        newPassword2.addValidator(password2Validator);
	
	}
	
	class UserNameValidator implements Validator {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

        public boolean isValid(Object value) {
            if (value == null || !(value instanceof String)) {
                return false;
            }
            return (userInfo.findIDByName((String)value) == null);
        }

        // Upon failure, the validate() method throws an exception
        public void validate(Object value) throws InvalidValueException {
            if (!isValid(value)) {
            	throw new InvalidValueException("User Name already exists.");
            } else {

            }
        }
    }

	class Password1Validator implements Validator {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

        public boolean isValid(Object value) {
            if (value == null || !(value instanceof String)) {
                return false;
            }
            return ((String)value).matches((String)newPassword2.getValue());
        }

        // Upon failure, the validate() method throws an exception
        public void validate(Object value) throws InvalidValueException {
            if (!isValid(value)) {
            	firstPWValidation = true;
            	throw new InvalidValueException("Passwords do not match.");
            } else {
            	firstPWValidation = false;
            }
        }
    }

	class Password2Validator implements Validator {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

        public boolean isValid(Object value) {
            if (value == null || !(value instanceof String)) {
                return false;
            }
            return ((String)value).matches((String)newPassword.getValue());
        }

        // Upon failure, the validate() method throws an exception
        public void validate(Object value) throws InvalidValueException {
            if (!isValid(value) && !firstPWValidation) {
            	throw new InvalidValueException("Passwords do not match.");
            }
        }
    }
}

