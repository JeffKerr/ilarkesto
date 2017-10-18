/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.pdf;

import ilarkesto.core.base.Args;
import ilarkesto.core.base.Color;

public abstract class ATable extends APdfElement {

	private Float width = 100f;
	private float[] cellWidths;
	private int columnCount;
	protected int headerRowCount;
	protected int footerRowCount;

	private Float defaultCellPadding;
	private FontStyle fontStyle;

	public abstract ARow row();

	public abstract ARow row(Object... cellTexts);

	public abstract ATable createCellBorders(Color color, float width);

	public abstract boolean isEmpty();

	public ATable(APdfElement parent, FontStyle fontStyle) {
		super(parent);
		Args.assertNotNull(fontStyle, "fontStyle");
		this.fontStyle = fontStyle;
	}

	public ARow headerRow() {
		headerRowCount++;
		return row();
	}

	public ARow headerRow(Object... cellTexts) {
		headerRowCount++;
		return row(cellTexts);
	}

	public ARow footerRow() {
		headerRowCount++;
		footerRowCount++;
		return row();
	}

	public ARow footerRow(Object... cellTexts) {
		headerRowCount++;
		footerRowCount++;
		return row(cellTexts);
	}

	/**
	 * Width in percent.
	 */
	public ATable setWidth(Float width) {
		this.width = width;
		return this;
	}

	public Float getWidth() {
		return width;
	}

	public float[] getCellWidths() {
		return cellWidths;
	}

	public ATable setCellWidths(float... cellWidths) {
		this.cellWidths = cellWidths;
		setColumnCount(cellWidths.length);
		return this;
	}

	public int getColumnCount() {
		if (cellWidths != null) return cellWidths.length;
		return columnCount;
	}

	public ATable setColumnCount(int columnCount) {
		this.columnCount = columnCount;
		return this;
	}

	public ATable setDefaultCellPadding(Float defaultCellPadding) {
		this.defaultCellPadding = defaultCellPadding;
		return this;
	}

	public Float getDefaultCellPadding() {
		return defaultCellPadding;
	}

	public ATable setFontStyle(FontStyle fontStyle) {
		Args.assertNotNull(fontStyle, "fontStyle");
		this.fontStyle = fontStyle;
		return this;
	}

	public final FontStyle getFontStyle() {
		return fontStyle;
	}

	// --- helper ---

}
