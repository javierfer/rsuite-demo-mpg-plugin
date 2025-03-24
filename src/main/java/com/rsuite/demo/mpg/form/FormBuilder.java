package com.rsuite.demo.mpg.form;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.DataTypeOptionValue;
import com.reallysi.rsuite.api.FormControlType;
import com.reallysi.rsuite.api.IdAllocationException;
import com.reallysi.rsuite.api.MetaDataType;
import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.reallysi.rsuite.api.forms.FormColumnInstance;
import com.reallysi.rsuite.api.forms.FormDefinition;
import com.reallysi.rsuite.api.forms.FormInstance;
import com.reallysi.rsuite.api.forms.FormParameterInstance;

public class FormBuilder {
	private static final Log log = LogFactory.getLog(FormBuilder.class);
	protected int columnIndex = 0;
	protected FormInstance form;
	protected Map<String, FormColumnInstance> byColumnName = new HashMap<String, FormColumnInstance>();
	protected Map<String, ColumnParamTuple> byParamName = new HashMap<String, ColumnParamTuple>();

	public FormBuilder(FormInstance form) {
		this.form = form;
		List<FormColumnInstance> columns = form.getColumns();
		if (columns.size() == 0) {
			column().name(FormDefinition.DEFAULT_COLUMN);
		} else {
			for (FormColumnInstance column : columns) {
				byColumnName.put(column.getName(), column);
				for (FormParameterInstance param : column.getParams()) {
					byParamName.put(param.getName(), new ColumnParamTuple(column, param));
				}
			}
		}
	}

	public FormBuilder(ExecutionContext context) throws IdAllocationException {
		this.form = new FormInstance(context.getGenericIDGenerator().allocateId());
	}

	public FormColumnBuilder column() {

		List<FormColumnInstance> columns = form.getColumns();
		int sz = columns.size();
		if (columnIndex == sz - 1) {
			columnIndex = sz;
		}
		FormColumnBuilder builder = new FormColumnBuilder(this).name("column-" + sz);
		columns.add(builder.column);
		return builder;
	}

	public FormColumnBuilder lastColumn() {
		return new FormColumnBuilder(this, this.form.getColumns().get(columnIndex));
	}

	public FormColumnBuilder column(int columnIndex) {
		int sz = form.getColumns().size();
		if (columnIndex == sz) {
			return column();
		}
		if (sz == 0 || columnIndex < 0 || columnIndex > sz) {
			return null;
		}
		return new FormColumnBuilder(this, this.form.getColumns().get(columnIndex));
	}

	public FormColumnBuilder column(String columnName) {
		FormColumnInstance column = byColumnName.get(columnName);
		if (column == null) {
			return column().name(columnName);
		}
		return new FormColumnBuilder(this, column);
	}

	public FormBuilder description(String description) {
		form.setDescription(description);
		return this;
	}

	public FormBuilder instructions(String instructions) {
		form.setInstructions(instructions);
		return this;
	}

	public FormBuilder title(String title) {
		form.setLabel(title);
		return this;
	}

	public FormBuilder name(String name) {
		form.setName(name);
		return this;
	}

	public FormParameterBuilder<FormBuilder> param() {
		FormColumnBuilder column = lastColumn();
		return new FormParameterBuilder<FormBuilder>(this, column, this)
				.name("label-" + column.column.getName() + "-" + column.column.getParams().size());
	}

	public FormParameterBuilder<FormBuilder> param(String name) {
		ColumnParamTuple tuple = byParamName.get(name);
		if (tuple == null) {
			return param().name(name);
		}
		return new FormParameterBuilder<FormBuilder>(this,
				new FormColumnBuilder(this, tuple.column), tuple.param, this);
	}

	public FormParameterBuilder<FormBuilder> param(String type, String name) {
		return param(name).formControlType(type);
	}

	public FormBuilder property(String name, String value, String... more) {
		Map<String, Object> props = form.getPropertyMap();
		props.put(name, value);
		for (int i = 0; i < more.length; i += 2) {
			props.put(more[i], more[i + 1]);
		}
		return this;
	}

