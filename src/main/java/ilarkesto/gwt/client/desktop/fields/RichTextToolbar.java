/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ilarkesto.gwt.client.desktop.fields;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.RichTextArea.Formatter;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RichTextToolbar extends Composite {

	/** Local CONSTANTS **/
	// ImageMap and CSS related
	// private static final String HTTP_STATIC_ICONS_GIF =
	// "http://blog.elitecoderz.net/wp-includes/js/tinymce/themes/advanced/img/icons.gif";
	private static final String CSS_ROOT_NAME = "RichTextToolbar";

	// Color and Fontlists - First Value (key) is the Name to display, Second Value (value) is the
	// HTML-Definition
	public final static HashMap<String, String> GUI_COLORLIST = new HashMap<String, String>();
	static {
		GUI_COLORLIST.put("weiss", "#FFFFFF");
		GUI_COLORLIST.put("schwarz", "#000000");
		GUI_COLORLIST.put("rot", "red");
		GUI_COLORLIST.put("grün", "green");
		GUI_COLORLIST.put("gelb", "yellow");
		GUI_COLORLIST.put("blau", "blue");
	}
	public final static HashMap<String, String> GUI_FONTLIST = new HashMap<String, String>();
	static {
		GUI_FONTLIST.put("Times New Roman", "Times New Roman");
		GUI_FONTLIST.put("Arial", "Arial");
		GUI_FONTLIST.put("Courier New", "Courier New");
		GUI_FONTLIST.put("Georgia", "Georgia");
		GUI_FONTLIST.put("Trebuchet", "Trebuchet");
		GUI_FONTLIST.put("Verdana", "Verdana");
	}

	// HTML Related (styles made by SPAN and DIV)
	private static final String HTML_STYLE_CLOSE_SPAN = "</span>";
	private static final String HTML_STYLE_CLOSE_DIV = "</div>\n";
	private static final String HTML_STYLE_OPEN_BOLD = "<span style=\"font-weight: bold;\">";
	private static final String HTML_STYLE_OPEN_ITALIC = "<span style=\"font-weight: italic;\">";
	private static final String HTML_STYLE_OPEN_UNDERLINE = "<span style=\"font-weight: underline;\">";
	private static final String HTML_STYLE_OPEN_LINETHROUGH = "<span style=\"font-weight: line-through;\">";
	private static final String HTML_STYLE_OPEN_ALIGNLEFT = "\n<div style=\"text-align: left;\">";
	private static final String HTML_STYLE_OPEN_ALIGNCENTER = "\n<div style=\"text-align: center;\">";
	private static final String HTML_STYLE_OPEN_ALIGNRIGHT = "\n<div style=\"text-align: right;\">";
	private static final String HTML_STYLE_OPEN_INDENTRIGHT = "\n<div style=\"margin-left: 40px;\">";

	// HTML Related (styles made by custom HTML-Tags)
	private static final String HTML_STYLE_OPEN_SUBSCRIPT = "<sub>";
	private static final String HTML_STYLE_CLOSE_SUBSCRIPT = "</sub>";
	private static final String HTML_STYLE_OPEN_SUPERSCRIPT = "<sup>";
	private static final String HTML_STYLE_CLOSE_SUPERSCRIPT = "</sup>";
	private static final String HTML_STYLE_OPEN_ORDERLIST = "\n<ol>\n  <li>";
	private static final String HTML_STYLE_CLOSE_ORDERLIST = "</li></ol>\n";
	private static final String HTML_STYLE_OPEN_UNORDERLIST = "\n<ul><li>";
	private static final String HTML_STYLE_CLOSE_UNORDERLIST = "</li></ul>\n";

	// HTML Related (styles without closing Tag)
	private static final String HTML_STYLE_HLINE = "\n<hr>\n";

	// GUI Related stuff
	private static final String GUI_DIALOG_INSERTURL = "Bitte ULR eingeben:";
	private static final String GUI_DIALOG_IMAGEURL = "Bitte Bild-URL eingeben:";

	private static final String GUI_LISTNAME_COLORS = "Farbe";
	private static final String GUI_LISTNAME_INLINEIMAGES = "Bilder";
	private static final String GUI_LISTNAME_FONTS = "Schrift";

	private static final String GUI_HOVERTEXT_SWITCHVIEW = "Switch View HTML/Source";
	private static final String GUI_HOVERTEXT_REMOVEFORMAT = "Formatierung entfernen";
	private static final String GUI_HOVERTEXT_IMAGE = "Insert Image";
	private static final String GUI_HOVERTEXT_HLINE = "Insert Horizontal Line";
	private static final String GUI_HOVERTEXT_BREAKLINK = "Break Link";
	private static final String GUI_HOVERTEXT_LINK = "Link erstellen";
	private static final String GUI_HOVERTEXT_IDENTLEFT = "Links einrücken";
	private static final String GUI_HOVERTEXT_IDENTRIGHT = "Richts einrücken";
	private static final String GUI_HOVERTEXT_UNORDERLIST = "Aufzählung";
	private static final String GUI_HOVERTEXT_ORDERLIST = "Nummerierte Aufzählung";
	private static final String GUI_HOVERTEXT_ALIGNRIGHT = "Align Right";
	private static final String GUI_HOVERTEXT_ALIGNCENTER = "Align Center";
	private static final String GUI_HOVERTEXT_ALIGNLEFT = "Align Left";
	private static final String GUI_HOVERTEXT_SUPERSCRIPT = "Superscript";
	private static final String GUI_HOVERTEXT_SUBSCRIPT = "Subscript";
	private static final String GUI_HOVERTEXT_STROKE = "Durchgestrichen";
	private static final String GUI_HOVERTEXT_UNDERLINE = "Unterstrichen";
	private static final String GUI_HOVERTEXT_ITALIC = "Kursiv";
	private static final String GUI_HOVERTEXT_BOLD = "Fett";

	/** Private Variables **/
	// The main (Vertical)-Panel and the two inner (Horizontal)-Panels
	private VerticalPanel outer;
	private HorizontalPanel topPanel;
	// private HorizontalPanel bottomPanel;h

	// The RichTextArea this Toolbar referes to and the Interfaces to access the RichTextArea
	private RichTextArea styleText;
	private Formatter styleTextFormatter;

	// We use an internal class of the ClickHandler and the KeyUpHandler to be private to others with these
	// events
	private EventHandler evHandler;

	// The Buttons of the Menubar
	private ToggleButton bold;
	private ToggleButton italic;
	private ToggleButton underline;
	private ToggleButton stroke;
	// private ToggleButton subscript;
	// private ToggleButton superscript;
	// private PushButton alignleft;
	// private PushButton alignmiddle;
	// private PushButton alignright;
	private PushButton orderlist;
	private PushButton unorderlist;
	private PushButton indentleft;
	private PushButton indentright;
	private PushButton generatelink;
	// private PushButton breaklink;
	// private PushButton insertline;
	// private PushButton insertimage;
	// private PushButton removeformatting;
	private ToggleButton toggleTextSource;

	// private ListBox fontlist;
	private ListBox colorlist;

	private ListBox inlineimageslist;

	private boolean extrasEnabled = false;
	private String inlineImageUrlPrefix = "";
	private List<String> inlineImages = Collections.emptyList();

	public RichTextToolbar(RichTextArea richtext, boolean extrasEnabled, String inlineImageUrlPrefix,
			List<String> inlineImages) {
		this.extrasEnabled = extrasEnabled;
		this.inlineImageUrlPrefix = inlineImageUrlPrefix;
		this.inlineImages = inlineImages;

		// Initialize the main-panel
		outer = new VerticalPanel();

		// Initialize the two inner panels
		topPanel = new HorizontalPanel();
		// bottomPanel = new HorizontalPanel();
		topPanel.setStyleName(CSS_ROOT_NAME);
		// bottomPanel.setStyleName(CSS_ROOT_NAME);

		// Save the reference to the RichText area we refer to and get the interfaces to the stylings

		styleText = richtext;
		styleTextFormatter = styleText.getFormatter();

		// Set some graphical options, so this toolbar looks how we like it.
		topPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
		// bottomPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);

		// Add the two inner panels to the main panel
		outer.add(topPanel);
		// outer.add(bottomPanel);

		// Some graphical stuff to the main panel and the initialisation of the new widget
		outer.setWidth("100%");
		outer.setStyleName(CSS_ROOT_NAME);
		initWidget(outer);

		//
		evHandler = new EventHandler();

		// Add KeyUp and Click-Handler to the RichText, so that we can actualize the toolbar if neccessary
		styleText.addKeyUpHandler(evHandler);
		styleText.addClickHandler(evHandler);

		// Now lets fill the new toolbar with life
		buildTools();

	}

	public String getHtml() {
		if (toggleTextSource.isDown()) return styleText.getText();
		return beautifyHtml(styleText.getHTML());
	}

	/** Click Handler of the Toolbar **/
	private class EventHandler implements ClickHandler, KeyUpHandler, ChangeHandler {

		@Override
		public void onClick(ClickEvent event) {
			if (event.getSource().equals(bold)) {
				if (isSourceMode()) {
					changeHtmlStyle(HTML_STYLE_OPEN_BOLD, HTML_STYLE_CLOSE_SPAN);
				} else {
					styleTextFormatter.toggleBold();
				}
			} else if (event.getSource().equals(italic)) {
				if (isSourceMode()) {
					changeHtmlStyle(HTML_STYLE_OPEN_ITALIC, HTML_STYLE_CLOSE_SPAN);
				} else {
					styleTextFormatter.toggleItalic();
				}
			} else if (event.getSource().equals(underline)) {
				if (isSourceMode()) {
					changeHtmlStyle(HTML_STYLE_OPEN_UNDERLINE, HTML_STYLE_CLOSE_SPAN);
				} else {
					styleTextFormatter.toggleUnderline();
				}
			} else if (event.getSource().equals(stroke)) {
				if (isSourceMode()) {
					changeHtmlStyle(HTML_STYLE_OPEN_LINETHROUGH, HTML_STYLE_CLOSE_SPAN);
				} else {
					styleTextFormatter.toggleStrikethrough();
				}
				// } else if (event.getSource().equals(subscript)) {
				// if (isHTMLMode()) {
				// changeHtmlStyle(HTML_STYLE_OPEN_SUBSCRIPT, HTML_STYLE_CLOSE_SUBSCRIPT);
				// } else {
				// styleTextFormatter.toggleSubscript();
				// }
				// } else if (event.getSource().equals(superscript)) {
				// if (isHTMLMode()) {
				// changeHtmlStyle(HTML_STYLE_OPEN_SUPERSCRIPT, HTML_STYLE_CLOSE_SUPERSCRIPT);
				// } else {
				// styleTextFormatter.toggleSuperscript();
				// }
				// } else if (event.getSource().equals(alignleft)) {
				// if (isHTMLMode()) {
				// changeHtmlStyle(HTML_STYLE_OPEN_ALIGNLEFT, HTML_STYLE_CLOSE_DIV);
				// } else {
				// styleTextFormatter.setJustification(RichTextArea.Justification.LEFT);
				// }
				// } else if (event.getSource().equals(alignmiddle)) {
				// if (isHTMLMode()) {
				// changeHtmlStyle(HTML_STYLE_OPEN_ALIGNCENTER, HTML_STYLE_CLOSE_DIV);
				// } else {
				// styleTextFormatter.setJustification(RichTextArea.Justification.CENTER);
				// }
				// } else if (event.getSource().equals(alignright)) {
				// if (isHTMLMode()) {
				// changeHtmlStyle(HTML_STYLE_OPEN_ALIGNRIGHT, HTML_STYLE_CLOSE_DIV);
				// } else {
				// styleTextFormatter.setJustification(RichTextArea.Justification.RIGHT);
				// }
			} else if (event.getSource().equals(orderlist)) {
				if (isSourceMode()) {
					changeHtmlStyle(HTML_STYLE_OPEN_ORDERLIST, HTML_STYLE_CLOSE_ORDERLIST);
				} else {
					styleTextFormatter.insertOrderedList();
				}
			} else if (event.getSource().equals(unorderlist)) {
				if (isSourceMode()) {
					changeHtmlStyle(HTML_STYLE_OPEN_UNORDERLIST, HTML_STYLE_CLOSE_UNORDERLIST);
				} else {
					styleTextFormatter.insertUnorderedList();
				}
			} else if (event.getSource().equals(indentright)) {
				if (isSourceMode()) {
					changeHtmlStyle(HTML_STYLE_OPEN_INDENTRIGHT, HTML_STYLE_CLOSE_DIV);
				} else {
					styleTextFormatter.rightIndent();
				}
			} else if (event.getSource().equals(indentleft)) {
				if (isSourceMode()) {
					// TODO nothing can be done here at the moment
				} else {
					styleTextFormatter.leftIndent();
				}
			} else if (event.getSource().equals(generatelink)) {
				String url = Window.prompt(GUI_DIALOG_INSERTURL, "http://");
				if (url != null) {
					if (isSourceMode()) {
						changeHtmlStyle("<a href=\"" + url + "\">", "</a>");
					} else {
						styleTextFormatter.createLink(url);
					}
				}
				// } else if (event.getSource().equals(breaklink)) {
				// if (isHTMLMode()) {
				// // TODO nothing can be done here at the moment
				// } else {
				// styleTextFormatter.removeLink();
				// }
				// } else if (event.getSource().equals(insertimage)) {
				// String url = Window.prompt(GUI_DIALOG_IMAGEURL, "http://");
				// if (url != null) {
				// if (isHTMLMode()) {
				// changeHtmlStyle("<img src=\"" + url + "\">", "");
				// } else {
				// styleTextFormatter.insertImage(url);
				// }
				// }
				// } else if (event.getSource().equals(insertline)) {
				// if (isHTMLMode()) {
				// changeHtmlStyle(HTML_STYLE_HLINE, "");
				// } else {
				// styleTextFormatter.insertHorizontalRule();
				// }
				// } else if (event.getSource().equals(removeformatting)) {
				// if (isHTMLMode()) {
				// // TODO nothing can be done here at the moment
				// } else {
				// styleTextFormatter.removeFormat();
				// }
			} else if (event.getSource().equals(toggleTextSource)) {
				if (toggleTextSource.isDown()) {
					String html = beautifyHtml(styleText.getHTML());
					styleText.setText(html);
					styleTextFormatter.selectAll();
					styleTextFormatter.setFontName("monospace");
				} else {
					styleText.setHTML(styleText.getText());
				}
			} else if (event.getSource().equals(styleText)) {
				// Change invoked by the richtextArea
			}
			updateStatus();
		}

		@Override
		public void onKeyUp(KeyUpEvent event) {
			updateStatus();
		}

		@Override
		public void onChange(ChangeEvent event) {
			// if (event.getSource().equals(fontlist)) {
			// if (isHTMLMode()) {
			// changeHtmlStyle("<span style=\"font-family: " + fontlist.getValue(fontlist.getSelectedIndex())
			// + ";\">", HTML_STYLE_CLOSE_SPAN);
			// } else {
			// styleTextFormatter.setFontName(fontlist.getValue(fontlist.getSelectedIndex()));
			// }
			// } else
			if (event.getSource().equals(colorlist)) {
				if (isSourceMode()) {
					changeHtmlStyle("<span style=\"color: " + colorlist.getValue(colorlist.getSelectedIndex()) + ";\">",
						HTML_STYLE_CLOSE_SPAN);
				} else {
					styleTextFormatter.setForeColor(colorlist.getValue(colorlist.getSelectedIndex()));
				}
			} else if (event.getSource().equals(inlineimageslist)) {
				String name = inlineimageslist.getValue(inlineimageslist.getSelectedIndex());
				String url = inlineImageUrlPrefix + name;
				if (isSourceMode()) {
					changeHtmlStyle("<img src=\"" + name + "\" alt=\"" + url + "\">", "");
				} else {
					styleTextFormatter.insertImage(url);
				}
			}
		}
	}

	/** Native JavaScript that returns the selected text and position of the start **/
	public static native JsArrayString getSelection(Element elem) /*-{
																	var txt = "";
																	var pos = 0;
																	var range;
																	var parentElement;
																	var container;

																	if (elem.contentWindow.getSelection) {
																	txt = elem.contentWindow.getSelection();
																	pos = elem.contentWindow.getSelection().getRangeAt(0).startOffset;
																	} else if (elem.contentWindow.document.getSelection) {
																	txt = elem.contentWindow.document.getSelection();
																	pos = elem.contentWindow.document.getSelection().getRangeAt(0).startOffset;
																	} else if (elem.contentWindow.document.selection) {
																	range = elem.contentWindow.document.selection.createRange();
																	txt = range.text;
																	parentElement = range.parentElement();
																	container = range.duplicate();
																	container.moveToElementText(parentElement);
																	container.setEndPoint('EndToEnd', range);
																	pos = container.text.length - range.text.length;
																	}
																	return [""+txt,""+pos];
																	}-*/;

	/** Method called to toggle the style in HTML-Mode **/
	private void changeHtmlStyle(String startTag, String stopTag) {
		JsArrayString tx = getSelection(styleText.getElement());
		String txbuffer = styleText.getText();
		Integer startpos = Integer.parseInt(tx.get(1));
		String selectedText = tx.get(0);
		styleText.setText(txbuffer.substring(0, startpos) + startTag + selectedText + stopTag
				+ txbuffer.substring(startpos + selectedText.length()));
	}

	/** Private method with a more understandable name to get if HTML mode is on or not **/
	private boolean isSourceMode() {
		return toggleTextSource.isDown();
	}

	/** Private method to set the toggle buttons and disable/enable buttons which do not work in html-mode **/
	private void updateStatus() {
		if (styleTextFormatter != null) {
			bold.setDown(styleTextFormatter.isBold());
			italic.setDown(styleTextFormatter.isItalic());
			underline.setDown(styleTextFormatter.isUnderlined());
			// subscript.setDown(styleTextFormatter.isSubscript());
			// superscript.setDown(styleTextFormatter.isSuperscript());
			stroke.setDown(styleTextFormatter.isStrikethrough());
		}

		boolean sourceMode = isSourceMode();
		bold.setEnabled(!sourceMode);
		italic.setEnabled(!sourceMode);
		underline.setEnabled(!sourceMode);
		stroke.setEnabled(!sourceMode);
		colorlist.setEnabled(!sourceMode);
		inlineimageslist.setEnabled(!sourceMode);
		orderlist.setEnabled(!sourceMode);
		unorderlist.setEnabled(!sourceMode);
		indentleft.setEnabled(!sourceMode);
		indentright.setEnabled(!sourceMode);
		generatelink.setEnabled(!sourceMode);
		// removeformatting.setEnabled(!htmlMode);
	}

	/** Initialize the options on the toolbar **/
	private void buildTools() {
		// Init the TOP Panel forst
		topPanel.add(bold = createToggleButton("bold", GUI_HOVERTEXT_BOLD));
		topPanel.add(italic = createToggleButton("italic", GUI_HOVERTEXT_ITALIC));
		topPanel.add(underline = createToggleButton("underline", GUI_HOVERTEXT_UNDERLINE));
		topPanel.add(stroke = createToggleButton("stroke", GUI_HOVERTEXT_STROKE));
		topPanel.add(colorlist = createColorList());

		// topPanel.add(subscript = createToggleButton("subscript", GUI_HOVERTEXT_SUBSCRIPT));
		// topPanel.add(superscript = createToggleButton("superscript", GUI_HOVERTEXT_SUPERSCRIPT));
		// topPanel.add(new HTML("&nbsp;"));
		// topPanel.add(alignleft = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 460, 20, 20,
		// GUI_HOVERTEXT_ALIGNLEFT));
		// topPanel.add(alignmiddle = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 420, 20, 20,
		// GUI_HOVERTEXT_ALIGNCENTER));
		// topPanel.add(alignright = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 480, 20, 20,
		// GUI_HOVERTEXT_ALIGNRIGHT));

		// topPanel.add(new HTML("&nbsp;"));
		// topPanel.add(breaklink = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 640, 20, 20,
		// GUI_HOVERTEXT_BREAKLINK));
		// topPanel.add(new HTML("&nbsp;"));
		// topPanel.add(insertline = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 360, 20, 20,
		// GUI_HOVERTEXT_HLINE));
		// topPanel.add(insertimage = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 380, 20, 20,
		// GUI_HOVERTEXT_IMAGE));
		// topPanel.add(new HTML("&nbsp;"));

		// Init the BOTTOM Panel
		// topPanel.add(fontlist = createFontList());
		// topPanel.add(new HTML("&nbsp;"));

		inlineimageslist = createInlineimageList();
		if (!inlineImages.isEmpty()) {
			topPanel.add(new HTML("&nbsp;"));
			topPanel.add(inlineimageslist);
		}

		generatelink = createPushButton("link", GUI_HOVERTEXT_LINK);
		unorderlist = createPushButton("unorderlist", GUI_HOVERTEXT_UNORDERLIST);
		orderlist = createPushButton("orderlist", GUI_HOVERTEXT_ORDERLIST);
		indentright = createPushButton("indentRight", GUI_HOVERTEXT_IDENTRIGHT);
		indentleft = createPushButton("indentLeft", GUI_HOVERTEXT_IDENTLEFT);
		if (extrasEnabled) {
			topPanel.add(new HTML("&nbsp;"));
			topPanel.add(unorderlist);
			topPanel.add(orderlist);
			topPanel.add(indentright);
			topPanel.add(indentleft);

			topPanel.add(new HTML("&nbsp;"));
			topPanel.add(generatelink);
		}

		// topPanel.add(new HTML("&nbsp;"));
		// topPanel.add(removeformatting = createPushButton("remove", GUI_HOVERTEXT_REMOVEFORMAT));

		topPanel.add(new HTML("&nbsp;"));
		toggleTextSource = createToggleButton("texthtml", GUI_HOVERTEXT_SWITCHVIEW);

		topPanel.add(toggleTextSource);
	}

	private static final int ICON_SIZE = 16;

	/** Method to create a Toggle button for the toolbar **/
	private ToggleButton createToggleButton(String name, String tip) {
		Image image = new Image("img/richtext/" + name + ".png");
		image.setWidth(ICON_SIZE + "px");
		image.setHeight(ICON_SIZE + "px");
		ToggleButton button = new ToggleButton(image);
		button.setHeight((ICON_SIZE + 2) + "px");
		button.setWidth((ICON_SIZE + 2) + "px");
		button.addClickHandler(evHandler);
		if (tip != null) button.setTitle(tip);
		return button;
	}

	/** Method to create a Push button for the toolbar **/
	private PushButton createPushButton(String name, String tip) {
		Image image = new Image("img/richtext/" + name + ".png");
		image.setWidth(ICON_SIZE + "px");
		image.setHeight(ICON_SIZE + "px");
		PushButton button = new PushButton(image);
		button.setHeight((ICON_SIZE + 2) + "px");
		button.setWidth((ICON_SIZE + 2) + "px");
		button.addClickHandler(evHandler);
		if (tip != null) {
			button.setTitle(tip);
		}
		return button;
	}

	/** Method to create the fontlist for the toolbar **/
	private ListBox createFontList() {
		ListBox mylistBox = new ListBox();
		mylistBox.addChangeHandler(evHandler);
		mylistBox.setVisibleItemCount(1);

		mylistBox.addItem(GUI_LISTNAME_FONTS);
		for (String name : GUI_FONTLIST.keySet()) {
			mylistBox.addItem(name, GUI_FONTLIST.get(name));
		}

		return mylistBox;
	}

	/** Method to create the colorlist for the toolbar **/
	private ListBox createColorList() {
		ListBox mylistBox = new ListBox();
		mylistBox.addChangeHandler(evHandler);
		mylistBox.setVisibleItemCount(1);

		mylistBox.addItem(GUI_LISTNAME_COLORS);
		for (String name : GUI_COLORLIST.keySet()) {
			mylistBox.addItem(name, GUI_COLORLIST.get(name));
		}

		return mylistBox;
	}

	private ListBox createInlineimageList() {
		ListBox mylistBox = new ListBox();
		mylistBox.addChangeHandler(evHandler);
		mylistBox.setVisibleItemCount(1);

		mylistBox.addItem(GUI_LISTNAME_INLINEIMAGES);
		for (String image : inlineImages) {
			mylistBox.addItem(image);
		}

		return mylistBox;
	}

	private static String beautifyHtml(String html) {
		html = html.replace("><div>", ">\n<div>");
		html = html.replace("><br>", ">\n<br>");
		html = html.replace("<br><", "<br>\n<");
		html = html.replace("><ul>", ">\n<ul>");
		html = html.replace("><ol>", ">\n<ol>");
		html = html.replace("><li>", ">\n  <li>");
		html = html.replace("><img ", ">\n  <img ");
		return html;
	}
}
