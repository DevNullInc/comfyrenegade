package sh.hnet.comfychair.workflow

import org.json.JSONArray
import org.json.JSONObject
import sh.hnet.comfychair.util.DebugLogger

/**
 * Parses ComfyUI workflow JSON into a WorkflowGraph
 */
class WorkflowParser {

    companion object {
        private const val TAG = "WorkflowParser"

        /**
         * Check if an input name belongs to a connection slot (e.g. lora_stack, model, image).
         * Used to prevent serializing primitive literal values into connection inputs.
         */
        fun isConnectionInputName(inputName: String): Boolean {
            val lower = inputName.lowercase()
            return lower in setOf(
                "model", "clip", "vae", "positive", "negative", "conditioning",
                "latent", "latent_image", "image", "images", "mask", "sigmas",
                "guides", "guider", "sampler", "lora_stack", "control_net_stack",
                "model_stack", "cnet_stack", "sam_model_opt", "segm_detector_opt",
                "bbox_detector", "detailer_hook"
            ) || lower.endsWith("_stack")
        }
    }

    /**
     * Parse workflow JSON content into a WorkflowGraph
     */
    fun parse(jsonContent: String, workflowName: String = "", workflowDescription: String = ""): WorkflowGraph {
        val json = JSONObject(jsonContent)

        // Try to detect name and description if not provided
        val finalName = if (workflowName.isEmpty()) {
            sh.hnet.comfychair.util.WorkflowJsonAnalyzer.detectWorkflowName(json)
        } else {
            workflowName
        }

        val finalDescription = if (workflowDescription.isEmpty()) {
            sh.hnet.comfychair.util.WorkflowJsonAnalyzer.detectWorkflowDescription(json)
        } else {
            workflowDescription
        }

        DebugLogger.d(TAG, "Parsing workflow: name=$finalName")

        val nodes = mutableListOf<WorkflowNode>()
        val edges = mutableListOf<WorkflowEdge>()
        val groups = mutableListOf<WorkflowGroup>()
        val allTemplateVars = mutableSetOf<String>()
        val mappedFields = mutableMapOf<Pair<String, String>, String>()

        // Parse groups if present (at root level)
        if (json.has("groups")) {
            DebugLogger.d(TAG, "Found 'groups' key in JSON at root level")
            val groupsArray = json.optJSONArray("groups")
            if (groupsArray != null) {
                DebugLogger.d(TAG, "Groups array has ${groupsArray.length()} entries")
                groups.addAll(parseGroups(groupsArray))
            } else {
                DebugLogger.w(TAG, "'groups' key exists but is not a JSON array")
            }
        } else {
            DebugLogger.d(TAG, "No 'groups' key found in JSON root")
        }

        // Determine the structure: our custom format has "nodes" object, ComfyUI API format has nodes at top level
        val nodesJson = if (json.has("nodes") && json.get("nodes") is JSONObject) {
            // Our custom workflow format: { "name": "...", "description": "...", "nodes": { ... } }
            json.getJSONObject("nodes")
        } else {
            // Standard ComfyUI API format: nodes at top level
            json
        }

        // Parse each node - workflow JSON has node IDs as keys
        val nodeIds = nodesJson.keys()
        for (nodeId in nodeIds) {
            val nodeValue = nodesJson.get(nodeId)
            if (nodeValue !is JSONObject) continue

            val nodeJson = nodeValue
            val classType = nodeJson.optString("class_type", "Unknown")
            val meta = nodeJson.optJSONObject("_meta")
            val title = meta?.optString("title")?.takeIf { it.isNotEmpty() } ?: classType

            val inputsJson = nodeJson.optJSONObject("inputs") ?: JSONObject()
            val inputs = parseInputs(inputsJson)

            // Find inputs that contain template placeholders like {{positive_prompt}}
            val templateKeys = mutableSetOf<String>()
            inputs.forEach { (key, value) ->
                if (value is InputValue.Literal) {
                    val stringValue = value.value.toString()
                    // Check for {{placeholder}} patterns
                    val placeholderRegex = """\{\{(\w+)\}\}""".toRegex()
                    val matches = placeholderRegex.findAll(stringValue)
                    for (match in matches) {
                        val placeholderName = match.groupValues[1]
                        // Store the input key that contains a template placeholder
                        templateKeys.add(key)
                        // Also track the placeholder name for the graph
                        allTemplateVars.add(placeholderName)
                        // Track mapped field: (nodeId, inputName) -> placeholderName
                        mappedFields[Pair(nodeId, key)] = placeholderName
                    }
                }
            }

            // Also check for keys that are known template targets (for workflows using direct key names)
            inputs.keys.filter { key ->
                TemplateKeyRegistry.isTemplateKey(key)
            }.forEach { key ->
                templateKeys.add(key)
                allTemplateVars.add(key)
            }

            // Parse node mode (0=active, 2=muted, 4=bypassed)
            val mode = nodeJson.optInt("mode", 0)
            if (mode != 0) {
                DebugLogger.d(TAG, "Parsed node $nodeId with mode=$mode (${if (mode == 4) "bypassed" else if (mode == 2) "muted" else "unknown"})")
            }

            // Parse manual position from _meta.position array [x, y]
            val positionArray = meta?.optJSONArray("position")
            val hasManualPosition = positionArray != null && positionArray.length() >= 2
            val manualX = if (hasManualPosition) positionArray!!.optDouble(0, 0.0).toFloat() else 0f
            val manualY = if (hasManualPosition) positionArray!!.optDouble(1, 0.0).toFloat() else 0f
            if (hasManualPosition) {
                DebugLogger.d(TAG, "Parsed node $nodeId with manual position: ($manualX, $manualY)")
            }

            nodes.add(
                WorkflowNode(
                    id = nodeId,
                    classType = classType,
                    title = title,
                    category = categorizeNode(classType),
                    inputs = sortInputsForLayout(inputs),
                    templateInputKeys = templateKeys,
                    mode = mode,
                    x = manualX,
                    y = manualY,
                    hasManualPosition = hasManualPosition
                )
            )

            // Extract edges from connection inputs
            inputs.forEach { (inputName, value) ->
                if (value is InputValue.Connection) {
                    edges.add(
                        WorkflowEdge(
                            sourceNodeId = value.sourceNodeId,
                            sourceOutputIndex = value.outputIndex,
                            targetNodeId = nodeId,
                            targetInputName = inputName
                        )
                    )
                }
            }
        }

        // Parse notes if present (at root level)
        val notes = if (json.has("notes")) {
            DebugLogger.d(TAG, "Found 'notes' key in JSON at root level")
            val notesArray = json.optJSONArray("notes")
            if (notesArray != null) {
                DebugLogger.d(TAG, "Notes array has ${notesArray.length()} entries")
                parseNotes(notesArray)
            } else {
                DebugLogger.w(TAG, "'notes' key exists but is not a JSON array")
                emptyList()
            }
        } else {
            DebugLogger.d(TAG, "No 'notes' key found in JSON root")
            emptyList()
        }

        // Parse manual placement mode flag from root level
        val isManualPlacementMode = json.optBoolean("manual_placement", false)
        if (isManualPlacementMode) {
            DebugLogger.d(TAG, "Workflow has manual_placement mode enabled")
        }

        DebugLogger.d(TAG, "Parsed workflow: ${nodes.size} nodes, ${edges.size} edges, ${groups.size} groups, ${notes.size} notes, ${allTemplateVars.size} template vars, ${mappedFields.size} mapped fields")

        // Prioritize output nodes (SaveImage, PreviewImage) by putting them first in the list
        // so they are preferred by components that pick the "first" or "primary" output.
        val outputNodeClasses = setOf("SaveImage", "PreviewImage", "VHS_VideoCombine", "SaveAnimatedPNG")
        val sortedNodes = nodes.sortedByDescending { it.classType in outputNodeClasses }

        return WorkflowGraph(
            name = finalName,
            description = finalDescription,
            nodes = sortedNodes,
            edges = edges,
            groups = groups,
            notes = notes,
            templateVariables = allTemplateVars,
            mappedFields = mappedFields,
            isManualPlacementMode = isManualPlacementMode
        )
    }