	public FormBuilder warning(String warning) {
		form.setWarningMessage(warning);
		return this;
	}

	public class ColumnParamTuple {
		protected FormColumnInstance column;
		protected FormParameterInstance param;

		protected ColumnParamTuple(FormColumnInstance column, FormParameterInstance param) {
			this.column = column;
			this.param = param;
		}
	}

	public FormParameterBuilder<FormBuilder> label(String content) {
		return param().formControlType("label").label(content);
	}

	public FormParameterBuilder<FormBuilder> hrule() {
		return label("<hr />");
	}

	public FormParameterBuilder<FormBuilder> text(String name, String label) {
		return param().formControlType("input").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> textarea(String name, String label) {
		return param().formControlType("textarea").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> select(String name, String label) {
		return param().formControlType("input").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> multiselect(String name, String label) {
		return param().formControlType("multiselect").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> radio(String name, String label) {
		return param().formControlType("radiobutton").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> checkbox(String name, String label) {
		return param().formControlType("checkbox").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> taxonomy(String name, String label) {
		return param().formControlType("taxonomy").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> ckeditor(String name, String label) {
		return param().formControlType("ckeditor").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> date(String name, String label) {
		return param().formControlType("datepicker").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> dateRange(String name, String label) {
		return param().formControlType("daterange").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> hidden(String name, String value) {
		return param().formControlType("hidden").name(name).value(value);
	}

	public FormParameterBuilder<FormBuilder> autocomplete(String name, String label) {
		return param().formControlType("autocomplete").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> file(String name, String label) {
		return param().formControlType("file").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> button(String name, String label) {
		return param().formControlType("submitbutton").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> buttons(String... nameValuePairs) {
		FormParameterBuilder<FormBuilder> param = param().formControlType("submitbutton");
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String moreName = nameValuePairs[i];
			String moreLabel = nameValuePairs[i + 1];
			if (moreName == null || moreLabel == null) {
				continue;
			}
			param.option(moreLabel, moreName);
		}
		return param;
	}

	public FormParameterBuilder<FormBuilder> password(String name, String label) {
		return param().formControlType("password").name(name).label(label);
	}

	public FormParameterBuilder<FormBuilder> copyOf(String name) {
		FormParameterInstance copy = copyParam(byParamName.get(name).param);
		lastColumn().column.getParams().add(copy);
		return new FormParameterBuilder<FormBuilder>(this, lastColumn(), copy, this);
	}

	public class FormColumnBuilder {

		protected FormColumnInstance column;
		protected FormBuilder form;
		protected Map<String, ColumnParamTuple> byParamName = new HashMap<String, ColumnParamTuple>();

		protected FormColumnBuilder(FormBuilder form) {
			this(form, new FormColumnInstance());
		}

		protected FormColumnBuilder(FormBuilder form, FormColumnInstance column) {
			this.form = form;
			this.column = column;
		}

		public FormParameterBuilder<FormColumnBuilder> param() {
			FormParameterBuilder<FormColumnBuilder> param = new FormParameterBuilder<FormColumnBuilder>(
					form, this, this);
			List<FormParameterInstance> params = column.getParams();
			param.name("label-" + column.getName() + "-" + params.size());
			params.add(param.parameter);
			return param;
		}

		public FormParameterBuilder<FormColumnBuilder> param(String name) {
			ColumnParamTuple tuple = byParamName.get(name);
			if (tuple == null) {
				return param().name(name);
			}
			return new FormParameterBuilder<FormColumnBuilder>(form, this, tuple.param, this);
		}

		public FormParameterBuilder<FormColumnBuilder> param(String type, String name) {
			return param(name).formControlType(type);
		}

		public FormParameterBuilder<FormColumnBuilder> label(String content) {
			return param().formControlType("label").label(content);
		}

		public FormParameterBuilder<FormColumnBuilder> hrule() {
			return label("<hr />");
		}

		public FormParameterBuilder<FormColumnBuilder> text(String name, String label) {
			return param().formControlType("input").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> textarea(String name, String label) {
			return param().formControlType("textarea").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> select(String name, String label) {
			return param().formControlType("input").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> multiselect(String name, String label) {
			return param().formControlType("multiselect").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> radio(String name, String label) {
			return param().formControlType("radiobutton").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> checkbox(String name, String label) {
			return param().formControlType("checkbox").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> taxonomy(String name, String label) {
			return param().formControlType("taxonomy").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> ckeditor(String name, String label) {
			return param().formControlType("ckeditor").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> date(String name, String label) {
			return param().formControlType("datepicker").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> dateRange(String name, String label) {
			return param().formControlType("daterange").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> hidden(String name, String value) {
			return param().formControlType("hidden").name(name).value(value);
		}

		public FormParameterBuilder<FormColumnBuilder> autocomplete(String name, String label) {
			return param().formControlType("autocomplete").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> file(String name, String label) {
			return param().formControlType("file").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> button(String name, String label) {
			return param().formControlType("input").name(name).label(label);
		}

		public FormParameterBuilder<FormColumnBuilder> buttons(String... nameValuePairs) {
			FormParameterBuilder<FormColumnBuilder> param = param().formControlType("submitbutton");
			for (int i = 0; i < nameValuePairs.length; i += 2) {
				String moreName = nameValuePairs[i];
				String moreLabel = nameValuePairs[i + 1];
				if (moreName == null || moreLabel == null) {
					continue;
				}
				param.option(moreLabel, moreName);
			}
			return param;
		}

		public FormParameterBuilder<FormColumnBuilder> password(String name, String label) {
			return param().formControlType("input").name(name).label(label);
		}

		public FormColumnBuilder name(String name) {
			if (form.byColumnName.containsKey(name)) {
				log.warn("Column name '" + name
						+ "' is not unique; if you're addressing it by FormBuilder#column(name), you may not get the right column.");
			}
			form.byColumnName.remove(column.getName());
			column.setName(name);
			form.byColumnName.put(name, column);
			return this;
		}

		public FormParameterBuilder<FormColumnBuilder> copyOf(String name) {
			FormParameterInstance copy = copyParam(form.byParamName.get(name).param);
			column.getParams().add(copy);
			return new FormParameterBuilder<FormColumnBuilder>(form, this, copy, this);
		}

		public FormBuilder end() {
			return form;
		}

	}

	public class FormParameterBuilder<T> {
		protected T parent;
		protected FormBuilder form;
		protected FormColumnBuilder column;
		protected FormParameterInstance parameter;

		protected FormParameterBuilder(FormBuilder form, FormColumnBuilder column, T parent) {
			parameter = new FormParameterInstance();
			this.form = form;
			this.column = column;
			this.parent = parent;
		}

		protected FormParameterBuilder(FormBuilder form, FormColumnBuilder column,
				FormParameterInstance param, T parent) {
			parameter = param;
			this.form = form;
			this.column = column;
			this.parent = parent;
		}
		
		public FormParameterBuilder<T> addValue(String value) {
			parameter.addValue(value);
			return this;
		}

		public FormParameterBuilder<T> name(String name) {
			form.byParamName.remove(parameter.getName());
			column.byParamName.remove(parameter.getName());
			parameter.setName(name);
			ColumnParamTuple tuple = new ColumnParamTuple(column.column, parameter);
			form.byParamName.put(name, tuple);
			column.byParamName.put(name, tuple);
			return this;
		}

		public FormParameterBuilder<T> label(String label) {
			parameter.setLabel(label);
			return this;
		}
		
		public String label() {
			return parameter.getLabel();
		}

		public FormParameterBuilder<T> description(String description) {
			parameter.setDescription(description);
			return this;
		}

		public FormParameterBuilder<T> size(String size) {
			parameter.setSize(size);
			return this;
		}

		public FormParameterBuilder<T> style(String style) {
			parameter.setFieldStyle(style);
			return this;
		}

		public FormParameterBuilder<T> styleClass(String styleClass) {
			parameter.setFieldStyleClass(styleClass);
			return this;
		}

		public FormParameterBuilder<T> value(String... value) {
			parameter.getValues().addAll(Arrays.asList(value));
			return this;
		}

		public FormParameterBuilder<T> values(Collection<String> values) {
			parameter.getValues().addAll(values);
			return this;
		}

		public FormParameterBuilder<T> dataType(String name) {
			parameter.setDataTypeName(name);
			return this;
		}

		public FormParameterBuilder<T> sortOptions(String options) {
			parameter.setSortOptions(options);
			return this;
		}

		public FormParameterBuilder<T> baseType(String baseType) {
			parameter.setBaseType(baseType);
			return this;
		}

		public FormParameterBuilder<T> metaDataType(String type) {
			parameter.setMetaDataType(MetaDataType.fromName(type, MetaDataType.OBJECT));
			return this;
		}

		public FormParameterBuilder<T> formControlType(String type) {
			parameter.setFormControlType(FormControlType.fromName(type));
			return this;
		}

		public FormParameterBuilder<T> required(Boolean b) {
			parameter.setRequired(b);
			return this;
		}

		public FormParameterBuilder<T> allowMultiple(Boolean b) {
			parameter.setAllowMultiple(b);
			return this;
		}

		public FormParameterBuilder<T> readOnly(Boolean b) {
			parameter.setReadOnly(b);
			return this;
		}

		public FormParameterBuilder<T> validationRegex(String rx) {
			parameter.setValidationRegex(rx);
			return this;
		}

		public FormParameterBuilder<T> validationErrorMessage(String message) {
			parameter.setValidationErrorMessage(message);
			return this;
		}

		public FormParameterBuilder<T> validate(String regex, String message) {
			parameter.setValidationRegex(regex);
			parameter.setValidationErrorMessage(message);
			return this;
		}

		public FormParameterBuilder<T> option(String... more) {
			List<DataTypeOptionValue> options = parameter.getBeforeOptions();
			for (int i = 0; i < more.length; i += 2) {
				String value = more[i];
				String label = more[i + 1];
				if (value == null || label == null) {
					continue;
				}
				options.add(new DataTypeOptionValue(value, label));
			}
			return this;
		}

		public FormParameterBuilder<T> option(String value) {
			parameter.getBeforeOptions().add(new DataTypeOptionValue(value, value));
			return this;
		}

		public FormParameterBuilder<T> property(Object... more) {
			Map<String, Object> props = parameter.getPropertyMap();
			for (int i = 0; i < more.length; i += 2) {
				if (more[i] == null || more[i + 1] == null) {
					continue;
				}
				String moreName = more[i].toString();
				Object moreValue = more[i + 1];
				props.put(moreName, moreValue);
			}

			return this;
		}

		public T end() {
			return parent;
		}
	}

	protected static FormParameterInstance copyParam(FormParameterInstance parameter) {
		FormParameterInstance copy = new FormParameterInstance();
		copy.setAllowMultiple(parameter.isAllowMultiple());
		copy.setBaseType(parameter.getBaseType());
		copy.getBeforeOptions().addAll(parameter.getBeforeOptions());
		copy.setCol(parameter.getCol());
		copy.setDataTypeName(parameter.getDataTypeName());
		copy.setDescription(parameter.getDescription());
		copy.setFormControlType(parameter.getFormControlType());
		copy.setLabel(parameter.getLabel());
		copy.setMetaDataType(parameter.getMetaDataType());
		copy.setName(parameter.getName());
		copy.setReadOnly(parameter.isReadOnly());
		copy.setRequired(parameter.isRequired());
		copy.setSize(parameter.getSize());
		copy.setSortOptions(parameter.getSortOptions());
		copy.setFieldStyle(parameter.getFieldStyle());
		copy.setFieldStyleClass(parameter.getFieldStyleClass());
		copy.setValidationErrorMessage(parameter.getValidationErrorMessage());
		copy.setValidationRegex(parameter.getValidationRegex());
		copy.getValues().addAll(parameter.getValues());
		return copy;
	}
}