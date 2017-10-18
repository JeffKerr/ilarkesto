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
package ilarkesto.mda.legacy.generator;

import ilarkesto.core.base.MultilineBuilder;
import ilarkesto.core.base.Str;
import ilarkesto.core.base.Utl;
import ilarkesto.core.money.Money;
import ilarkesto.core.persistance.AEntity;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.DateRange;
import ilarkesto.core.time.DayAndMonth;
import ilarkesto.core.time.Time;
import ilarkesto.gwt.client.desktop.AEditableReferenceField;
import ilarkesto.gwt.client.desktop.AEditableSetReferenceField;
import ilarkesto.gwt.client.desktop.ActivityParameters;
import ilarkesto.gwt.client.desktop.Widgets;
import ilarkesto.gwt.client.desktop.fields.AEditableBooleanDropdownField;
import ilarkesto.gwt.client.desktop.fields.AEditableDateAndTimeField;
import ilarkesto.gwt.client.desktop.fields.AEditableDateField;
import ilarkesto.gwt.client.desktop.fields.AEditableDateRangeField;
import ilarkesto.gwt.client.desktop.fields.AEditableDayAndMonthField;
import ilarkesto.gwt.client.desktop.fields.AEditableDecimalField;
import ilarkesto.gwt.client.desktop.fields.AEditableFloatField;
import ilarkesto.gwt.client.desktop.fields.AEditableIntegerField;
import ilarkesto.gwt.client.desktop.fields.AEditableLongField;
import ilarkesto.gwt.client.desktop.fields.AEditableMoneyField;
import ilarkesto.gwt.client.desktop.fields.AEditableMultiLineTextField;
import ilarkesto.gwt.client.desktop.fields.AEditableRichTextField;
import ilarkesto.gwt.client.desktop.fields.AEditableTextField;
import ilarkesto.gwt.client.desktop.fields.AEditableTimeField;
import ilarkesto.gwt.client.desktop.fields.AField;
import ilarkesto.mda.legacy.model.ACollectionPropertyModel;
import ilarkesto.mda.legacy.model.EntityModel;
import ilarkesto.mda.legacy.model.PropertyModel;
import ilarkesto.mda.legacy.model.ReferencePropertyModel;
import ilarkesto.mda.legacy.model.ReferenceSetPropertyModel;
import ilarkesto.mda.legacy.model.StringPropertyModel;
import ilarkesto.mda.legacy.model.WarningModel;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public class GwtEntityPropertyFieldGenerator extends AClassGenerator {

	protected PropertyModel property;

	protected EntityModel entity;
	protected boolean readonly;
	protected boolean typeDate;
	protected boolean typeTime;
	protected boolean typeDateAndTime;
	protected boolean typeDayAndMonth;
	protected boolean typeDateRange;
	protected boolean typeInteger;
	protected boolean typeLong;
	protected boolean typeFloat;
	protected boolean typeDecimal;
	protected boolean typeMoney;
	protected boolean typeSetReference;
	protected boolean typeReference;
	protected boolean typeBoolean;

	public GwtEntityPropertyFieldGenerator(PropertyModel property) {
		super();
		this.property = property;

		entity = property.getEntity();
		typeDate = property.getType().equals(Date.class.getName());
		typeTime = property.getType().equals(Time.class.getName());
		typeDateAndTime = property.getType().equals(DateAndTime.class.getName());
		typeDateRange = property.getType().equals(DateRange.class.getName());
		typeDayAndMonth = property.getType().equals(DayAndMonth.class.getName());
		typeInteger = property.getType().equals(Integer.class.getName())
				|| property.getType().equals(int.class.getName());
		typeLong = property.getType().equals(Long.class.getName());
		typeFloat = property.getType().equals(Float.class.getName());
		typeDecimal = property.getType().equals(BigDecimal.class.getName());
		typeMoney = property.getType().equals(Money.class.getName());
		typeSetReference = property instanceof ReferenceSetPropertyModel;
		typeReference = property instanceof ReferencePropertyModel;
		typeBoolean = property.getType().equals(boolean.class.getName());
	}

	@Override
	protected void writeContent() {
		String label = property.getLabel();
		if (label == null) label = property.getName();
		String tooltip = property.getTooltip();
		if (tooltip == null) tooltip = "";

		ln();
		ln("    public static final String LABEL = \"" + label + "\";");
		ln("    public static final String TOOLTIP = \"" + Str.escapeJavaString(tooltip) + "\";");

		ln();
		ln("    protected", replaceServerWithClient(entity.getBeanClass()), "entity;");

		writeConstructor();
		writeGetLabel();
		writeGetTooltip(tooltip);
		if (!readonly) {
			writeIsMandatory();
			writeApplyValue();
			writeEditVeto();
		}
		writeIsMasked();
		writeGetWarning();

		if (!property.isCollection() && !property.isReference() && !readonly) {
			writeGetValue();
		}

		if (readonly) {
			writeCreateDisplayWidget();
		}

		if (property instanceof ReferencePropertyModel) {
			writeGetSelectedEntity();
			writeGetSelectableEntities();
		}
		if (property.isReference() && property.isCollection()) {
			writeGetSelectableEntities();
			writeGetSelectedEntities();
		}

		writeCreateParametersForServer();

		writeGetSuffix();
		writeGetMaxLength();
	}

	private void writeCreateParametersForServer() {
		ln();
		ln("    public", ActivityParameters.class.getName(), "createParametersForServer() {");
		ln("        if (!entity.isPersisted()) return new " + ActivityParameters.class.getName() + "();");
		ln("        return new " + ActivityParameters.class.getName() + "(entity);");
		ln("    }");
	}

	private void writeGetMaxLength() {
		if (!(property instanceof StringPropertyModel)) return;
		Integer maxLenght = ((StringPropertyModel) property).getMaxLenght();
		if (maxLenght == null) return;
		ln();
		annotationOverride();
		ln("    protected int getMaxLength() {");
		ln("        return " + maxLenght + ";");
		ln("    }");
	}

	protected void writeLabelImportant() {
		ln();
		annotationOverride();
		ln("    protected boolean isLabelImportant() {");
		ln("        return true;");
		ln("    }");
	}

	protected void writeCreateDisplayWidget() {
		ln();
		annotationOverride();
		ln("    public", IsWidget.class.getName(), "createDisplayWidget() {");
		ln("        return",
			Widgets.class.getName() + ".text(entity.get" + Str.uppercaseFirstLetter(property.getName()) + "());");
		ln("    }");
	}

	protected void writeEditVeto() {
		ln();
		annotationOverride();
		ln("    protected String getEditVetoMessage() {");
		ln("        return super.getEditVetoMessage();");
		ln("    }");
	}

	private void writeConstructor() {
		ln();
		ln("    public " + getName() + "(" + replaceServerWithClient(entity.getBeanClass()) + " entity) {");
		ln("        this.entity = entity;");
		if (property instanceof ReferencePropertyModel || property instanceof ReferenceSetPropertyModel) {
			ln("        setEditorAsync(!getClass().getName().contains(\"$\"));");
		}
		ln("    }");
	}

	private void writeGetWarning() {
		List<WarningModel> warnings = property.getWarnings();
		if (warnings.isEmpty()) return;

		ln();
		annotationOverride();
		ln("    public String getWarning() {");
		ln("        " + MultilineBuilder.class.getName() + " mb = new " + MultilineBuilder.class.getName() + "();");
		for (WarningModel warning : warnings) {
			ln("        if (entity.is" + Str.uppercaseFirstLetter(warning.getPredicate()) + "()) mb.ln(\""
					+ warning.getWarning() + "\");");
		}
		ln("        return mb.isEmpty() ? super.getWarning() : mb.toString();");
		ln("    }");
	}

	private void writeGetSuffix() {
		String suffix = property.getSuffix();
		if (suffix == null) return;
		ln();
		annotationOverride();
		ln("    public String getSuffix() {");
		ln("        return \"" + suffix + "\";");
		ln("    }");
	}

	private void writeIsMasked() {
		if (!(property instanceof StringPropertyModel)) return;
		StringPropertyModel spm = (StringPropertyModel) property;
		if (!spm.isMasked()) return;

		ln();
		annotationOverride();
		ln("    protected boolean isMasked() {");
		ln("        return true;");
		ln("    }");
	}

	private void writeGetSelectedEntities() {
		String referencedType;
		if (property.isCollection()) {
			referencedType = property.getContentType();
		} else {
			referencedType = getBeanClass(((ReferencePropertyModel) property));
		}
		referencedType = replaceServerWithClient(referencedType);
		ln();
		annotationOverride();
		ln("    protected Collection<" + referencedType + "> getSelectedEntities() {");
		ln("        if (entity == null) return Collections.emptyList();");
		ln("        return " + Utl.class.getName() + ".sort(entity.get" + Str.uppercaseFirstLetter(property.getName())
				+ "());");
		ln("    }");
	}

	private void writeGetSelectableEntities() {

		String referencedType;
		if (property.isCollection()) {
			referencedType = property.getContentType();
		} else {
			referencedType = getBeanClass(((ReferencePropertyModel) property));
		}
		referencedType = replaceServerWithClient(referencedType);
		ln();
		annotationOverride();
		ln("    protected List<" + referencedType + "> getSelectableEntities() {");
		ln("        List<" + referencedType + "> ret = new java.util.ArrayList<" + referencedType + ">("
				+ referencedType + "." + property.getSelectableEntitiesListAllMethodName() + "());");
		ln("        " + Collections.class.getName() + ".sort(ret);");
		ln("        return ret;");
		ln("    }");
	}

	private void writeGetSelectedEntity() {
		ln();
		annotationOverride();
		String type = getBeanClass(((ReferencePropertyModel) property));
		type = replaceServerWithClient(type);
		ln("    public", type, "getSelectedEntity()  {");
		ln("        return entity.get" + Str.uppercaseFirstLetter(property.getName()) + "();");
		ln("    }");
	}

	protected void writeGetLabel() {
		ln();
		annotationOverride();
		ln("    public String getLabel() {");
		ln("        return LABEL;");
		ln("    }");
	}

	protected void writeGetMilestoneLabel() {
		ln();
		annotationOverride();
		ln("    public String getMilestoneLabel() {");
		ln("        return LABEL;");
		ln("    }");
	}

	private void writeGetTooltip(String tooltip) {
		if (tooltip != null) {
			ln();
			annotationOverride();
			ln("    public String getTooltip() {");
			ln("        return TOOLTIP;");
			ln("    }");
		}
	}

	private void writeIsMandatory() {
		ln();
		ln("    public boolean isMandatory() {");
		ln("        return " + property.isMandatory() + ";");
		ln("    }");
	}

	private void writeApplyValue() {
		ln();
		annotationOverride();
		String type = property.getType();
		type = type.replace("Set<", "Collection<");
		type = type.replace("List<", "Collection<");
		type = replaceServerWithClient(type);
		if (type.equals(int.class.getName())) type = Integer.class.getName();
		ln("    public void applyValue(" + type + " value) {");
		ln("        entity.set" + Str.uppercaseFirstLetter(property.getName()) + "(value);");
		ln("    }");
	}

	private void writeGetValue() {
		ln();
		annotationOverride();
		String retType = "String";
		if (typeBoolean) retType = "boolean";
		if (typeMoney) retType = Money.class.getName();
		if (typeDateRange) retType = DateRange.class.getName();
		if (typeDecimal) retType = BigDecimal.class.getName();
		String typePrefix = "";
		if (typeMoney) typePrefix = "Money";
		if (typeDecimal) typePrefix = "BigDecimal";
		ln("    public", retType, "get" + typePrefix + "Value()  {");

		if (typeBoolean) {
			ln("       return entity.is" + Str.uppercaseFirstLetter(property.getName()) + "();");
		} else if (typeDecimal) {
			ln("        return entity.get" + Str.uppercaseFirstLetter(property.getName()) + "();");
		} else if (typeMoney) {
			ln("        return entity.get" + Str.uppercaseFirstLetter(property.getName()) + "();");
		} else if (property.isString()) {
			ln("        return entity.get" + Str.uppercaseFirstLetter(property.getName()) + "();");
		} else if (typeDateRange) {
			ln("        return entity.get" + Str.uppercaseFirstLetter(property.getName()) + "();");
		} else {
			String getter = (property.isBoolean() ? "is" : "get") + Str.uppercaseFirstLetter(property.getName()) + "()";
			ln("        return " + Str.class.getName() + ".format(entity." + getter + ");");
		}

		ln("    }");

	}

	@Override
	protected String getName() {
		return "G" + entity.getName() + Str.uppercaseFirstLetter(property.getName()) + "Field";
	}

	@Override
	protected String getPackage() {
		String s = entity.getPackageName();
		s = s.replace(".shared.", ".client.");
		s = s.replace(".server.", ".client.");
		return s + ".entityfields";
	}

	@Override
	protected String getSuperclass() {
		if (typeBoolean) return AEditableBooleanDropdownField.class.getName();
		if (typeMoney) return AEditableMoneyField.class.getName();
		if (typeInteger) return AEditableIntegerField.class.getName();
		if (typeLong) return AEditableLongField.class.getName();
		if (typeFloat) return AEditableFloatField.class.getName();
		if (typeDecimal) return AEditableDecimalField.class.getName();
		if (typeDate) return AEditableDateField.class.getName();
		if (typeTime) return AEditableTimeField.class.getName();
		if (typeDateAndTime) return AEditableDateAndTimeField.class.getName();
		if (typeDateRange) return AEditableDateRangeField.class.getName();
		if (typeDayAndMonth) return AEditableDayAndMonthField.class.getName();
		if (property.isCollection()) return AEditableSetReferenceField.class.getName() + "<"
				+ ((ACollectionPropertyModel) property).getContentType().replace(".server.", ".client.") + ">";
		if (typeSetReference) return AEditableSetReferenceField.class.getName() + "<"
				+ getBeanClass(((ReferenceSetPropertyModel) property)) + ">";
		if (property.isString()) {
			if (((StringPropertyModel) property).isRichtext()) return AEditableRichTextField.class.getName();
			if (((StringPropertyModel) property).isMultiline()) return AEditableMultiLineTextField.class.getName();
			return AEditableTextField.class.getName();
		}
		if (property instanceof ReferencePropertyModel) return AEditableReferenceField.class.getName() + "<"
				+ getBeanClass((ReferencePropertyModel) property) + ">";
		return AField.class.getName();
	}

	protected String getBeanClass(ReferenceSetPropertyModel property) {
		return replaceServerWithClient(property.getReferencedEntity().getBeanClass());
	}

	protected String getBeanClass(ReferencePropertyModel property) {
		return replaceServerWithClient(property.getReferencedEntity().getBeanClass());
	}

	protected String replaceServerWithClient(String className) {
		String ret = className.replace(".server.", ".client.");
		if (ret.equals(ilarkesto.persistence.AEntity.class.getName())) return AEntity.class.getName();
		return ret;
	}

	@Override
	protected boolean isInterface() {
		return false;
	}

	@Override
	protected boolean isAbstract() {
		return false;
	}

	@Override
	protected boolean isOverwrite() {
		return true;
	}

}