    /**
     * Parse groups array from workflow JSON.
     *
     * New format (ComfyChair): { id, title, member_nodes }
     * Legacy format (ComfyUI): { id, title, bounding } - groups without member_nodes are skipped
     *
     * Groups with no members are skipped.
     */
    private fun parseGroups(groupsArray: JSONArray): List<WorkflowGroup> {
        DebugLogger.d(TAG, "parseGroups: parsing ${groupsArray.length()} group entries")
        val result = mutableListOf<WorkflowGroup>()

        for (i in 0 until groupsArray.length()) {
            val groupJson = groupsArray.optJSONObject(i)
            if (groupJson == null) {
                DebugLogger.w(TAG, "parseGroups: entry $i is not a JSON object, skipping")
                continue
            }

            val id = groupJson.optInt("id", -1)
            if (id < 0) {
                DebugLogger.w(TAG, "parseGroups: entry $i has invalid id ($id), skipping")
                continue
            }

            val title = groupJson.optString("title", "Group")
            val hasMemberNodes = groupJson.has("member_nodes")
            val hasBounding = groupJson.has("bounding")

            DebugLogger.d(TAG, "parseGroups: entry $i - id=$id, title='$title', has member_nodes=$hasMemberNodes, has bounding=$hasBounding")

            // Parse member node IDs (required for new format)
            val memberNodeIds = groupJson.optJSONArray("member_nodes")?.let { arr ->
                val ids = (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotEmpty() } }.toSet()
                DebugLogger.d(TAG, "parseGroups: group $id member_nodes array has ${arr.length()} entries, parsed ${ids.size} valid IDs: $ids")
                ids
            } ?: run {
                DebugLogger.d(TAG, "parseGroups: group $id has no member_nodes array (legacy format?)")
                emptySet()
            }

            // Skip groups with no members (including legacy groups without member_nodes)
            if (memberNodeIds.isEmpty()) {
                DebugLogger.w(TAG, "parseGroups: skipping group $id '$title': has no members")
                continue
            }

            result.add(
                WorkflowGroup(
                    id = id,
                    title = title,
                    memberNodeIds = memberNodeIds
                )
            )
            DebugLogger.i(TAG, "parseGroups: successfully parsed group $id '$title' with ${memberNodeIds.size} members")
        }

