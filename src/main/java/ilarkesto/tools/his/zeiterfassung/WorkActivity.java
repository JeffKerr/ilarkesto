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
package ilarkesto.tools.his.zeiterfassung;

import ilarkesto.core.base.Parser;
import ilarkesto.core.base.Parser.ParseException;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.Time;
import ilarkesto.core.time.TimePeriod;
import ilarkesto.io.CsvWriter;

public class WorkActivity {

	private DayAtWork dayAtWork;
	private Time start;
	private Time end;
	private String hiszillaId;
	private String achievoPhaseId;
	private String achievoPackageId;
	private String achievoProjectId;
	private String text;
	private boolean bookingRequired = true;
	private boolean alreadyBookedInHiszilla;

	public WorkActivity(DayAtWork dayAtWork, String line) throws ParseException {
		this.dayAtWork = dayAtWork;
		Parser parser = new Parser(line);
		parser.gotoAfter("* ");
		start = new Time(parser.getUntilAndGotoAfter(" - "));
		end = new Time(parser.getUntilAndGotoAfter(" "));

		if (parser.getNext(1).startsWith("!")) {
			bookingRequired = false;
			parser.gotoAfter(" ");
		}

		WorkActivity lastActivity = dayAtWork.getLastActivity();

		if (parser.getNext(1).startsWith("%")) {
			hiszillaId = lastActivity.getHiszillaId();
			achievoPhaseId = lastActivity.getAchievoPhaseId();
			text = lastActivity.getText();
			return;
		}

		if (parser.getNext(1).startsWith("#")) {
			hiszillaId = parser.getUntilAndGotoAfter(" ").substring(1);
		}

		if (parser.getNext(1).startsWith("$")) {
			achievoPhaseId = parser.getUntilAndGotoAfter(" ").substring(1);
		}

		text = parser.getRemaining();

		// if (lastActivity != null && "Daily Scrum".equals(text)) {
		// hiszillaId = lastActivity.getHiszillaId();
		// achievoId = lastActivity.getAchievoId();
		// }
	}

	public boolean isBookingRequired() {
		return bookingRequired;
	}

	public String getText() {
		return text;
	}

	public String getHiszillaId() {
		return hiszillaId;
	}

	public String getAchievoPhaseId() {
		return achievoPhaseId;
	}

	public void setAchievoPhaseId(String achievoId) {
		this.achievoPhaseId = achievoId;
	}

	public String getAchievoPackageId() {
		return achievoPackageId;
	}

	public void setAchievoPackageId(String achievoPackageId) {
		this.achievoPackageId = achievoPackageId;
	}

	public String getAchievoProjectId() {
		return achievoProjectId;
	}

	public void setAchievoProjectId(String achievoProjectId) {
		this.achievoProjectId = achievoProjectId;
	}

	public Time getStart() {
		return start;
	}

	public Time getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return start + " - " + end + " | " + hiszillaId + " | " + achievoPhaseId + " | " + text;
	}

	public void appendTo(CsvWriter csv) {
		csv.writeField(achievoProjectId); // projectId
		csv.writeField(achievoPackageId); // package
		csv.writeField(achievoPhaseId); // phaseId
		Date date = dayAtWork.getDate();

		// csv.writeField(date.getMonth() + "/" + date.getDay() + "/" + date.getYear()); // activitydate
		csv.writeField(date.formatDayMonthYear());

		csv.writeField("788"); // userid
		csv.writeField(text); // remark
		csv.writeField(getWorktimeInMinutes()); // time
		csv.writeField("0"); // business_trip
		csv.writeField(hiszillaId); // hiszilla
		csv.writeField("0"); // remaining_effort_time
		csv.writeField("");
		csv.writeField(date.getWeekday().format());
		csv.writeField(date.format());
	}

	public long getWorktimeInMinutes() {
		return getWorktime().toMinutes();
	}

	public TimePeriod getWorktime() {
		return start.getPeriodTo(end);
	}

	public void setAlreadyBookedInHiszilla(boolean alreadyBookedInHiszilla) {
		this.alreadyBookedInHiszilla = alreadyBookedInHiszilla;
	}

	public boolean isAlreadyBookedInHiszilla() {
		return alreadyBookedInHiszilla;
	}
}
