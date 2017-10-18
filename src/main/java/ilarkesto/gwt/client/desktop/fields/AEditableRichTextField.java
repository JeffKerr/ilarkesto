package ilarkesto.gwt.client.desktop.fields;

import ilarkesto.gwt.client.desktop.Widgets;

import java.util.Collections;
import java.util.List;

import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AEditableRichTextField extends AEditableField {

	private RichTextArea textArea;
	private RichTextToolbar toolbar;

	public abstract void applyValue(String value);

	protected abstract String getValue();

	@Override
	public boolean isValueSet() {
		return getValue() != null;
	}

	protected String prepareValue(String text) {
		return text;
	}

	protected final String prepareText(String text) {
		if (text == null) return null;
		text = text.trim();
		if (text.isEmpty()) return null;

		return text;
	}

	public void validateValue(String value) throws RuntimeException {
		if (value == null && isMandatory() && !isSubmittingEmptyMandatoryFieldAllowed())
			throw new RuntimeException("Eingabe erforderlich.");
	}

	@Override
	public void trySubmit() throws RuntimeException {
		String html = prepareText(toolbar.getHtml());
		String value = prepareValue(html);
		validateValue(value);
		applyValue(value);
	}

	@Override
	public Widget createEditorWidget() {
		textArea = new RichTextArea();
		textArea.getElement().setId(getId() + "_textArea");
		Style style = textArea.getElement().getStyle();
		style.setFontSize(100, Unit.PX);
		style.setProperty("minWidth", "1000px");
		style.setProperty("minHeight", "400px");
		style.setBackgroundColor("#fff");
		style.setBorderWidth(1, Unit.PX);
		style.setBorderStyle(BorderStyle.SOLID);
		style.setBorderColor("#aaa");
		textArea.addInitializeHandler(new InitializeHandler() {

			@Override
			public void onInitialize(InitializeEvent event) {
				// textArea.getFormatter().setFontName("OpenSans");
				// textArea.getFormatter().setFontSize(FontSize.X_SMALL);
				IFrameElement iFrame = IFrameElement.as(textArea.getElement());
				Document document = iFrame.getContentDocument();
				BodyElement body = document.getBody();
				body.setAttribute("style", "font-family: OpenSans, Arial, sans-serif;");
			}
		});
		style.setWidth(getTextBoxWidth(), Unit.PX);
		style.setHeight(100, Unit.PX);
		style.setPadding(Widgets.defaultSpacing, Unit.PX);

		String value = getValue();
		if (value == null) value = getAlternateValueIfValueIsNull();
		textArea.setHTML(value);

		if (getEditVetoMessage() == null) {
			textArea.addKeyUpHandler(new EnterKeyUpHandler());
		} else {
			textArea.setEnabled(false);
			textArea.setTitle(getEditVetoMessage());
		}

		toolbar = new RichTextToolbar(textArea, isToolbarHtmlExtrasEnabled(), getToolbarInlineImageUrlPrefix(),
				getToolbarInlineImages());

		VerticalPanel vp = new VerticalPanel();
		textArea.setWidth("100%");

		vp.add(toolbar);
		vp.add(textArea);

		return vp;
	}

	public String getToolbarInlineImageUrlPrefix() {
		return "";
	}

	private int getTextBoxWidth() {
		int width = Window.getClientWidth();
		if (width > 700) width = 700;
		return width;
	}

	@Override
	public IsWidget createDisplayWidget() {
		String html = getValue();

		HTML htmlWidget = new HTML();
		if (html == null) {
			html = getAlternateValueIfValueIsNull();
			htmlWidget.getElement().getStyle().setColor("#AAA");
		}

		htmlWidget.setHTML(html);
		// html.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
		return htmlWidget;
	}

	public String getAlternateValueIfValueIsNull() {
		return null;
	}

	public boolean isToolbarHtmlExtrasEnabled() {
		return false;
	}

	public List<String> getToolbarInlineImages() {
		return Collections.emptyList();
	}

	private class EnterKeyUpHandler implements KeyUpHandler {

		@Override
		public void onKeyUp(KeyUpEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && event.getNativeEvent().getCtrlKey()) {
				submitOrParentSubmit();
			}
		}

	}

}
