# PhyModels

**PhyModels** 是一个基于 Minecraft Paper 1.21.4 构建服务端插件，它利用 **libbulletjme** 物理引擎，为服务器添加了真实物理的模拟体验。

---

## ✨ 核心特性 (Features)

本插件旨在将真实世界的物理法则引入 Minecraft，核心功能包括：

*   🧱 **多方块刚体 (Multi-Block Rigid Bodies)**
    *   将多个方块“粘合”成一个单一的的物理实体。
    *   这些结构可以作为一个整体进行移动、旋转、碰撞和下落。

*   🤝 **动态交互 (Dynamic Interactions)**
    *   玩家和生物可以与已经物理化的对象进行真实的交互。
    *   推倒一堵墙，炸倒一个建筑。
    *   爆炸效果会真实地将刚体炸飞，而不是简单地摧毁方块。

*   💧 **高级流体与浮力 (Advanced Fluids & Buoyancy)**
    *   **真实浮力**: 物体会根据其“密度”在水中漂浮或下沉，而不仅仅是变成掉落物。
    *   **基于粒子的流体**: 基于粒子的流体，可以模拟真实的水流、飞溅和压力效果。

*   🏳️ **布料模拟 (Cloth Simulation)**
    *   创建可以随风飘动、与物体和玩家发生碰撞的布料。 *虽然说并没有风*

*   📜 **完了我写错了 (Scriptable Rigid Bodies)**
    *   为物理对象赋予生命！通过简单的脚本（例如 JavaScript/Lua），你可以自定义刚体的行为。
    *   创建自动门、陷阱、弹射器、或任何你能想象到的机械装置。

## 🚀 快速开始 (Getting Started)

### 前置要求
*   服务器核心: **Paper** 1.21+

### 安装步骤
1.  从 Release 下载最新的 `PhyModels-vX.X.X.jar` 文件。
2.  将下载的 `.jar` 文件放入你服务器的 `plugins` 文件夹中。
3.  重启你的服务器。插件将自动加载。

## 📖 如何使用 (Usage)

### 创建一个多方块刚体
1.  使用燧石破坏方块或选择工具选定一个区域的方块。
2.  使用指令:
    ```bash
    /generatemodel <模型名称> <尺寸>
    ```
    *   `<模型名称>`: 已经被加载的模型。
    *   `<尺寸>`: (可选) 设置刚体的整体大小。
    .
    **示例**: `/generatemodel stone 9`


## 📚 指令与权限 (Commands & Permissions)

| 指令 (Command)                      | 权限 (Permission)                     | 描述 (Description)                               |
| ----------------------------------- | ------------------------------------- | ------------------------------------------------ |
| `/addmodel`  | `phymodels.command`                | 创建一个物理对象 (刚体, 布料, 流体等)。        |
| `/addscript`              | `phymodels.command`                | 创建一个脚本物理对象。                               |
| `/cloth`                | `phymodels.command`                   | 创建布料。                         |
| `/generatemodel`                     | `phymodels.command`                   | 生成物理对象。                     |
| `/liquidclear`           | `phymodels.command`               | 清空所有流体。                               |
| `/modelinfo`           | `phymodels.command`               | 模型信息。                               |
| `/phyperform`           | `phymodels.command`               | 性能分析。                               |
| `/removemodel`           | `phymodels.command`               | 移除物理实体。                               |
| `/scriptstart`           | `phymodels.command`               | 开始脚本。                               |
| `/toggledebug`           | `phymodels.command`               | 切换debug。                               |

   


## 🤝 贡献 (Contributing)

我们非常欢迎社区的贡献！如果你有任何好的想法、Bug 修复或功能建议，请：
1.  Fork 本项目。
2.  创建一个新的分支 (`git checkout -b feature/AmazingFeature`)。
3.  提交你的更改 (`git commit -m 'Add some AmazingFeature'`)。
4.  将你的分支推送到远程 (`git push origin feature/AmazingFeature`)。
5.  提交一个 Pull Request。

你也可以通过 [Issues 页面](https://github.com/wiyuka0/PhyModels/issues) 报告 Bug 或提出建议。

## 🙏 致谢 (Acknowledgements)
*   **libbulletjme** & **jMonkeyEngine**: 提供了强大的底层物理引擎和 3D 功能。
*   **PaperMC Team**: 创造了性能卓越的服务端核心。

## 📜 许可证 (License)
本项目采用 [MIT 许可证](./LICENSE) 开源。

---
