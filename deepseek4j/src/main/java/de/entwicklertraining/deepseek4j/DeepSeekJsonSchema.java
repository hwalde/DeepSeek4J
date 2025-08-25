package de.entwicklertraining.deepseek4j;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A sealed interface representing a DeepSeek-compatible JSON Schema definition,
 * supporting object/array/string/enum/anyOf types and the ability to mark fields as required.
 *
 * Usage example:
 * <pre>
 * DeepSeekJsonSchema eventSchema = DeepSeekJsonSchema.objectSchema()
 *     .property("name", DeepSeekJsonSchema.stringSchema("Event name"), true)
 *     .property("date", DeepSeekJsonSchema.stringSchema("Event date"), true)
 *     .additionalProperties(false);
 * </pre>
 */
public sealed interface DeepSeekJsonSchema permits DeepSeekJsonSchemaImpl {

    JSONObject toJson();

    DeepSeekJsonSchema description(String desc);

    DeepSeekJsonSchema property(String name, DeepSeekJsonSchema schema, boolean requiredField);

    DeepSeekJsonSchema items(DeepSeekJsonSchema itemSchema);

    DeepSeekJsonSchema enumValues(String... values);

    DeepSeekJsonSchema additionalProperties(boolean allowed);

    // --- Static factory methods ---

    static DeepSeekJsonSchema objectSchema() {
        return new DeepSeekJsonSchemaImpl("object");
    }

    static DeepSeekJsonSchema stringSchema(String description) {
        DeepSeekJsonSchemaImpl schema = new DeepSeekJsonSchemaImpl("string");
        schema.description(description);
        return schema;
    }

    static DeepSeekJsonSchema numberSchema(String description) {
        DeepSeekJsonSchemaImpl schema = new DeepSeekJsonSchemaImpl("number");
        schema.description(description);
        return schema;
    }

    static DeepSeekJsonSchema booleanSchema(String description) {
        DeepSeekJsonSchemaImpl schema = new DeepSeekJsonSchemaImpl("boolean");
        schema.description(description);
        return schema;
    }

    static DeepSeekJsonSchema integerSchema(String description) {
        DeepSeekJsonSchemaImpl schema = new DeepSeekJsonSchemaImpl("integer");
        schema.description(description);
        return schema;
    }

    static DeepSeekJsonSchema arraySchema(DeepSeekJsonSchema itemsSchema) {
        DeepSeekJsonSchemaImpl schema = new DeepSeekJsonSchemaImpl("array");
        schema.items(itemsSchema);
        return schema;
    }

    static DeepSeekJsonSchema enumSchema(String description, String... enumValues) {
        DeepSeekJsonSchemaImpl schema = new DeepSeekJsonSchemaImpl("string");
        schema.description(description);
        schema.enumValues(enumValues);
        return schema;
    }

    static DeepSeekJsonSchema anyOf(DeepSeekJsonSchema... variants) {
        DeepSeekJsonSchemaImpl schema = new DeepSeekJsonSchemaImpl(null);
        schema.setAnyOfMode(true);
        for (DeepSeekJsonSchema variant : variants) {
            schema.getAnyOfSchemas().put(variant.toJson());
        }
        return schema;
    }
}

final class DeepSeekJsonSchemaImpl implements DeepSeekJsonSchema {

    private String type;           // "object", "string", etc. May be null if we're in anyOfMode
    private String description;    // optional field
    private final JSONObject properties;
    private final JSONArray required;
    private final JSONArray enumValues;
    private DeepSeekJsonSchema itemsSchema;
    private final JSONArray anyOfSchemas;
    private boolean additionalProperties;
    private boolean anyOfMode;

    DeepSeekJsonSchemaImpl(String type) {
        this.type = type;
        this.properties = new JSONObject();
        this.required = new JSONArray();
        this.enumValues = new JSONArray();
        this.anyOfSchemas = new JSONArray();
        this.additionalProperties = false; // default
        this.anyOfMode = false;
    }

    @Override
    public DeepSeekJsonSchema description(String desc) {
        this.description = desc;
        return this;
    }

    @Override
    public DeepSeekJsonSchema property(String name, DeepSeekJsonSchema schema, boolean requiredField) {
        if (anyOfMode) {
            throw new IllegalStateException("Cannot add properties when building an anyOf schema directly.");
        }
        if (!"object".equals(type)) {
            throw new IllegalStateException("Can only add properties to an object schema.");
        }
        this.properties.put(name, schema.toJson());
        if (requiredField) {
            this.required.put(name);
        }
        return this;
    }

    @Override
    public DeepSeekJsonSchema items(DeepSeekJsonSchema itemSchema) {
        if (anyOfMode) {
            throw new IllegalStateException("Cannot set items in anyOf mode.");
        }
        if (!"array".equals(type)) {
            throw new IllegalStateException("items can only be defined for array schemas.");
        }
        this.itemsSchema = itemSchema;
        return this;
    }

    @Override
    public DeepSeekJsonSchema enumValues(String... values) {
        if (anyOfMode) {
            throw new IllegalStateException("Cannot set enum values in anyOf mode.");
        }
        if (this.type == null || !"string".equals(this.type)) {
            throw new IllegalStateException("Enum is currently only supported on string type schemas.");
        }
        for (String v : values) {
            this.enumValues.put(v);
        }
        return this;
    }

    @Override
    public DeepSeekJsonSchema additionalProperties(boolean allowed) {
        this.additionalProperties = allowed;
        return this;
    }

    void setAnyOfMode(boolean anyOfMode) {
        this.anyOfMode = anyOfMode;
    }

    JSONArray getAnyOfSchemas() {
        return this.anyOfSchemas;
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();

        if (anyOfMode) {
            obj.put("anyOf", anyOfSchemas);
            if (description != null && !description.isBlank()) {
                obj.put("description", description);
            }
            return obj;
        }

        if (type != null) {
            obj.put("type", type);
        }
        if (properties.length() > 0) {
            obj.put("properties", properties);
        }
        if (required.length() > 0) {
            obj.put("required", required);
        }
        if (enumValues.length() > 0) {
            obj.put("enum", enumValues);
        }
        if ("array".equals(type) && itemsSchema != null) {
            obj.put("items", itemsSchema.toJson());
        }

        obj.put("additionalProperties", additionalProperties);

        if (description != null && !description.isBlank()) {
            obj.put("description", description);
        }

        return obj;
    }
}