        DebugLogger.i(TAG, "parseGroups: parsed ${result.size} valid groups out of ${groupsArray.length()} entries")
        return result
    }

    /**
     * Parse notes array from workflow JSON.
     *
     * Format: { id, title, content }
     */
    private fun parseNotes(notesArray: JSONArray): List<WorkflowNote> {
        DebugLogger.d(TAG, "parseNotes: parsing ${notesArray.length()} note entries")
        val result = mutableListOf<WorkflowNote>()

        for (i in 0 until notesArray.length()) {
            val noteJson = notesArray.optJSONObject(i)
            if (noteJson == null) {
                DebugLogger.w(TAG, "parseNotes: entry $i is not a JSON object, skipping")
                continue
            }

            val id = noteJson.optInt("id", -1)
            if (id < 0) {
                DebugLogger.w(TAG, "parseNotes: entry $i has invalid id ($id), skipping")
                continue
            }

            val title = noteJson.optString("title", "Note")
            val content = noteJson.optString("content", "")

            // Parse manual position from position array [x, y]
            val positionArray = noteJson.optJSONArray("position")
            val hasManualPosition = positionArray != null && positionArray.length() >= 2
            val manualX = if (hasManualPosition) positionArray!!.optDouble(0, 0.0).toFloat() else 0f
            val manualY = if (hasManualPosition) positionArray!!.optDouble(1, 0.0).toFloat() else 0f

            result.add(
                WorkflowNote(
                    id = id,
                    title = title,
                    content = content,
                    x = manualX,
                    y = manualY,
                    hasManualPosition = hasManualPosition
                )
            )
            if (hasManualPosition) {
                DebugLogger.d(TAG, "parseNotes: parsed note id=$id title='$title' with manual position: ($manualX, $manualY)")
            } else {
                DebugLogger.d(TAG, "parseNotes: parsed note id=$id title='$title' (${content.length} chars)")
            }
        }

        DebugLogger.i(TAG, "parseNotes: parsed ${result.size} notes out of ${notesArray.length()} entries")
        return result
    }

    /**
     * Parse inputs from a node's inputs JSON object
     */
    private fun parseInputs(inputsJson: JSONObject): Map<String, InputValue> {
        val result = mutableMapOf<String, InputValue>()

        for (key in inputsJson.keys()) {
            val value = inputsJson.get(key)
            result[key] = when {
                // Connection format: ["nodeId", outputIndex]
                value is JSONArray && value.length() == 2 -> {
                    try {
                        InputValue.Connection(
                            sourceNodeId = value.getString(0),
                            outputIndex = value.getInt(1)
                        )
                    } catch (e: Exception) {
                        // If parsing fails, treat as literal
                        DebugLogger.w(TAG, "Failed to parse connection for input '$key': ${e.message}")
                        InputValue.Literal(sanitizeInputValue(key, value.toString()))
                    }
                }
                isConnectionInputName(key) -> {
                    // Primitive value on a connection slot (e.g. "lora_stack": 10 from bad conversion)
                    // Treat as UnconnectedSlot so it's not serialized as literal to ComfyUI
                    InputValue.UnconnectedSlot(key)
                }
                else -> InputValue.Literal(sanitizeInputValue(key, value))
            }
        }

        return result
    }

    /**
     * Format and sanitize a literal value for display, repairing broken Lora stack node values.
     */
    private fun sanitizeInputValue(key: String, value: Any): Any {
        val formatted = formatLiteralValue(value)
        val keyLower = key.lowercase()
        val strVal = formatted.toString()

        // Fix lora_name showing boolean or switch values ("false", "true", "Off", "On")
        if (keyLower.startsWith("lora_name") || keyLower == "lora") {
            if (value is Boolean || strVal.lowercase() in setOf("false", "true", "off", "on")) {
                return "None"
            }
        }

        // Fix model_weight / clip_weight showing string lora names (e.g. "anima\R3...") or booleans
        if (keyLower.startsWith("model_weight") || keyLower.startsWith("clip_weight") || keyLower.contains("weight")) {
            if (value is Boolean || (value is String && strVal.toDoubleOrNull() == null)) {
                return 1.0
            }
        }

        return formatted
    }

    /**
     * Format a literal value for display
     */
    private fun formatLiteralValue(value: Any): Any {
        return when (value) {
            is String -> value
            is Number -> value
            is Boolean -> value
            else -> value.toString()
        }
    }

    /**
     * Categorize a node by its class type
     */
    private fun categorizeNode(classType: String): NodeCategory {
        return when {
            // Loaders
            classType.contains("Loader", ignoreCase = true) -> NodeCategory.LOADER

            // Text encoders
            classType.contains("CLIPTextEncode", ignoreCase = true) ||
            classType.contains("TextEncode", ignoreCase = true) -> NodeCategory.ENCODER

            // Samplers
            classType.contains("Sampler", ignoreCase = true) -> NodeCategory.SAMPLER

            // Latent image creation
            classType.contains("EmptyLatent", ignoreCase = true) ||
            classType.contains("Empty") && classType.contains("Latent", ignoreCase = true) -> NodeCategory.LATENT

            // Input nodes
            classType.contains("LoadImage", ignoreCase = true) -> NodeCategory.INPUT

            // Output nodes
            classType.contains("Save", ignoreCase = true) ||
            classType.contains("Preview", ignoreCase = true) -> NodeCategory.OUTPUT

            // Processing nodes
            classType.contains("Decode", ignoreCase = true) ||
            classType.contains("Encode", ignoreCase = true) ||
            classType.contains("Scale", ignoreCase = true) ||
            classType.contains("Create", ignoreCase = true) ||
            classType.contains("Sampling", ignoreCase = true) ||
            classType.contains("ModelMerge", ignoreCase = true) -> NodeCategory.PROCESS

            else -> NodeCategory.OTHER
        }
    }
}
