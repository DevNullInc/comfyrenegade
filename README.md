# <img src="metadata/en-US/icon.png" width="42"/> ComfyRenegade

An unofficial, native Android UI for [ComfyUI](https://github.com/comfyanonymous/ComfyUI). Forked from ComfyChair with advanced workflow graph processing, full subgraph import support, and enhanced custom node compatibility.

**Current version**: v0.8.12

[<img src="https://raw.githubusercontent.com/rubenpgrady/get-it-on-github/refs/heads/main/get-it-on-github.png"
    alt="Get it on GitHub"
    height="80">](https://github.com/legal-hkr/ComfyRenegade/releases/latest)
[<img src="https://f-droid.org/badge/get-it-on.png"
    alt="Get it on F-Droid"
    height="80">](https://f-droid.org/packages/sh.hnet.ComfyRenegade)

## Overview

ComfyRenegade provides a streamlined mobile interface for interacting with ComfyUI servers, allowing you to generate and manage AI images and videos directly from your Android device. The app communicates with your ComfyUI server via its API, bringing the power of advanced node-based AI generation and complex workflows to your mobile device.

**Note**: This is an independent, community-developed project and is not officially affiliated with or endorsed by the ComfyUI team.

## Screenshots

<img src="metadata/en-US/images/phoneScreenshots/1.png" width="200"/> <img src="metadata/en-US/images/phoneScreenshots/2.png" width="200"/> <img src="metadata/en-US/images/phoneScreenshots/3.png" width="200"/> <img src="metadata/en-US/images/phoneScreenshots/4.png" width="200"/> <img src="metadata/en-US/images/phoneScreenshots/5.png" width="200"/> <img src="metadata/en-US/images/phoneScreenshots/6.png" width="200"/> <img src="metadata/en-US/images/phoneScreenshots/7.png" width="200"/> <img src="metadata/en-US/images/phoneScreenshots/8.png" width="200"/>

## Features

### General

- **Multi-server support**: Save and manage multiple ComfyUI server connections with per-server settings and optional auto-login
- **Server connection**: Connect to remote or local ComfyUI servers with automatic HTTP/HTTPS detection and self-signed certificate support
- **Authentication**: HTTP Basic Auth and API key/Bearer token authentication for servers behind reverse proxies
- **Offline mode**: Browse cached data without network connection, with automatic offline prompt when server is unreachable
- **Queue management**: Submit multiple jobs without waiting, view queue count, cancel current job, add to front of queue, or clear queue entirely
- **Dual workflow support**:
  - **Checkpoint mode**: Traditional CheckpointLoaderSimple workflows
  - **UNET mode**: Modern diffusion workflows (Flux, Z-Image, etc.) with separate UNET, VAE, and CLIP model selection
- **LoRA chain support**:
  - Add up to 5 LoRAs per chain with individual strength control (-5.0-5.0)
  - Text to Image and Image to Image: Separate LoRA chains for Checkpoint and UNET modes
  - Text to Video and Image to Video: Separate High Noise and Low Noise LoRA chains
  - LoRAs are dynamically injected into workflows at generation time
- **Prompt Library**:
  - Save prompts as reusable presets with custom names and tags
  - Organize presets with favorites and tag-based filtering
  - Quick access via dropdown or full library dialog
  - Search presets by name, prompt text, or tags
- **Localization**: Available in English (default), German, French, Polish, Spanish, and Chinese with per-app language selection
- **Native Android experience**: Built with Kotlin and Jetpack Compose with Material Design 3

### Advanced Workflow Engine & Full Import

- **Full Workflow Import Capabilities**:
  - Import native ComfyUI web workflows (`.json` LiteGraph format), direct ComfyUI API prompt payloads (`.json` API format), and native ComfyRenegade workflows.
- **Subgraph Flattening & Execution Engine**:
  - Full support for nested subgraphs (e.g. `massive-detail`), automatically expanding subgraphs into executable flat API graphs at generation time while remapping internal node IDs and resolving virtual input/output connections.
- **Smart VAE Auto-Linking & Multi-Loader Resolution**:
  - Auto-detects missing or unlinked VAE connections on decode and detailer nodes (`VAEDecode`, `FaceDetailer`, `DetailerForEach`, `FaceDetailerPipe`), matching target VAEs by inspecting `vae_name` filenames across multiple VAE loader nodes.
  - Preserves dedicated internal VAEs on custom nodes (such as Lotus Depth estimators).
- **Single-Source Seed Propagation**:
  - Dedicated seed generator nodes (`ttN seed`, `Seed`) automatically propagate their active seed values directly to downstream detailer nodes (`FaceDetailer`, `HandDetailer`, `EyeDetailer`, `BreastDetailer`, `NippleDetailer`).
  - Automatically resolves dangling subgraph seed links pointing to missing internal nodes.
- **Connection Slot Protection**:
  - Guards active node connection links (`vae`, `model`, `clip`, `lora_stack`, `positive`, `negative`, `latent_image`) from being accidentally overwritten by primitive widget values or string filenames during node attribute edits.
- **Intelligent Parameter & Widget Mapping**:
  - Type- and pattern-aware jump-matching for sampler names (`dpmpp_2m`, `euler`), scheduler names (`karras`, `normal`), seeds (`>99999`), and multi-widget frontend skips on complex stack nodes (e.g. `Lora Stack [Eclipse]`).
- **Enhanced Custom Node Compatibility**:
  - Built-in support and fallback definitions for popular custom node ecosystems including ComfyUI Impact Pack, ComfyUI Eclipse, RES4LYF, and Ultralytics Detector Providers.

### Generation Modes

- **Text to Image**:
  - Mobile-optimized interface with live progress updates
  - Live preview images during generation (when supported by server)
- **Image to Image**:
  - Two modes: **Editing** (transformation) and **Inpainting** (mask-based)
  - **Editing mode**: Transform images with optional reference images (up to 2)
  - **Inpainting mode**: Mask editor with adjustable brush size and feathered edges
  - Live progress updates and preview images during generation
- **Text to Video**:
  - Generate AI videos with High Noise / Low Noise UNET and LoRA model selection
  - Live preview during generation
- **Image to Video**:
  - Animate still images into videos with adjustable length and frame rate
  - High Noise / Low Noise UNET and LoRA model selection
  - Live preview during generation

### Media

- **Media viewer**:
  - Unified fullscreen viewer for images and videos
  - Material Design 3 Expressive UI with floating toolbar and FAB
  - Swipe navigation between gallery items with tonal navigation buttons
  - Pinch-to-zoom and double-tap to toggle between fit and crop zoom
  - Single-tap to toggle UI visibility (with system bars)
  - Quick actions: delete, save to gallery, share, view metadata
  - Metadata viewer showing generation parameters (prompt, model, seed, steps, etc.)
  - Optimized media caching for smooth transitions between items
- **Gallery**:
  - View all generated images and videos with 2-column grid layout
  - Video indicator on thumbnails
  - Pull-to-refresh to update gallery
  - Multi-select mode with dedicated button or long press to select items
  - Batch operations: save, share, or delete multiple items at once
- **Media management**: Save to device gallery (Pictures/ComfyRenegade or Movies/ComfyRenegade) or share

### Workflows

- **Full node graph visualization**: Colored nodes and connections matching ComfyUI's visual style with smooth Bezier curves or Hermite splines
- **Node management**:
  - Add nodes via searchable Node Browser with category filter chips
  - More options for positive prompt injection instead of default "CLIPTextEncode"
  - Human-readable display names shown where available (with class type as subtitle)
  - Deprecated and experimental nodes clearly marked with warning/science icons
  - Search by display name, class type, category, or description
  - Long-press output slots to open Node Browser filtered by compatible types
  - Delete, duplicate, bypass, and rename nodes
  - Cleanup tool to remove disconnected nodes
- **Node groups**:
  - Organize nodes into collapsible groups with grid layout
  - Rename groups via context menu
  - Groups persist across saves and theme changes
- **Connection management**:
  - Create connections by tapping an output socket and then an input socket (or vice versa)
  - Target sockets glow when compatible during connection creation
  - Delete connections via node context menu or by selecting and deleting
  - Selected nodes highlight connections with animated flowing segments
  - Connected slot circles enlarge and color-code when highlighted
- **Node attribute editing**:
  - Side sheet editor for node parameters (tap nodes to edit)
  - All editable inputs shown, including those using default values
  - Full support for both legacy and new ComfyUI COMBO input formats
  - Numeric steppers with floating labels, range hints, and increment/decrement buttons
  - Boolean values displayed as toggle switches
  - Image previews for image selector fields
  - Template values highlighted with "UI:" prefix
  - Connected inputs styled with wire colors
  - Edited values highlighted for easy identification
- **Workflow notes**:
  - Add notes to workflows with Markdown support
  - Unified title and text editor
  - Notes displayed in the workflow graph
- **Navigation and zoom**:
  - Material Design 3 Expressive floating toolbar
  - Zoom controls with percentage display
  - Fit all / Fit width zoom options
  - Initial drawing animation when opening workflows
- **Workflow management**:
  - Create workflows from scratch or import existing ones from ComfyUI
  - Supported formats:
    - ComfyUI LiteGraph (import only) - native ComfyUI workflow format with subgraph support
    - ComfyUI API JSON (import/export) - workflow format used by the ComfyUI API
    - ComfyRenegade JSON (import/export) - preserves field mappings and app settings
  - Dynamic workflow thumbnails showing mini graph previews
  - Context menus for edit, rename, duplicate, export, and delete
  - Flexible field mapping: only Positive Prompt required (plus source image for image workflows)
  - Dynamic parameter constraints based on workflow node definitions
  - Per-workflow generation settings persistence
  - Workflow Editor returns to view mode after saving for continued editing

### Configuration

- **Server configuration**:
  - View detailed server information (ComfyUI version, OS, Python, PyTorch versions)
  - Monitor hardware resources (RAM and GPU VRAM usage with free/total display)
  - Server management actions (clear queue, clear history)
- **App management**:
  - Clear local cache (generated images, videos, source images, masks)
  - Restore default settings
  - Backup configuration to JSON file (connection settings, workflow values, custom workflows)
  - Restore configuration from backup
- **Workflow management**:
  - View and manage custom workflows with visual thumbnails
  - Upload custom ComfyUI workflows with automatic validation
  - Edit workflow name and description
  - Default generation settings extracted during workflow import
  - Per-workflow generation settings (each workflow remembers its own configuration)
- **Configuration persistence**: Automatically saves and restores all settings including prompts, models, workflow selections, and generation parameters on a per-workflow basis

## Requirements

- Android 13 (API level 33) or higher
- Access to a running ComfyUI server instance
- Network connectivity to reach your ComfyUI server

## Development setup

### Prerequisites

1. **Android Studio** (latest stable version recommended)
2. **JDK 11** or higher
3. **Android SDK** with API level 36

### Building the project

1. Clone the repository:
   ```bash
   git clone https://github.com/legal-hkr/ComfyRenegade.git
   cd ComfyRenegade
   ```

2. Set up your local environment:
   - Ensure `JAVA_HOME` is set to your JDK installation
   - Configure Android SDK path in `local.properties`

3. Build the app:
   ```bash
   ./gradlew assembleDebug
   ```

4. Run on device/emulator:
   ```bash
   ./gradlew installDebug
   ```

### Running tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## Configuration

To connect to your ComfyUI server, you'll need:
- ComfyUI server URL (e.g., `http(s)://192.168.1.100:8188`)
- Network access between your Android device and the ComfyUI server

## Tech stack

- **Language**: Kotlin 2.0.21
- **Min SDK**: Android 13 (API 33)
- **Target SDK**: Android 15 (API 36)
- **UI Framework**: Jetpack Compose with Material Design 3 (including Expressive components)
- **Video Playback**: Media3 ExoPlayer
- **Architecture**: MVVM with ViewModels and StateFlow
- **Navigation**: Jetpack Compose Navigation
- **Build system**: Gradle with Kotlin DSL

## Contributing

This project follows standard Android development practices:
- Kotlin coding conventions
- Jetpack Compose best practices
- Material Design 3 guidelines
- MVVM architecture pattern

## License

[GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)

## Acknowledgments

- [ComfyUI](https://github.com/comfyanonymous/ComfyUI) - The powerful node-based UI this app interfaces with
