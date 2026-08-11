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

        org.junit.Assert.assertFalse(inputs.getBoolean("switch_1"))
        assertEquals("anima\\R3.safetensors", inputs.getString("lora_name_1"))
        assertEquals(1.0, inputs.getDouble("model_weight_1"), 0.001)
        assertEquals(1.0, inputs.getDouble("clip_weight_1"), 0.001)

        org.junit.Assert.assertFalse(inputs.getBoolean("switch_2"))
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

    @Test
    fun testFaceDetailerFallbackWidgetMapping() {
        val converter = LiteGraphConverter { null } // No server definitions registered

        val liteGraphJson = JSONObject().apply {
            put("nodes", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 7)
                    put("type", "FaceDetailer")
                    put("widgets_values", JSONArray().apply {
                        put(512.0) // guide_size
                        put(true)  // guide_size_for
                        put(1024.0)// max_size
                        put(0)     // offset_x
                        put(0)     // offset_y
                        put(3.0)   // crop_factor
                        put(10)    // drop_size
                        put("")    // wildcard
                        put(1)     // cycle
                        put(12345) // seed
                        put("randomize") // control_after_generate (frontend only)
                        put(20)    // steps
                        put(8.0)   // cfg
                        put("euler") // sampler_name
                        put("normal") // scheduler
                        put(0.5)   // denoise
                        put(5)     // feather
                    })
                })
            })
            put("links", JSONArray())
        }

        val result = converter.convert(liteGraphJson)
        val apiJson = JSONObject(result.jsonContent)
        val node7Inputs = apiJson.getJSONObject("nodes").getJSONObject("7").getJSONObject("inputs")

        assertEquals(20, node7Inputs.getInt("steps"))
        assertEquals(8.0, node7Inputs.getDouble("cfg"), 0.001)
        assertEquals("euler", node7Inputs.getString("sampler_name"))
        assertEquals("normal", node7Inputs.getString("scheduler"))
        assertEquals(0.5, node7Inputs.getDouble("denoise"), 0.001)
        assertEquals(12345, node7Inputs.getInt("seed"))
    }

    @Test
    fun testSubgraphVaeConnectionNotOverwrittenByStringWidget() {
        val converter = LiteGraphConverter { null }

        // Test that proxy widgets containing string filenames ("Wan2_1_VAE_fp32.safetensors") for VAE input
        // are NEVER mapped into a VAE connection input slot in subgraphs.
        val liteGraphJson = JSONObject().apply {
            put("definitions", JSONObject().apply {
                put("subgraphs", JSONArray().apply {
                    put(JSONObject().apply {
                        put("id", "test-subgraph")
                        put("inputNodeId", -10)
                        put("outputNodeId", -20)
                        put("inputs", JSONArray().apply {
                            put(JSONObject().apply {
                                put("name", "vae")
                                put("type", "VAE")
                            })
                        })
                        put("nodes", JSONArray().apply {
                            put(JSONObject().apply {
                                put("id", 100)
                                put("type", "VAEDecode")
                                put("inputs", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("name", "vae")
                                        put("type", "VAE")
                                        put("link", 500)
                                    })
                                })
                            })
                        })
                        put("links", JSONArray().apply {
                            put(JSONArray().apply {
                                put(500) // linkId
                                put(-10) // originId (virtual input node)
                                put(0)   // originSlot (vae slot)
                                put(100) // targetId (VAEDecode)
                                put(0)   // targetSlot
                                put("VAE")
                            })
                        })
                    })
                })
            })
            put("nodes", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 2)
                    put("type", "test-subgraph")
                    put("properties", JSONObject().apply {
                        put("proxyWidgets", JSONArray().apply {
                            put(JSONArray().apply {
                                put(100)
                                put("vae")
                            })
                        })
                    })
                    put("widgets_values", JSONArray().apply {
                        put("Wan2_1_VAE_fp32.safetensors")
                    })
                })
            })
            put("links", JSONArray())
        }

        val result = converter.convert(liteGraphJson)
        val apiJson = JSONObject(result.jsonContent)
        val nodes = apiJson.getJSONObject("nodes")
        val internalVaeNode = nodes.optJSONObject("102") // 100 + offset 2
        val inputs = internalVaeNode?.optJSONObject("inputs")

        // "vae" connection input MUST NOT contain string filename "Wan2_1_VAE_fp32.safetensors"
        if (inputs != null && inputs.has("vae")) {
            org.junit.Assert.assertFalse(inputs.opt("vae") is String)
        }
    }

    @Test
    fun testWikkedAnimaV4FullWorkflowConversion() {
        val file = java.io.File("../Example workflows/Wikked Anima V4-full.json")
        if (!file.exists()) {
            println("File does not exist: ${file.absolutePath}")
            return
        }

        val jsonString = file.readText()
        val liteGraphJson = JSONObject(jsonString)

        val converter = LiteGraphConverter { null }
        val result = converter.convert(liteGraphJson)

        val apiJson = JSONObject(result.jsonContent)
        val nodes = apiJson.getJSONObject("nodes")

        // 1. Verify Lora Stack [Eclipse] node #24
        val node24 = nodes.getJSONObject("24")
        val node24Inputs = node24.getJSONObject("inputs")
        assertEquals("standard", node24Inputs.getString("mode"))
        assertEquals(10, node24Inputs.getInt("lora_count"))
        org.junit.Assert.assertTrue(node24Inputs.getBoolean("switch_1"))
        assertEquals("anima\\fucked_sensless_Anima_epoch_8.safetensors", node24Inputs.getString("lora_name_1"))
        org.junit.Assert.assertFalse(node24Inputs.getBoolean("switch_2"))
        assertEquals("anima\\R3alB3auty_ANIMA.safetensors", node24Inputs.getString("lora_name_2"))
        org.junit.Assert.assertTrue(node24Inputs.getBoolean("switch_3"))
        assertEquals("anima\\lenovo_ultrareal_anima_base1.safetensors", node24Inputs.getString("lora_name_3"))

        // 2. Verify VAEDecode node #46 has connection link, not string filename
        val node46 = nodes.getJSONObject("46")
        val node46Inputs = node46.getJSONObject("inputs")
        val vaeConn = node46Inputs.getJSONArray("vae")
        assertEquals("49", vaeConn.getString(0))
        assertEquals(0, vaeConn.getInt(1))

        // 3. Verify all FaceDetailer nodes inside subgraph #2 have required execution parameters
        val keys = nodes.keys()
        var faceDetailerCount = 0
        while (keys.hasNext()) {
            val key = keys.next()
            val nodeObj = nodes.getJSONObject(key)
            if (nodeObj.optString("class_type") == "FaceDetailer") {
                faceDetailerCount++
                val nodeInputs = nodeObj.getJSONObject("inputs")
                org.junit.Assert.assertTrue(nodeInputs.has("steps"))
                org.junit.Assert.assertTrue(nodeInputs.has("cfg"))
                org.junit.Assert.assertTrue(nodeInputs.has("sampler_name"))
                org.junit.Assert.assertTrue(nodeInputs.has("scheduler"))
                org.junit.Assert.assertTrue(nodeInputs.has("denoise"))
            }
        }
        org.junit.Assert.assertTrue("Should find FaceDetailer nodes", faceDetailerCount > 0)
    }
}
