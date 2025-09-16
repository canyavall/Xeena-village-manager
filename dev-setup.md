# Development Environment Setup

## Available Development Commands

### Building
- `./gradlew build` - Full build
- `./gradlew build -q` - Quiet build (faster for hot reload testing)

### Running
- `./gradlew runClient` - Start Minecraft client with mod
- `./gradlew runServer` - Start Minecraft server with mod

### Development Tools
- `./gradlew genSources` - Generate Minecraft source code for reference
- `./gradlew tasks --group="fabric"` - List all Fabric development tasks

## IDE Configuration

### VSCode
- `.vscode/settings.json` - Java project settings configured
- `.vscode/tasks.json` - Build and run tasks configured
- Use Ctrl/Cmd+Shift+P -> "Tasks: Run Task" -> Select build/run tasks

### IntelliJ IDEA
- `.idea/runConfigurations/` - Run configurations for client/server
- Import as Gradle project
- Use Run configurations for development

## Hot Reload Testing

1. Make changes to Java files
2. Run `./gradlew build -q` to compile
3. Changes should be reflected in running client (limited hot reload)

## Project Structure
- `src/main/java/com/xeenaa/villagepicker/` - Main mod code
- `src/client/java/com/xeenaa/villagepicker/` - Client-side code
- `src/main/resources/` - Mod resources and configs
- `src/client/resources/` - Client-side resources

## Development Notes
- Java 21 required
- Fabric development environment ready
- Build system configured for Minecraft 1.21.1
- Hot reload limited by Minecraft's architecture