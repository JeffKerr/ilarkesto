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
package ilarkesto.integration.itext;

import ilarkesto.core.base.Color;
import ilarkesto.core.base.Utl;
import ilarkesto.pdf.ACell;
import ilarkesto.pdf.APdfElement;
import ilarkesto.pdf.ARow;
import ilarkesto.pdf.ATable;
import ilarkesto.pdf.FontStyle;

import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class Table extends ATable implements ItextElement {

	private List<ARow> rows = new ArrayList<ARow>();

	Table(APdfElement parent, FontStyle fontStyle) {
		super(parent, fontStyle);
	}

	@Override
	public Element[] createITextElements(Document document) {
		float[] cellWidths = getCellWidths();
		int columnCount = getColumnCount();
		PdfPTable t = cellWidths == null ? new PdfPTable(columnCount) : new PdfPTable(cellWidths);
		t.setHeaderRows(headerRowCount);
		t.setFooterRows(footerRowCount);

		t.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);

		Float width = getWidth();
		if (width != null) t.setWidthPercentage(width);

		List<Integer> rowsToKeepTogether = new ArrayList<Integer>();

		int rowIdx = 0;
		for (ARow row : rows) {
			row.addMissingCells();
			if (row.isKeepTogether() && rowIdx < rows.size()) rowsToKeepTogether.add(rowIdx);
			for (ACell cell : row.getCells()) {
				PdfPCell pdfPCell = (PdfPCell) ((Cell) cell).createITextElements(document)[0];
				t.addCell(pdfPCell);
			}
			rowIdx++;
		}

		if (!rowsToKeepTogether.isEmpty()) t.keepRowsTogether(Utl.toArrayOfInt(rowsToKeepTogether));

		return new Element[] { t };
	}

	@Override
	public ATable createCellBorders(Color color, float width) {
		float[] cellWidths = getCellWidths();
		int cols = cellWidths == null ? getColumnCount() : cellWidths.length;
		int col = 0;
		int rowIdx = 0;
		for (ARow row : rows) {
			for (ACell cell : row.getCells()) {
				if (rowIdx == 0) cell.setBorderTop(color, width);
				cell.setBorderRight(color, width);
				cell.setBorderBottom(color, width);
				if (col == 0) cell.setBorderLeft(color, width);
				col += cell.getColspan();
				if (col >= cols) {
					col = 0;
					rowIdx++;
				}
			}
		}
		return this;
	}

	@Override
	public ARow row() {
		if (!rows.isEmpty()) {
			int lastRowIndex = rows.size() - 1;
			ARow lastRow = rows.get(lastRowIndex);
			if (lastRow.getCellCount() != getColumnCount())
				throw new IllegalStateException("Previous row (" + lastRowIndex + ") has " + lastRow.getCellCount()
						+ " cells, while table specifies " + getColumnCount() + " columns.");
		}

		ARow row = new Row(this);
		rows.add(row);

		FontStyle defaultFontStyle = getFontStyle();
		if (defaultFontStyle != null) row.setDefaultFontStyle(defaultFontStyle);

		return row;
	}

	@Override
	public ARow row(Object... cellTexts) {
		ARow row = row();
		for (Object text : cellTexts)
			row.cell(text);
		return row;
	}

	@Override
	public boolean isEmpty() {
		return rows.isEmpty();
	}

}
