package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.actions.oauth.JavaScriptValidationError;
import com.eviware.soapui.impl.rest.actions.oauth.JavaScriptValidator;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Component that allows a user to edit the JavaScript snippets associated with an OAuth 2 flow.
 */
public class OAuth2ScriptsEditor extends JPanel
{
	static final String[] SCRIPT_NAMES = { "Login screen script", "Consent screen script"};
	public static final String TEST_SCRIPTS_BUTTON_NAME = "testScriptsButton";

	private List<RSyntaxTextArea> scriptFields = new ArrayList<RSyntaxTextArea>(  );
	private JavaScriptValidator javaScriptValidator = new JavaScriptValidator();

	public OAuth2ScriptsEditor( OAuth2Profile profile )
	{
		super( new BorderLayout(  ));
		JPanel buttonPanel = new JPanel(  );
		JButton testScriptsButton = new JButton( "Test scripts" );
		testScriptsButton.setName( TEST_SCRIPTS_BUTTON_NAME );
		testScriptsButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				testScripts();
			}
		} );
		buttonPanel.add( testScriptsButton);
		add(buttonPanel, BorderLayout.NORTH);
		add( makeScriptsPanel( profile.getJavaScripts() ), BorderLayout.CENTER );
	}

	private void testScripts()
	{
		for( RSyntaxTextArea scriptField : scriptFields )
		{
			String script = scriptField.getText();
			JavaScriptValidationError validate = javaScriptValidator.validate( script );
			if( validate != null )
			{
				UISupport.showErrorMessage( "Validation failed for script [" + script + "]: " + validate.getErrorMessage());
			}
		}
	}

	public List<String> getJavaScripts()
	{
		List<String> scripts = new ArrayList<String>(  );
		for( RSyntaxTextArea scriptField : scriptFields )
		{
			scripts.add(scriptField.getText());
		}
		return scripts;
	}

	private JPanel makeScriptsPanel( List<String> currentScripts )
	{
		JPanel scriptsPanel = new JPanel( new GridLayout( 2, 1 ) );
		int index = 0;
		for( String scriptName : SCRIPT_NAMES )
		{
			RSyntaxTextArea scriptField = SyntaxEditorUtil.createDefaultJavaScriptSyntaxTextArea();
			scriptField.setName(scriptName);
			if (currentScripts.size() > index)
			{
				scriptField.setText(currentScripts.get(index));
			}
			scriptFields.add(scriptField);
			scriptsPanel.add( new InputAreaWithHeader( scriptName, scriptField ) );
			index++;
		}
		return scriptsPanel;
	}

	private boolean hasInvalidJavaScripts()
	{
		for( RSyntaxTextArea scriptField : scriptFields )
		{
			if( javaScriptValidator.validate( scriptField.getText()  ) != null )
			{
				return true;
			}
		}
		return false;
	}

	private class InputAreaWithHeader extends JPanel
	{
		public InputAreaWithHeader( String scriptName, RSyntaxTextArea scriptField )
		{
			super(new BorderLayout(  ));
			add( new JLabel(scriptName), BorderLayout.NORTH);
			add( new JScrollPane(scriptField), BorderLayout.CENTER);
		}
	}

	public static class Dialog extends JDialog
	{

		static final String OK_BUTTON_NAME = "okButton";

		private List<String> scriptsToReturn;



		public Dialog( Frame owner, String title, OAuth2Profile profile)
		{
			super( owner, title, true );
			Container contentPane = getContentPane();
			final OAuth2ScriptsEditor inputPanel = new OAuth2ScriptsEditor( profile );
			contentPane.setLayout( new BorderLayout(  ) );
			contentPane.add(inputPanel, BorderLayout.CENTER);
			JPanel buttonsPanel = new JPanel(new FlowLayout( FlowLayout.RIGHT ));
			JButton okButton = new JButton( "OK" );
			okButton.setName( OK_BUTTON_NAME );
			okButton.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( ActionEvent e )
				{
					if (inputPanel.hasInvalidJavaScripts() && !UISupport.confirm(
							"One or more of the entered scripts you've entered seems to be incorrect.\n\n" +
									"Do you still want to apply it?", "Incorrect JavaScript", Dialog.this ))
					{
						return;
					}
					scriptsToReturn = inputPanel.getJavaScripts();
					closeDialog();
				}
			} );
			JButton cancelButton = new JButton( "Cancel" );
			cancelButton.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( ActionEvent e )
				{
					scriptsToReturn = null;
					closeDialog();
				}
			} );
			buttonsPanel.add( okButton );
			buttonsPanel.add(cancelButton);
			contentPane.add(buttonsPanel, BorderLayout.SOUTH);
			setBounds(500, 500, 600, 500);
		}

		public List<String> getScripts()
		{
			return scriptsToReturn;
		}

		private void closeDialog()
		{
			setVisible( false );
			dispose();
		}

		public Dialog(OAuth2Profile profile)
		{
			this(null, "OAuth2 flow JavaScripts", profile);
		}
	}


}
