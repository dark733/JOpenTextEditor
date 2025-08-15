# JOpenTextEditor - Enhanced Retro Text Editor

A modern, feature-rich retro-styled text editor built with Java Swing that combines nostalgic aesthetics with contemporary functionality.

![Version](https://img.shields.io/badge/version-2.0-brightgreen)
![Java](https://img.shields.io/badge/java-8%2B-orange)
![License](https://img.shields.io/badge/license-Open%20Source-blue)

## 🌟 Features

### Core Editing
- **Rich Text Editing** - Full-featured text area with syntax highlighting support
- **Line Numbers** - Dynamic line numbering with retro styling
- **Word Wrap** - Toggle word wrapping on/off
- **Unlimited Undo/Redo** - Complete editing history with Ctrl+Z/Ctrl+Y
- **Auto-Save** - Automatic saving every 30 seconds (configurable)

### File Management
- **Multiple File Formats** - Support for .txt, .java, and all file types
- **Recent Files** - Quick access to recently opened files (up to 10)
- **Smart Save Dialog** - Overwrite confirmation and file extension handling
- **Auto-Recovery** - Unsaved changes protection

### Advanced Search & Navigation
- **Find & Replace Dialog** - Comprehensive search and replace functionality
  - Case-sensitive search
  - Whole word matching
  - Find next/previous with F3/Shift+F3
  - Replace all with count feedback
- **Go to Line** - Quick navigation to specific line numbers (Ctrl+G)
- **Text Selection** - Advanced text selection and highlighting

### View & Display
- **Zoom Controls** - Zoom in/out with Ctrl+Plus/Minus, reset with Ctrl+0
- **Font Customization** - Complete font chooser with preview
- **Status Bar** - Real-time display of:
  - Current line and column position
  - Document length and word count
  - Modification status
  - Auto-save notifications

### Tools & Utilities
- **Document Statistics** - Detailed word, character, and line counts
- **Date/Time Insertion** - Insert current date/time with Ctrl+D
- **Print Support** - Direct printing functionality
- **Keyboard Shortcuts Help** - Built-in shortcut reference (F1)

### User Interface
- **Retro Theme** - Consistent brown/tan color scheme throughout
- **Modern Toolbar** - Quick access buttons for common operations
- **Enhanced Menus** - Organized menu structure with mnemonics
- **Dialog Windows** - Professional, themed dialog boxes
- **Responsive Design** - Proper window management and sizing

## 🎨 Retro Design Theme

The editor maintains a consistent retro aesthetic with:
- **Background**: Warm cream (#F5E3A1) 
- **Text**: Rich brown (#8B4513)
- **Highlights**: Orange-red accents (#FF4500)
- **Menus**: Dark brown backgrounds with cream text
- **Classic monospace fonts** (Consolas/Monospaced)

## 🚀 Installation & Usage

### Requirements
- Java 8 or higher
- Any operating system supporting Java Swing

### Running the Application

#### From JAR file:
```bash
java -jar RetroTextEditor.jar
```

#### From source:
```bash
javac RetroTextEditor.java
java RetroTextEditor
```

## ⌨️ Keyboard Shortcuts

### File Operations
| Shortcut | Action |
|----------|--------|
| `Ctrl+N` | New Document |
| `Ctrl+O` | Open File |
| `Ctrl+S` | Save File |
| `Ctrl+Shift+S` | Save As |
| `Ctrl+P` | Print Document |
| `Ctrl+Q` | Exit Application |

### Edit Operations
| Shortcut | Action |
|----------|--------|
| `Ctrl+Z` | Undo |
| `Ctrl+Y` | Redo |
| `Ctrl+X` | Cut |
| `Ctrl+C` | Copy |
| `Ctrl+V` | Paste |
| `Ctrl+A` | Select All |

### Search & Navigation
| Shortcut | Action |
|----------|--------|
| `Ctrl+F` | Find & Replace |
| `Ctrl+H` | Find & Replace (alternative) |
| `F3` | Find Next |
| `Shift+F3` | Find Previous |
| `Ctrl+G` | Go to Line |
| `Ctrl+L` | Go to Line (alternative) |

### View Controls
| Shortcut | Action |
|----------|--------|
| `Ctrl+Plus` | Zoom In |
| `Ctrl+Equals` | Zoom In (alternative) |
| `Ctrl+Minus` | Zoom Out |
| `Ctrl+0` | Reset Zoom |

### Utilities
| Shortcut | Action |
|----------|--------|
| `Ctrl+D` | Insert Date/Time |
| `F1` | Show Keyboard Shortcuts |

## 🔧 Advanced Features

### Auto-Save Configuration
- Automatically saves files every 30 seconds
- Can be toggled on/off from Tools menu
- Shows auto-save notifications in status bar
- Only saves existing files (not new unsaved documents)

### Recent Files Management
- Maintains list of up to 10 recently opened files
- Stored in user home directory (`.retrotexteditor/recent.txt`)
- Automatically removes non-existent files
- Quick access through File → Recent Files menu

### Find & Replace Options
- **Case Sensitive**: Match exact case
- **Whole Word**: Match complete words only
- **Wrap Around**: Continue search from beginning/end
- **Replace All**: Batch replacement with count feedback

### Document Statistics
Access detailed document information including:
- Total characters (with and without spaces)
- Word count using smart word detection
- Line count
- Current cursor position

## 🏗️ Technical Details

### Architecture
- Built with Java Swing for cross-platform compatibility
- Event-driven architecture with proper separation of concerns
- Memory-efficient text handling for large files
- Robust error handling and user feedback

### File Handling
- Supports all text file formats
- UTF-8 encoding support
- Automatic file extension detection
- Safe save operations with backup handling

### Performance
- Efficient line numbering updates
- Optimized text search algorithms
- Minimal memory footprint
- Responsive UI even with large documents

## 🤝 Contributing

This is an open-source project. Contributions are welcome!

### Development Setup
1. Clone the repository
2. Ensure Java 8+ is installed
3. Compile with `javac *.java`
4. Run with `java RetroTextEditor`

### Code Style
- Follow Java naming conventions
- Maintain the retro color scheme consistency
- Add appropriate comments and documentation
- Test all features before submitting

## 📈 Version History

### Version 2.0 (Current)
- Complete UI overhaul with toolbar and enhanced menus
- Find & Replace functionality with advanced options
- Go to Line feature
- Document statistics and word count
- Zoom controls and font customization
- Auto-save with user preferences
- Recent files management
- Print support
- Comprehensive keyboard shortcuts
- Enhanced status bar
- Professional dialog windows
- Improved error handling

### Version 1.0 (Original)
- Basic text editing functionality
- Simple file open/save operations
- Basic undo/redo support
- Line numbering
- Retro color scheme
- Font selection dialog

## 👨‍💻 Author

**Juzer Baatwala**
- Original concept and implementation
- Enhanced version with advanced features

## 📄 License

Open Source - Feel free to use, modify, and distribute.

---

*Enjoy writing with retro style! 🎯*