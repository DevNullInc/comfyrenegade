# ComfyRenegade Features & Capabilities

**ComfyRenegade** is an unofficial, native Android UI for [ComfyUI](https://github.com/comfyanonymous/ComfyUI), designed for high-performance AI image and video generation directly from your mobile device.

---

## 1. Advanced Workflow Engine & Graph Processing

### Subgraph Flattening & Execution Engine
- **Nested Subgraph Support**: Full recursive parsing and flattening of nested LiteGraph subgraphs.
- **ID Remapping**: Automatically remaps internal subgraph node IDs and link IDs into unique non-colliding ranges during conversion.
- **Virtual Input/Output Resolution**: Converts virtual subgraph input/output nodes into direct inter-node connections for server API execution.

### Smart VAE Connection Repair
- **Filename-Aware Matching**: Automatically inspects `vae_name` across all active `VAELoader` nodes in a workflow and links unlinked decode/detailer nodes to the exact loader matching the target filename.
- **Custom Node Protection**: Preserves dedicated internal VAE parameters on specialty nodes (such as Lotus Depth estimators) without overriding them with the main model VAE.
- **Auto-Linking Fallback**: Automatically connects unlinked `VAEDecode`, `FaceDetailer`, `DetailerForEach`, and `FaceDetailerPipe` nodes to the active VAE loader, preventing server runtime exceptions (`'str' object has no attribute 'decode'`).

### Single-Source Seed Propagation
- **Primary Seed Extraction**: Reads active seed values from primary seed generator nodes (`ttN seed`, `Seed`, `PrimitiveNode`).
- **Downstream Detailer Sync**: Automatically passes the primary seed value into downstream detailer nodes (`FaceDetailer`, `HandDetailer`, `EyeDetailer`, `BreastDetailer`, `NippleDetailer`).
- **Dangling Link Repair**: Resolves broken or dangling seed connection links (pointing to missing internal/subgraph node IDs) into direct integer seed values, eliminating `Exception when validating inner node` server errors.

### Connection Slot Protection
- **Link Guarding**: Guards connection input slots (`vae`, `model`, `clip`, `lora_stack`, `positive`, `negative`, `latent_image`, `mask`, `sigmas`) from being overwritten by primitive widget values or string filenames during user edits.
- **Primitive Value Removal**: Automatically cleans invalid primitive integers or strings (e.g. `lora_stack: 1`) assigned to connection slots, allowing custom nodes (such as `ComfyUI Eclipse` LoRA stack loaders) to receive `None` defaults cleanly.

### Intelligent Parameter & Widget Mapping
- **Smart Jump-Matching**: Pattern- and type-aware jump-matching for sampler algorithm names (`dpmpp_2m`, `euler`), scheduler names (`karras`, `normal`), and large seed values (`>99999`).
### Workflow Viewer & Canvas Optimization
- **Stateless Viewport Calculator**: High-performance, pure functional viewport transform calculations (`GraphViewportCalculator`) for smooth panning, pinch-to-zoom, and focal point tracking.
- **Adaptive Canvas Auto-Fitting**: Smart `fitToScreen`, `fitToWidth`, and `fitToHeight` viewport operations with strict scale clamping (`MIN_SCALE = 0.2f`, `MAX_SCALE = 3.0f`, `MAX_FIT_SCALE = 1.5f`) preventing over-zooming on small graphs.
- **Side-Sheet Layout Adaptation**: Dynamically recalculates visible canvas width and centers focused nodes when opening or closing the node attribute side-sheet, avoiding UI clipping.
- **Optimized Connection Wire Rendering**: Fast Compose canvas rendering for Bezier and Hermite connection wires with animated flowing selection highlights.
- **Asynchronous Mini-Graph Thumbnails**: Background preview generator rendering compact graph thumbnails for workflow lists.

---

## 2. Full Workflow Import & API Compatibility

### Multi-Format Import Support
- **ComfyUI LiteGraph (`.json`)**: Import native ComfyUI web canvas workflows directly, including node positions, groups, notes, and subgraphs.
- **ComfyUI API JSON (`.json`)**: Import and export server API prompt payloads (both flat node dictionaries and wrapped JSON format).
- **ComfyRenegade JSON (`.json`)**: Native format preserving field mappings, node attribute edits, UI placeholder mappings (`{{positive_prompt}}`, `{{seed}}`, `{{vae}}`), and per-workflow settings.

### Dynamic Workflow Validation
- **Server Schema Validation**: Validates custom workflow node class types against the connected ComfyUI server's `/object_info` catalog.
- **Placeholder Extraction**: Scans workflow definitions for template placeholders (`{{width}}`, `{{height}}`, `{{steps}}`, `{{cfg}}`, `{{sampler_name}}`, `{{scheduler}}`, `{{seed}}`, `{{denoise}}`).

---

## 3. Custom Node Ecosystem Compatibility

ComfyRenegade includes built-in fallback definitions and smart handling for popular ComfyUI custom node packs:
- **ComfyUI Impact Pack**: Full support for `FaceDetailer`, `FaceDetailerPipe`, `DetailerForEach`, `DetailerForEachPipe`, and `UltralyticsDetectorProvider`.
- **ComfyUI Eclipse**: Support for `Lora Stack [Eclipse]` and `Lora Stack apply [Eclipse]` nodes.
- **RES4LYF**: Support for custom samplers and guiders (`ClownsharKSampler_Beta`, `SharkGuider`).
- **Ultralytics Detector Providers**: Support for YOLO face, body, hand, breast, nipple, and hair detector models (`.pt`).

---

## 4. Generation Modes & Pipeline Support

### Text to Image (TTI)
- Support for Checkpoint (`CheckpointLoaderSimple`) and UNET (`UNETLoader`, `CLIPLoader`, `VAELoader`) workflows.
- Real-time generation progress updates and live preview websocket streams.

### Image to Image (ITI)
- **Editing Mode**: Image-to-image transformations with reference image inputs.
- **Inpainting Mode**: Built-in canvas mask editor with adjustable brush size and feathered edges.

### Text to Video (TTV) & Image to Video (ITV)
- High Noise / Low Noise UNET and LoRA model selection for multi-stage video models (e.g. Wan2.1, LTX-Video).
- Adjustable video length (frame count) and frame rate (FPS).

---

## 5. LoRA Stacking & Dynamic Injection

- **Multi-LoRA Chains**: Stack up to 5 LoRAs per chain with individual strength sliders (-5.0 to +5.0).
- **Separate Noise Stage LoRAs**: Independent LoRA chains for Checkpoint mode, UNET mode, High Noise video stage, and Low Noise video stage.
- **Dynamic Injection**: LoRAs are injected into model/CLIP connection paths dynamically at generation time.

---

## 6. Server & Infrastructure Management

- **Multi-Server Management**: Add, edit, and switch between multiple ComfyUI servers with saved credentials and per-server settings.
- **Flexible Authentication**: Support for HTTP Basic Auth and API Key/Bearer token authentication.
- **Server Resource Monitoring**: Real-time display of server hardware information (ComfyUI version, OS, Python/PyTorch versions, system RAM, and GPU VRAM usage).
- **Offline Mode**: Browsing of cached generated media and workflows when server is unreachable.

---

## 7. Media Gallery & Fullscreen Viewer

- **2-Column Grid Gallery**: Browsing of generated images and videos with pull-to-refresh and thumbnail video indicators.
- **Fullscreen Viewer**: Pinch-to-zoom, double-tap zoom, swipe navigation, floating Material 3 toolbar, and metadata inspection (prompts, model, seed, steps, etc.).
- **Batch Operations**: Multi-select mode for batch saving to gallery (`Pictures/ComfyRenegade` or `Movies/ComfyRenegade`), sharing, or deleting.

---

## 8. Prompt Preset Library

- **Preset Organization**: Save reusable prompts with custom titles, tags, and favorite toggles.
- **Search & Filter**: Search presets by title, prompt text, or tag filters.
- **Quick Insertion**: Instant insertion from prompt field dropdowns.

---

## 9. Security, Backup, & Data Management

- **Local-Only Storage**: All server settings, credentials, prompts, and cached media stay strictly on your local Android device.
- **Backup & Restore**: Export and import full app configuration backups in JSON format.
- **HTTPS & Self-Signed Certs**: Connection support for self-signed certificates and SSL/TLS reverse proxies.

---

## 10. Technical Specifications

- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material Design 3 (Expressive components)
- **Min SDK**: Android 13 (API 33)
- **Target SDK**: Android 15 (API 36)
- **Architecture**: MVVM with ViewModels, StateFlow, and Kotlin Coroutines
- **License**: GPL-3.0 Open Source
