<div align="center">

<img src="assets/banner.svg" alt="Chatcatcher" width="100%">

# Chatcatcher

**输入捕获器** —— 让玩家在聊天框或告示牌里"输入参数"，把输入交给任意命令处理；从 TrMenu 的捕获器独立而来的通用组件。

![Platform](https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-62bda8?style=flat-square&logo=minecraft&logoColor=white)
![Minecraft](https://img.shields.io/badge/MC-1.20.x-8a9a5b?style=flat-square)
![Java](https://img.shields.io/badge/Java-17-f89820?style=flat-square&logo=openjdk&logoColor=white)
![Depends](https://img.shields.io/badge/depend-PlaceholderAPI-e2b90f?style=flat-square)

[功能](#-功能) · [配置](#️-配置)

</div>

## ✨ 功能

- **两种捕获模式**：`CHAT`（聊天框输入）与 `SIGN`（告示牌输入），按需选择交互方式
- **三段生命周期**：每个 catcher 定义 `start`（进入输入态的提示）、`cancel`（取消时执行）、`end`（收到输入后执行）三组动作
- **动作即命令**：三段都支持多行命令，占位符 `%player_name%` 与 `{input}`（玩家输入内容）可用
- **PlaceholderAPI**：所有文案与命令支持 PAPI 变量

## ⚙️ 配置

```yaml
catchers:
  catcher1:
    type: CHAT
    start:
      - 'tell %player_name% &b&lPlease input something:'
    cancel:
      - 'tell %player_name% &8Cancelled'
    end:
      - 'tell %player_name% You entered: {input}'
      - 'give %player_name% diamond {input}'

  catcher2:
    type: SIGN
    start:
      - 'tell %player_name% &b&lPlease input a valid number on the sign'
    end:
      - 'tell %player_name% Set amount to: {input}'
```

其他插件触发 `catcher1` 后，玩家的下一条聊天/告示牌内容就会以 `{input}` 的形式流进 `end` 动作链。

## 🔨 构建

```bash
./gradlew build
```
