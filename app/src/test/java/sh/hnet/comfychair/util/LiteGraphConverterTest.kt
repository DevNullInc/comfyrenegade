package sh.hnet.comfychair.util

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import sh.hnet.comfychair.workflow.InputDefinition
import sh.hnet.comfychair.workflow.InputValue
import sh.hnet.comfychair.workflow.NodeTypeDefinition
import sh.hnet.comfychair.workflow.OutputSlot
import sh.hnet.comfychair.workflow.WorkflowParser

class LiteGraphConverterTest {

    @Test
    fun testLoraStackWidgetValuesMapping() {
        val crLoraStackDef = NodeTypeDefinition(
            classType = "CR_LoRAStack",
            category = "loaders",
            inputs = listOf(
                InputDefinition(name = "switch_1", type = "ENUM", isRequired = true, options = listOf("On", "Off")),
                InputDefinition(name = "lora_name_1", type = "ENUM", isRequired = true, options = listOf("None", "anima\\R3.safetensors")),
                InputDefinition(name = "model_weight_1", type = "FLOAT", isRequired = true, default = 1.0),
                InputDefinition(name = "clip_weight_1", type = "FLOAT", isRequired = true, default = 1.0),
                InputDefinition(name = "switch_2", type = "ENUM", isRequired = true, options = listOf("On", "Off")),
                InputDefinition(name = "lora_name_2", type = "ENUM", isRequired = true, options = listOf("None", "anima\\le.safetensors")),
                InputDefinition(name = "model_weight_2", type = "FLOAT", isRequired = true, default = 1.0),
                InputDefinition(name = "clip_weight_2", type = "FLOAT", isRequired = true, default = 1.0)
            ),
            outputs = listOf(OutputSlot(type = "LORA_STACK", name = "LORA_STACK"))
        )

        val converter = LiteGraphConverter { classType ->
            if (classType == "CR_LoRAStack") crLoraStackDef else null
        }

        val liteGraphJson = JSONObject().apply {
            put("nodes", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 10)
                    put("type", "CR_LoRAStack")
                    // Leading 3 is lora_count widget in LiteGraph format!
                    put("widgets_values", JSONArray().apply {
                        put(3)
                        put("Off")
                        put("anima\\R3.safetensors")
                        put(1.0)
                        put(1.0)
                        put("Off")
                        put("anima\\le.safetensors")
                        put(1.01)
                        put(1.0)
                    })
                })
            })
            put("links", JSONArray())
        }

        val result = converter.convert(liteGraphJson)
        val apiJson = JSONObject(result.jsonContent)
        val nodes = apiJson.getJSONObject("nodes")
        val node10 = nodes.getJSONObject("10")
        val inputs = node10.getJSONObject("inputs")

        assertEquals("Off", inputs.getString("switch_1"))
        assertEquals("anima\\R3.safetensors", inputs.getString("lora_name_1"))
        assertEquals(1.0, inputs.getDouble("model_weight_1"), 0.001)
        assertEquals(1.0, inputs.getDouble("clip_weight_1"), 0.001)

        assertEquals("Off", inputs.getString("switch_2"))
        assertEquals("anima\\le.safetensors", inputs.getString("lora_name_2"))
        assertEquals(1.01, inputs.getDouble("model_weight_2"), 0.001)
        assertEquals(1.0, inputs.getDouble("clip_weight_2"), 0.001)
    }

    @Test
    fun testWorkflowParserSanitizesCorruptedLoraValues() {
        val parser = WorkflowParser()

        val jsonWithCorruptedValues = """
        {
          "10": {
            "class_type": "CR_LoRAStack",
            "inputs": {
              "switch_2": "Off",
              "lora_name_2": "false",
              "model_weight_2": "anima\\R3.safetensors",
              "clip_weight_2": 1.0
            }
          }
        }
        """.trimIndent()

        val graph = parser.parse(jsonWithCorruptedValues, "Test")
        val node = graph.nodes.find { it.id == "10" }
        assertNotNull(node)

        val loraNameInput = node!!.inputs["lora_name_2"] as InputValue.Literal
        val modelWeightInput = node.inputs["model_weight_2"] as InputValue.Literal

        assertEquals("None", loraNameInput.value)
        assertEquals(1.0, modelWeightInput.value)
    }

    @Test
    fun testUnconnectedLoraStackNotSerializedAsLiteral() {
        val parser = WorkflowParser()
        val serializer = sh.hnet.comfychair.workflow.WorkflowSerializer()

        val jsonWithLiteralLoraStack = """
        {
          "24": {
            "class_type": "Lora Stack [Eclipse]",
            "inputs": {
              "lora_stack": 10,
              "switch_1": "Off",
              "lora_name_1": "None",
              "model_weight_1": 1.0,
              "clip_weight_1": 1.0
            }
          }
        }
        """.trimIndent()

        val graph = parser.parse(jsonWithLiteralLoraStack, "Test")
        val serializedJson = serializer.serialize(graph)
        val apiJson = JSONObject(serializedJson)
        val node24Inputs = apiJson.getJSONObject("24").getJSONObject("inputs")

        // "lora_stack" must NOT be in serialized inputs JSON so ComfyUI gets None instead of int 10
        org.junit.Assert.assertFalse(node24Inputs.has("lora_stack"))
        assertEquals("Off", node24Inputs.getString("switch_1"))
        assertEquals("None", node24Inputs.getString("lora_name_1"))
    }
}
