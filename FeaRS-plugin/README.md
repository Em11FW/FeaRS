# FeaRS Android Studio Plugin


## Installation
### Binary
To install using pre-built binary, first download the latest release (`FeaRS.zip`). Then, go to `Android Studio IDE | Preference | Plugin | install plugin from Hard`.

To have a intuitive understanding of the plugin usage, please refer to https://github.com/USI-INF-Software/ICSE21-ReplicationPackage-Fears/tree/master/plugin. 

### Source code
- **Using in-project IDEA project files**:
After cloning and opening the project via IntelliJ IDEA, you need to follow [Setting Up a Development Environment](./setup-and-deploy.md#i-setting-up-a-development-environment) section. Afterwards, create a new `Run | Edit Configuration | + | Plugin` and use the newly created *IntelliJ Platform SDK* as the SDK (requires Java 1.8+).
- **Creating the project from scratch**:
Follow both [Setting Up a Development Environment and Creating a Plugin Project](./setup-and-deploy.md#i-setting-up-a-development-environment) steps and copy relevant files (sources, resources, ...) to corresponding folders in your own project.

In the end, deploy the project by following the [Deploying a Plugin](./setup-and-deploy.md#deploying-a-plugin) section.


